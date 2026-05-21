package com.si.myfoodordering.data.repository

import com.si.myfoodordering.data.model.UserProfile
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val supabase: SupabaseClient
) {
    private val auth = supabase.auth
    private val postgrest = supabase.postgrest

    /**
     * @return null si succès, sinon message d’erreur à afficher.
     * Le signIn juste après signUp peut échouer (timing) alors que le compte est créé : on réessaie avec délais.
     */
    suspend fun signUp(emailText: String, passwordText: String, name: String): String? = withContext(Dispatchers.IO) {
        try {
            auth.signUpWith(Email) {
                email = emailText
                password = passwordText
                data = buildJsonObject {
                    put("nom", name)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext humanizeAuthException(e)
        }

        delay(300)
        if (auth.currentUserOrNull() != null) return@withContext null

        repeat(3) { attempt ->
            delay((attempt + 1) * 350L)
            try {
                auth.signInWith(Email) {
                    email = emailText
                    password = passwordText
                }
            } catch (e: Exception) {
                println("signUp→signIn tentative ${attempt + 1}: ${e.message}")
                e.printStackTrace()
            }
            if (auth.currentUserOrNull() != null) return@withContext null
        }

        return@withContext buildString {
            append("Compte peut‑être créé, mais aucune session locale. ")
            append("Va sur « Connexion » avec le même email et mot de passe. ")
            append("(Si ça persiste : Auth → Providers → Email activé, confirmation email désactivée.)")
        }
    }

    private fun humanizeAuthException(e: Exception): String {
        val m = e.message ?: ""
        return when {
            m.contains("User already registered", ignoreCase = true) ||
                m.contains("already been registered", ignoreCase = true) ->
                "Cet email est déjà utilisé — connecte‑toi ou utilise « mot de passe oublié » dans Supabase."

            m.contains("Password", ignoreCase = true) &&
                (m.contains("short", ignoreCase = true) || m.contains("least", ignoreCase = true)) ->
                "Mot de passe trop court : au moins 6 caractères (règle Supabase)."

            m.contains("valid email", ignoreCase = true) ->
                "Adresse email invalide."

            else -> m.ifBlank { "Erreur : ${e.javaClass.simpleName}" }
        }
    }

    suspend fun signIn(emailText: String, passwordText: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            auth.signInWith(Email) {
                email = emailText
                password = passwordText
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun getUserProfile(): UserProfile? = withContext(Dispatchers.IO) {
        return@withContext try {
            val user = auth.currentUserOrNull()
            if (user == null) {
                println("getUserProfile: Aucun utilisateur connecté dans la session Supabase")
                return@withContext null
            }
            val userId = user.id.toString()
            println("getUserProfile: Tentative de récupération du profil pour ID: $userId")

            val response = postgrest.from("profiles").select {
                filter { eq("id", userId) }
            }.decodeSingle<UserProfile>()
            
            println("getUserProfile: Profil récupéré avec succès pour $userId")
            response
        } catch (e: Exception) {
            println("getUserProfile ERROR: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    suspend fun updateProfile(name: String, phone: String, address: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val user = auth.currentUserOrNull()
            if (user == null) {
                println("updateProfile: Aucun utilisateur connecté")
                return@withContext false
            }
            val userId = user.id.toString()
            println("updateProfile: Début de mise à jour pour $userId avec: name=$name, phone=$phone, addr=$address")

            postgrest.from("profiles").update(
                {
                    set("nom", name)
                    set("telephone", phone)
                    set("adresse", address)
                }
            ) {
                filter { eq("id", userId) }
            }
            
            println("updateProfile: Mise à jour terminée avec succès pour $userId")
            true
        } catch (e: Exception) {
            println("updateProfile ERROR: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    /**
     * Si le trigger SQL n'a pas créé la ligne (ou course au démarrage), l'utilisateur authentifié
     * peut insérer sa propre ligne grâce à la politique RLS `profiles_insert_own`.
     */
    suspend fun insertProfileIfMissing(nom: String, email: String): Boolean = withContext(Dispatchers.IO) {
        val user = auth.currentUserOrNull() ?: return@withContext false
        return@withContext try {
            if (getUserProfile() != null) return@withContext true
            val row = UserProfile(
                id = user.id.toString(),
                nom = nom,
                email = email
            )
            postgrest.from("profiles").insert(row)
            true
        } catch (e: Exception) {
            println("insertProfileIfMissing: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    suspend fun signOut() {
        auth.signOut()
    }

    fun getCurrentUser() = try { auth.currentUserOrNull() } catch (e: Exception) { null }

    /**
     * @return null si succès, sinon message d’erreur affichable.
     */
    suspend fun changePassword(currentPassword: String, newPassword: String): String? = withContext(Dispatchers.IO) {
        val sessionUser = auth.currentUserOrNull()
            ?: return@withContext "Session expirée. Reconnecte-toi."

        val email = sessionUser.email?.takeIf { it.isNotBlank() }
            ?: getUserProfile()?.email?.takeIf { it.isNotBlank() }
            ?: return@withContext "Impossible de récupérer l’email du compte."

        if (newPassword.length < 6) {
            return@withContext "Le nouveau mot de passe doit contenir au moins 6 caractères."
        }
        if (currentPassword == newPassword) {
            return@withContext "Le nouveau mot de passe doit être différent de l’actuel."
        }

        try {
            auth.signInWith(Email) {
                this.email = email
                password = currentPassword
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext "Mot de passe actuel incorrect."
        }

        try {
            auth.updateUser {
                password = newPassword
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext humanizeAuthException(e)
        }

        null
    }
}
