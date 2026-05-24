package com.magazines.catalog.data.remote

import android.util.Log
import com.magazines.catalog.BuildConfig

object CoverUrlResolver {

    fun resolve(url: String?, logTag: String = TAG_COVER): String? {
        if (url.isNullOrBlank()) return null

        val resolved = when {
            url.startsWith("http://", ignoreCase = true) ||
                url.startsWith("https://", ignoreCase = true) -> url
            url.startsWith("/") -> {
                val base = BuildConfig.BASE_URL.trimEnd('/')
                "$base$url"
            }
            else -> {
                val base = BuildConfig.BASE_URL.trimEnd('/')
                "$base/${url.trimStart('/')}"
            }
        }

        Log.d(logTag, "url = $resolved (raw = $url)")
        return resolved
    }

    private const val TAG_COVER = "Cover"
}
