package com.ninezero.halo.core.network

import com.ninezero.halo.core.network.upload.LocalFileReadException
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import retrofit2.Response
import timber.log.Timber
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/** ApiResponse 호출 래퍼 */
suspend inline fun <reified T> safeApiCall(
    apiCall: suspend () -> Response<ApiResponse<T>>
): Result<T> {
    return try {
        val response = apiCall()

        if (response.isSuccessful) {
            val body = response.body()
            if (body != null && body.success) {
                body.data?.let { return Result.success(it) }

                // 페이로드 없는 성공 응답
                if (T::class == Unit::class) {
                    @Suppress("UNCHECKED_CAST")
                    return Result.success(Unit as T)
                }

                // String이면 message만 있는 성공도 허용
                if (T::class == String::class && body.message != null) {
                    @Suppress("UNCHECKED_CAST")
                    return Result.success(body.message as T)
                }
            }
        }
        Result.failure(responseFailure(response))
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(exceptionFailure(e))
    }
}

/** 데이터 없는 ApiResponse 호출 래퍼 */
suspend fun <T> safeApiCallNoData(
    apiCall: suspend () -> Response<ApiResponse<T>>
): Result<Unit> {
    return try {
        val response = apiCall()

        if (response.isSuccessful && response.body()?.success == true) {
            Result.success(Unit)
        } else {
            Result.failure(responseFailure(response))
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(exceptionFailure(e))
    }
}

/** Response<Unit> 호출 래퍼 */
suspend fun safeApiCallUnit(
    apiCall: suspend () -> Response<Unit>
): Result<Unit> {
    return try {
        val response = apiCall()

        if (response.isSuccessful) {
            Result.success(Unit)
        } else {
            Result.failure(responseFailure(response))
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(exceptionFailure(e))
    }
}

/** 실패 응답 → ApiException */
@PublishedApi
internal fun responseFailure(response: Response<*>): ApiException {
    return if (response.isSuccessful) {  // 2xx인데 success=false 또는 data 누락
        val error = backendError((response.body() as? ApiResponse<*>).backendErrorMessage(), response.code())
        Timber.e("API 오류: ${error.message}")
        error
    } else {
        val errorResponse = decodeApiError(response.errorBody()?.string())
        val error = backendError(errorResponse.backendErrorMessage(), response.code())
        Timber.e("HTTP 오류 [${response.code()}]: ${error.message}")
        error
    }
}

/** 호출 중 예외 → ApiException */
@PublishedApi
internal fun exceptionFailure(e: Exception): ApiException {
    val error = when (e) {
        is UnknownHostException, is ConnectException -> ApiException(0, "네트워크 연결을 확인해주세요")
        is SocketTimeoutException -> ApiException(0, "요청 시간이 초과되었습니다")
        is HttpException -> {
            val errorResponse = decodeApiError(e.response()?.errorBody()?.string())
            backendError(errorResponse.backendErrorMessage(), e.code())
        }
        is ApiException -> e
        is LocalFileReadException -> ApiException(0, "첨부 파일을 읽지 못했습니다")
        // 끊김·리셋·SSL 등 나머지 통신 오류
        is IOException -> ApiException(0, "네트워크 연결을 확인해주세요")
        is SerializationException -> ApiException(0, "서버 응답을 처리하지 못했습니다")
        else -> ApiException(0, "알 수 없는 오류")
    }
    Timber.e(e, "네트워크 예외: ${error.message}")
    return error
}

private fun backendError(errorMessage: String?, statusCode: Int): ApiException {
    return when {
        statusCode == 401 || statusCode == 403 ->  // 403을 401과 구분
            ApiException(statusCode, errorMessage ?: "로그인이 필요합니다")
        statusCode == 404 -> ApiException(statusCode, errorMessage ?: "리소스를 찾을 수 없습니다")
        statusCode == 409 -> ApiException(statusCode, errorMessage ?: "충돌이 발생했습니다")
        statusCode >= 500 -> ApiException(500, "서버 오류가 발생했습니다")
        else -> ApiException(statusCode, errorMessage ?: "알 수 없는 오류")
    }
}

// 관대한 설정 파싱
private val errorJson = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
    isLenient = true
}

private fun decodeApiError(raw: String?): ApiResponse<Unit>? {
    return try {
        errorJson.decodeFromString<ApiResponse<Unit>>(raw ?: "")
    } catch (e: Exception) {
        null
    }
}

private fun ApiResponse<*>?.backendErrorMessage(): String? {
    return this?.error ?: this?.message
}
