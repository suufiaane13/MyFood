package com.si.myfoodordering.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.painter.ColorPainter
import coil.compose.AsyncImage
import coil.request.ImageRequest

/** Image Unsplash par défaut (résolution suffisante pour détails nets après découpe). */
private const val FallbackPlatImageUrl =
    "https://images.unsplash.com/photo-1513104890138-7c749659a591?q=80&w=1600"

/**
 * Photo plat : recadrage centré (meilleur rendu sur plats), crossfade Coil,
 * fond neutre pendant le chargement / en erreur.
 */
@Composable
fun PlatCoverAsyncImage(
    imageUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    alignment: Alignment = Alignment.Center,
) {
    val scheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val url = imageUrl?.trim()?.takeIf { it.isNotEmpty() } ?: FallbackPlatImageUrl
    val model = remember(url, context) {
        ImageRequest.Builder(context)
            .data(url)
            .crossfade(280)
            .build()
    }
    val placeholder = remember(scheme.surfaceVariant) { ColorPainter(scheme.surfaceVariant) }
    val errorPainter = remember(scheme.outlineVariant) { ColorPainter(scheme.outlineVariant) }

    AsyncImage(
        model = model,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
        alignment = alignment,
        placeholder = placeholder,
        error = errorPainter,
    )
}
