package com.magazines.catalog.data.remote

import retrofit2.Response
import java.io.IOException

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val code: Int, val message: String) : ApiResult<Nothing>()
    object NetworkError : ApiResult<Nothing>()
    object Unauthorized : ApiResult<Nothing>()
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
        } else if (response.code() == 401) {
            ApiResult.Unauthorized
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
        } else if (response.code() == 401) {
            ApiResult.Unauthorized
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

fun apiErrorMessage(result: ApiResult.Error): String = result.toDisplayMessage()

fun ApiResult.Error.toDisplayMessage(): String = when (code) {
    401 -> "Сессия истекла. Войдите снова"
    else -> message
}

fun unauthorizedMessage(): String = "Сессия истекла. Войдите снова"

fun <T> ApiResult<T>.handleUnauthorized(): ApiResult<T> = when (this) {
    ApiResult.Unauthorized -> ApiResult.Error(401, unauthorizedMessage())
    else -> this
}

/** Maps [ApiResult.Unauthorized] to [ApiResult.Error] for repository `when` branches. */
fun <T> ApiResult<T>.mapUnauthorizedToError(): ApiResult<T> = handleUnauthorized()

/** No-op handler for exhaustive `when`; session logout is handled by [UnauthorizedInterceptor]. */
fun ApiResult<*>.ignoreUnauthorized() {}

fun ApiResult<*>.errorMessageOrNull(): String? = when (this) {
    is ApiResult.Error -> toDisplayMessage()
    ApiResult.Unauthorized -> unauthorizedMessage()
    ApiResult.NetworkError -> "Нет подключения к сети"
    is ApiResult.Success -> null
}
