package com.ninezero.halo.core.di

import android.os.Build
import com.ninezero.halo.BuildConfig
import com.ninezero.halo.core.network.auth.AuthInterceptor
import com.ninezero.halo.core.network.auth.TokenAuthenticator
import com.ninezero.halo.core.network.auth.TokenRefreshApi
import com.ninezero.halo.core.util.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor { message ->
            Timber.tag("OkHttp").d(message)
        }.apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator
    ): OkHttpClient {
        val deviceModel = Build.MODEL.filter { it.code in 32..126 }.ifBlank { "Unknown" }
        val userAgent =
            "Halo/${BuildConfig.VERSION_NAME} (Android ${Build.VERSION.RELEASE}; $deviceModel; Mobile)"
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                chain.proceed(
                    chain.request().newBuilder()
                        .header("User-Agent", userAgent)
                        .build()
                )
            }
            .addInterceptor { chain ->
                // 청구를 기다리는 결제 경로는 타임아웃 연장
                val request = chain.request()
                val path = request.url.encodedPath
                if (request.method == "POST" && BILLING_CHARGE_PATHS.any { path.endsWith(it) }) {
                    chain.withReadTimeout(BILLING_CHARGE_READ_TIMEOUT_SECONDS, TimeUnit.SECONDS).proceed(request)
                } else {
                    chain.proceed(request)
                }
            }
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .authenticator(tokenAuthenticator)
            .connectTimeout(Constants.CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(Constants.READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(Constants.WRITE_TIMEOUT, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        json: Json
    ): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    @RefreshAuth
    fun provideRefreshOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(Constants.CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(Constants.READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(Constants.WRITE_TIMEOUT, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    @RefreshAuth
    fun provideRefreshRetrofit(
        @RefreshAuth okHttpClient: OkHttpClient,
        json: Json
    ): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    @RefreshAuth
    fun provideTokenRefreshApi(@RefreshAuth retrofit: Retrofit): TokenRefreshApi {
        return retrofit.create(TokenRefreshApi::class.java)
    }

    @Provides
    @Singleton
    @DirectUpload
    fun provideDirectUploadOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(Constants.CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(UPLOAD_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(UPLOAD_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }

    private const val UPLOAD_TIMEOUT_SECONDS = 60L

    // 토스 청구를 기다리는 경로, 서버 대기보다 길게
    private val BILLING_CHARGE_PATHS = listOf(
        "/api/v1/billing/pay",
        "/api/v1/subscriptions",
        "/api/v1/subscriptions/upgrade"
    )
    private const val BILLING_CHARGE_READ_TIMEOUT_SECONDS = 75
}
