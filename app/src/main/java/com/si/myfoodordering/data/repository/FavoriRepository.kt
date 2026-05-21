package com.si.myfoodordering.data.repository

import com.si.myfoodordering.data.model.Favori
import com.si.myfoodordering.data.model.Plat
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import javax.inject.Inject

@Serializable
private data class FavoriPlat(val plat: Plat)

class FavoriRepository @Inject constructor(
    private val supabase: SupabaseClient
) {
    private val postgrest = supabase.postgrest
    private val auth = supabase.auth

    /** Liste des plats mis en favori par l'utilisateur connecté (jointure favoris → plats). */
    suspend fun getFavoris(): List<Plat> = withContext(Dispatchers.IO) {
        val userId = auth.currentUserOrNull()?.id ?: return@withContext emptyList()
        return@withContext try {
            postgrest.from("favoris").select(Columns.raw("plat:plats(*)")) {
                filter { eq("user_id", userId) }
            }.decodeList<FavoriPlat>().map { it.plat }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /** IDs de tous les plats en favori de l'utilisateur connecté. */
    suspend fun getFavoriIds(): Set<Int> = withContext(Dispatchers.IO) {
        val userId = auth.currentUserOrNull()?.id ?: return@withContext emptySet()
        return@withContext try {
            postgrest.from("favoris").select {
                filter { eq("user_id", userId) }
            }.decodeList<Favori>().mapNotNull { it.plat_id.takeIf { id -> id > 0 } }.toSet()
        } catch (e: Exception) {
            e.printStackTrace()
            emptySet()
        }
    }

    suspend fun addFavori(platId: Int): Boolean = withContext(Dispatchers.IO) {
        val userId = auth.currentUserOrNull()?.id ?: return@withContext false
        return@withContext try {
            val row = Favori(user_id = userId, plat_id = platId)
            postgrest.from("favoris").insert(row)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun removeFavori(platId: Int): Boolean = withContext(Dispatchers.IO) {
        val userId = auth.currentUserOrNull()?.id ?: return@withContext false
        return@withContext try {
            postgrest.from("favoris").delete {
                filter {
                    eq("user_id", userId)
                    eq("plat_id", platId)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Bascule le favori pour un plat.
     * @return true si le plat est maintenant en favori, false sinon.
     */
    suspend fun toggleFavori(platId: Int, currentlyFavori: Boolean): Boolean = withContext(Dispatchers.IO) {
        return@withContext if (currentlyFavori) {
            removeFavori(platId)
            false
        } else {
            addFavori(platId)
            true
        }
    }
}
