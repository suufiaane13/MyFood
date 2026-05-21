package com.si.myfoodordering.data.repository

import com.si.myfoodordering.data.model.Category
import com.si.myfoodordering.data.model.CategoryPatch
import com.si.myfoodordering.data.model.Plat
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

class PlatRepository @Inject constructor(
    private val supabase: SupabaseClient
) {
    private val postgrest = supabase.postgrest

    companion object {
        /** Bucket public utilisé dans seed_data_menu.sql / Supabase Storage */
        private const val PLATS_IMAGE_BUCKET = "plats"
    }

    /**
     * Envoie une image sur Storage et retourne l’URL publique (pour [Plat.image_url]).
     * En cas d’échec (RLS, bucket manquant, hors ligne), [Result.failure] contient le détail.
     */
    suspend fun uploadPlatCoverImage(bytes: ByteArray, mimeType: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val ext = when {
                mimeType.contains("png", ignoreCase = true) -> "png"
                mimeType.contains("webp", ignoreCase = true) -> "webp"
                mimeType.contains("gif", ignoreCase = true) -> "gif"
                else -> "jpg"
            }
            val path = "covers/${UUID.randomUUID()}.$ext"
            val bucket = supabase.storage.from(PLATS_IMAGE_BUCKET)
            bucket.upload(path, bytes) {
                upsert = true
            }
            bucket.publicUrl(path)
        }.onFailure { it.printStackTrace() }
    }

    /** Charge plats + catégories en une fois ; propage l’exception si le réseau ou Supabase échoue. */
    suspend fun loadMenu(): Pair<List<Plat>, List<Category>> = withContext(Dispatchers.IO) {
        val plats = postgrest.from("plats").select().decodeList<Plat>()
        val categories = postgrest.from("categories").select().decodeList<Category>()
        plats to categories
    }

    suspend fun addPlat(plat: Plat) = withContext(Dispatchers.IO) {
        try {
            postgrest.from("plats").insert(plat)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun updatePlat(plat: Plat) = withContext(Dispatchers.IO) {
        try {
            postgrest.from("plats").update(plat) {
                filter { eq("id", plat.id ?: 0) }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun deletePlat(platId: Int) = withContext(Dispatchers.IO) {
        try {
            postgrest.from("plats").delete {
                filter { eq("id", platId) }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun addCategory(category: Category) = withContext(Dispatchers.IO) {
        try {
            val row = Category(id = null, nom = category.nom, emoji = category.emoji?.ifBlank { null })
            postgrest.from("categories").insert(row)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun updateCategory(category: Category) = withContext(Dispatchers.IO) {
        try {
            val id = category.id ?: return@withContext false
            val patch = CategoryPatch(
                nom = category.nom,
                emoji = category.emoji?.ifBlank { null }
            )
            postgrest.from("categories").update(patch) {
                filter { eq("id", id) }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun deleteCategory(categoryId: Int) = withContext(Dispatchers.IO) {
        try {
            postgrest.from("categories").delete {
                filter { eq("id", categoryId) }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
