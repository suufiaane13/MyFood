package com.si.myfoodordering.data.repository

import com.si.myfoodordering.data.model.Avis
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AvisRepository @Inject constructor(
    private val supabase: SupabaseClient
) {
    private val postgrest = supabase.postgrest
    private val auth = supabase.auth

    /** Toutes les notes pour un plat (lecture publique). */
    suspend fun getAvisForPlat(platId: Int): List<Avis> = withContext(Dispatchers.IO) {
        return@withContext try {
            postgrest.from("avis").select {
                filter { eq("plat_id", platId) }
            }.decodeList<Avis>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /** Tous les avis de tous les plats (lecture publique — pas de filtre user). */
    suspend fun getAllAvis(): List<Avis> = withContext(Dispatchers.IO) {
        return@withContext try {
            postgrest.from("avis").select().decodeList<Avis>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /** Note de l'utilisateur connecté pour ce plat, ou null s'il n'a pas encore noté. */
    suspend fun getUserAvisForPlat(platId: Int): Avis? = withContext(Dispatchers.IO) {
        val userId = auth.currentUserOrNull()?.id ?: return@withContext null
        return@withContext try {
            postgrest.from("avis").select {
                filter {
                    eq("plat_id", platId)
                    eq("user_id", userId)
                }
            }.decodeList<Avis>().firstOrNull()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Insère ou met à jour la note de l'utilisateur connecté pour un plat.
     * La contrainte UNIQUE (plat_id, user_id) + ignoreDuplicates=false assure l'upsert.
     */
    suspend fun upsertAvis(platId: Int, note: Int): Boolean = withContext(Dispatchers.IO) {
        val userId = auth.currentUserOrNull()?.id ?: return@withContext false
        return@withContext try {
            val row = Avis(plat_id = platId, user_id = userId, note = note)
            postgrest.from("avis").upsert(row) {
                onConflict = "plat_id,user_id"
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /** Supprime l'avis de l'utilisateur connecté pour ce plat. */
    suspend fun deleteAvis(avisId: Int): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            postgrest.from("avis").delete {
                filter { eq("id", avisId) }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
