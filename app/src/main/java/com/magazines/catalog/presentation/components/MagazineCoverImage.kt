package com.magazines.catalog.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.magazines.catalog.R

private val CoverTopShape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)

@Composable
fun MagazineCoverImage(
    coverUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    val placeholder = painterResource(R.drawable.placeholder_cover)

    val imageModifier = modifier
        .clip(CoverTopShape)
        .background(MaterialTheme.colorScheme.surfaceVariant, CoverTopShape)

    if (!coverUrl.isNullOrBlank()) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(coverUrl)
                .crossfade(true)
                .build(),
            contentDescription = contentDescription,
            modifier = imageModifier,
            contentScale = ContentScale.Crop,
            placeholder = placeholder,
            error = placeholder,
            fallback = placeholder,
        )
    } else {
        Box(
            modifier = imageModifier,
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                contentDescription = contentDescription,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}