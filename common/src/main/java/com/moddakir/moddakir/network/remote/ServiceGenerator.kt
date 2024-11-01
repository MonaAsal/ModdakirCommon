package com.moddakir.moddakir.network.remote

import com.example.moddakirapps.BuildConfig
import com.moddakir.moddakir.network.remote.services.NonAuthService
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Singleton
class ServiceGenerator {
    companion object {

        private const val CONNECTION_TIMEOUT = 60L
        private val logger: HttpLoggingInterceptor
            get() {
                val loggingInterceptor = HttpLoggingInterceptor()
                if (BuildConfig.DEBUG) {
                    loggingInterceptor.apply {
                        loggingInterceptor.level = HttpLoggingInterceptor.Level.HEADERS
                    }.level = HttpLoggingInterceptor.Level.BODY
                }
                return loggingInterceptor
            }

        val nonAuthService =
            createService(NonAuthService::class.java, false)

        fun <T> createService(
            serviceClass: Class<T>
            , isAuth: Boolean

        ): T {

            val okHttpClientBuilder = OkHttpClient().newBuilder()
            okHttpClientBuilder.connectTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS)
            okHttpClientBuilder.readTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS)
            if (isAuth) {
                okHttpClientBuilder.addInterceptor(AuthInterceptor())
                okHttpClientBuilder.authenticator(TokenAuthenticator(nonAuthService))
            } else {
                okHttpClientBuilder.addInterceptor(NoneAuthInterceptor())
            }

            okHttpClientBuilder.addInterceptor(logger)

            val okHttpClient = okHttpClientBuilder.build()

            var retrofit = Retrofit.Builder().baseUrl(BuildConfig.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(Gson()))
                .client(okHttpClient)
                .build()

            return retrofit.create(serviceClass)
        }

        fun <T> createServiceFile(
            serviceClass: Class<T>
            , isAuth: Boolean

        ): T {

            val okHttpClientBuilder = OkHttpClient().newBuilder()
            okHttpClientBuilder.connectTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS)
            okHttpClientBuilder.readTimeout(CONNECTION_TIMEOUT, TimeUnit.SECONDS)
            if (isAuth) {
                okHttpClientBuilder.addInterceptor(AuthInterceptor())
                okHttpClientBuilder.authenticator(TokenAuthenticator(nonAuthService))
            } else {
                okHttpClientBuilder.addInterceptor(NoneAuthInterceptor())
            }

            okHttpClientBuilder.addInterceptor(logger)

            val okHttpClient = okHttpClientBuilder.build()

            var retrofit = Retrofit.Builder().baseUrl(BuildConfig.BASE_URL)
                .client(okHttpClient)
                .build()

            return retrofit.create(serviceClass)
        }
    }
}
