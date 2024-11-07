package com.mobileapp.spoofyrez.data.api

import com.mobileapp.spoofyrez.data.models.AudioFeatures
import com.mobileapp.spoofyrez.data.models.Track
import com.mobileapp.spoofyrez.data.models.RecommendationsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface SpotifyService {
    @GET("v1/tracks/{id}")
    suspend fun getTrack(@Path("id") trackId: String): Response<Track>

    @GET("v1/audio-features/{id}")
    suspend fun getAudioFeatures(@Path("id") trackId: String): Response<AudioFeatures>

    @GET("v1/recommendations")
    suspend fun getRecommendations(
        @Query("seed_tracks") seedTracks: String,
        @Query("target_danceability") targetDanceability: Float? = null,
        @Query("target_energy") targetEnergy: Float? = null,
        @Query("target_valence") targetValence: Float? = null,
        @Query("target_tempo") targetTempo: Float? = null,
        @Query("limit") limit: Int = 20
    ): Response<RecommendationsResponse>
}