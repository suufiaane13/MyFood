package com.si.myfoodordering.data.repository

import com.google.firebase.messaging.FirebaseMessaging
import com.si.myfoodordering.data.model.PushTokenRow
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PushTokenRepository @Inject constructor(
    private val supabase: SupabaseClient,
) {
    private val postgrest = supabase.postgrest
    private val auth = supabase.auth

    /** Enregistre le token FCM courant (une ligne active par appareil : remplace les anciens pour cet utilisateur). */
    suspend fun registerCurrentToken(): Unit = withContext(Dispatchers.IO) {
        try {
            val token = FirebaseMessaging.getInstance().token.await()
            registerToken(token)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun registerToken(token: String) = withContext(Dispatchers.IO) {
        val userId = auth.currentUserOrNull()?.id ?: return@withContext
        try {
            postgrest.from("push_tokens").delete {
                filter {
                    eq("user_id", userId.toString())
                }
            }
            postgrest.from("push_tokens").insert(
                PushTokenRow(
                    user_id = userId.toString(),
                    token = token,
                    platform = "android",
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /** À appeler avant déconnexion (session encore valide pour RLS). */
    suspend fun revokeLocalTokens(): Unit = withContext(Dispatchers.IO) {
        val userId = auth.currentUserOrNull()?.id ?: return@withContext
        try {
            postgrest.from("push_tokens").delete {
                filter {
                    eq("user_id", userId.toString())
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
