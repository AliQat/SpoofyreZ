package com.mobileapp.spoofyrez.data.repository

import com.mobileapp.spoofyrez.data.api.SpotifyService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SongRepository @Inject constructor(
    private val spotifyService: SpotifyService
) {
    suspend fun getSimilarSongs(trackId: String) {
    }
}