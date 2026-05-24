package com.magazines.catalog.presentation.components

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import com.magazines.catalog.R

@Composable
fun MagazineCoverImage(
    coverUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(coverUrl) {
        Log.d("Cover", "coverUrl = $coverUrl")
    }

    val placeholder = painterResource(R.drawable.placeholder_cover)

    if (coverUrl.isNullOrBlank()) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(0.4f),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    } else {
        AsyncImage(
            model = coverUrl,
            contentDescription = contentDescription,
            modifier = modifier,
            placeholder = placeholder,
            error = placeholder,
            contentScale = ContentScale.Crop,
        )
    }
}
