package com.mobileapp.spoofyrez.di

import android.util.Log
import com.mobileapp.spoofyrez.BuildConfig
import com.mobileapp.spoofyrez.data.api.SpotifyService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okhttp3.Credentials
import okhttp3.FormBody
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    private const val TAG = "NetworkModule"
    private const val BASE_URL = "https://api.spotify.com/"
    private const val AUTH_URL = "https://accounts.spotify.com/api/token"

    // Ask me (Ali) on how to get these, your app should crash if you do not have them set up
    private val CLIENT_ID = BuildConfig.SPOTIFY_CLIENT_ID
    private val CLIENT_SECRET = BuildConfig.SPOTIFY_CLIENT_SECRET

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor())
            .build()
    }

    @Provides
    @Singleton
    fun provideSpotifyService(okHttpClient: OkHttpClient): SpotifyService {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SpotifyService::class.java)
    }

    private class AuthInterceptor : Interceptor {
        private var accessToken: String? = null

        override fun intercept(chain: Interceptor.Chain): Response {
            try {
                // If we don't have a token, get one
                if (accessToken == null) {
                    accessToken = getAccessToken()
                }
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $accessToken")
                    .build()

                val response = chain.proceed(request)
                if (response.code() == 401) {
                    accessToken = getAccessToken()
                    val newRequest = chain.request().newBuilder()
                        .addHeader("Authorization", "Bearer $accessToken")
                        .build()
                    response.close()
                    return chain.proceed(newRequest)
                }

                return response
            } catch (e: Exception) {
                Log.e(TAG, "Error in interceptor: ${e.message}")
                throw e
            }
        }

        private fun getAccessToken(): String {
            val client = OkHttpClient()

            val requestBody = FormBody.Builder()
                .add("grant_type", "client_credentials")
                .build()

            val auth = Credentials.basic(CLIENT_ID, CLIENT_SECRET)

            val request = Request.Builder()
                .url(AUTH_URL)
                .post(requestBody)
                .addHeader("Authorization", auth)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build()

            try {
                val response = client.newCall(request).execute()
                val responseBody = response.body()?.string() ?: throw IllegalStateException("Empty response body")
                Log.d(TAG, "Auth response: $responseBody")
                val jsonData = JSONObject(responseBody)
                response.close()

                return jsonData.getString("access_token")
            } catch (e: Exception) {
                Log.e(TAG, "Error getting access token: ${e.message}")
                throw e
            }
        }
    }
}