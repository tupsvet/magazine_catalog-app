package com.magazines.catalog.data.remote

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.magazines.catalog.domain.model.FileData

object UriFileReader {

    fun read(context: Context, uri: Uri, defaultMimeType: String, defaultFileName: String): FileData? {
        val resolver = context.contentResolver
        val mimeType = resolver.getType(uri) ?: defaultMimeType
        val fileName = resolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex >= 0) {
                cursor.getString(nameIndex)
            } else {
                null
            }
        } ?: defaultFileName

        val bytes = resolver.openInputStream(uri)?.use { it.readBytes() } ?: return null
        return FileData(fileName = fileName, mimeType = mimeType, bytes = bytes)
    }
}
