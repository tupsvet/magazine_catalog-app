package com.magazines.catalog.data.remote

import com.magazines.catalog.domain.model.FileData
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

fun FileData.toMultipartPart(fieldName: String): MultipartBody.Part {
    val body = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
    return MultipartBody.Part.createFormData(fieldName, fileName, body)
}

fun String.toTextRequestBody(): RequestBody =
    toRequestBody("text/plain".toMediaTypeOrNull())
