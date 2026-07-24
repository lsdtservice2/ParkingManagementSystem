package com.logisparktech.parkingmanagementsystem.di

import com.logisparktech.parkingmanagementsystem.data.remote.ApiService
import com.logisparktech.parkingmanagementsystem.data.local.preferences.PreferenceManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

//    private const val BASE_URL = "https://logisparktech.com/nepvent-parking/"
    private const val BASE_URL = "http://192.168.1.133:6767/"

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        preferenceManager: PreferenceManager,
        authEventManager: com.logisparktech.parkingmanagementsystem.core.auth.AuthEventManager
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                val original = chain.request()
                val token = preferenceManager.getAccessToken()
                val requestBuilder = original.newBuilder()
                if (token.isNotEmpty()) {
                    requestBuilder.header("Authorization", "Bearer $token")
                }
                val response = chain.proceed(requestBuilder.build())
                
                if (response.code == 401) {
                    preferenceManager.clear()
                    authEventManager.onUnauthorized()
                }
                response
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }


    @Provides
    @Singleton
    fun provideApiService(okHttpClient: OkHttpClient): ApiService {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(ApiService::class.java)
    }
}
