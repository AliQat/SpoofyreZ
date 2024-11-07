package com.mobileapp.spoofyrez.data.api

import com.mobileapp.spoofyrez.data.models.AudioFeatures
import com.mobileapp.spoofyrez.data.models.Track
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface SpotifyService {
    @GET("v1/tracks/{id}")
    suspend fun getTrack(@Path("id") trackId: String): Response<Track>

    @GET("v1/audio-features/{id}")
    suspend fun getAudioFeatures(@Path("id") trackId: String): Response<AudioFeatures>
}