package com.magazines.catalog.data.remote

import retrofit2.Response
import java.io.IOException

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val code: Int, val message: String) : ApiResult<Nothing>()
    object NetworkError : ApiResult<Nothing>()
}

suspend fun <T> safeApiCall(call: suspend () -> Response<T>): ApiResult<T> {
    return try {
        val response = call()
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                ApiResult.Success(body)
            } else {
                ApiResult.Error(
                    code = response.code(),
                    message = "Empty response body",
                )
            }
        } else {
            ApiResult.Error(
                code = response.code(),
                message = response.errorBody()?.string() ?: "Unknown error",
            )
        }
    } catch (_: IOException) {
        ApiResult.NetworkError
    } catch (e: Exception) {
        ApiResult.Error(code = -1, message = e.message ?: "Unknown error")
    }
}

/** For POST/DELETE endpoints that return 204 No Content with an empty body. */
suspend fun safeApiCallNoContent(call: suspend () -> Response<Unit>): ApiResult<Unit> {
    return try {
        val response = call()
        if (response.isSuccessful) {
            ApiResult.Success(Unit)
        } else {
            ApiResult.Error(
                code = response.code(),
                message = response.errorBody()?.string() ?: "Unknown error",
            )
        }
    } catch (_: IOException) {
        ApiResult.NetworkError
    } catch (e: Exception) {
        ApiResult.Error(code = -1, message = e.message ?: "Unknown error")
    }
}
