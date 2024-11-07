package com.mobileapp.spoofyrez.data.repository

import android.util.Log
import com.mobileapp.spoofyrez.data.api.SpotifyService
import com.mobileapp.spoofyrez.data.models.AudioFeatures
import com.mobileapp.spoofyrez.data.models.Track
import com.mobileapp.spoofyrez.data.models.Image
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.abs
import kotlin.math.exp
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SongRepository @Inject constructor(
    private val spotifyService: SpotifyService
) {
    private val TAG = "SongRepository"

    private val testTrackId = "3DW6GVr7RVyfvo4NBRvZIZ"
    private val defaultLimit = 50

    private val _currentTrack = MutableStateFlow<Track?>(null)
    val currentTrack: StateFlow<Track?> = _currentTrack

    private val _similarTracks = MutableStateFlow<List<Track>>(emptyList())
    val similarTracks: StateFlow<List<Track>> = _similarTracks

    suspend fun getSimilarSongs(trackId: String = testTrackId, limit: Int = defaultLimit) {
        try {
            Log.d(TAG, "Finding $limit similar songs for track ID: $trackId")

            val originalTrackResponse = spotifyService.getTrack(trackId)
            val originalFeaturesResponse = spotifyService.getAudioFeatures(trackId)

            if (!originalTrackResponse.isSuccessful || !originalFeaturesResponse.isSuccessful) {
                Log.e(TAG, "Failed to get original track data")
                return
            }

            val originalTrack = originalTrackResponse.body()!!
            val originalFeatures = originalFeaturesResponse.body()!!

            _currentTrack.value = originalTrack

            Log.d(TAG, "Original track: ${originalTrack.name} by ${originalTrack.artists.firstOrNull()?.name}")
            Log.d(TAG, """Original features: 
                |danceability=${originalFeatures.danceability}, 
                |energy=${originalFeatures.energy}, 
                |tempo=${originalFeatures.tempo},
                |valence=${originalFeatures.valence}
            """.trimMargin())

            val recommendationsResponse = spotifyService.getRecommendations(
                seedTracks = trackId,
                targetDanceability = originalFeatures.danceability,
                targetEnergy = originalFeatures.energy,
                targetValence = originalFeatures.valence,
                targetTempo = originalFeatures.tempo,
                limit = limit
            )

            if (!recommendationsResponse.isSuccessful) {
                Log.e(TAG, "Failed to get recommendations")
                return
            }

            val recommendations = recommendationsResponse.body()!!

            _similarTracks.value = recommendations.tracks

            Log.d(TAG, "Found ${recommendations.tracks.size} similar tracks:")

            recommendations.tracks.forEach { track ->
                val featuresResponse = spotifyService.getAudioFeatures(track.id)
                if (featuresResponse.isSuccessful) {
                    val features = featuresResponse.body()!!
                    val similarity = calculateSimilarityScore(originalFeatures, features)

                    Log.d(TAG, """
                        Similar track found:
                        Name: ${track.name}
                        Artist: ${track.artists.firstOrNull()?.name}
                        Similarity score: ${"%.3f".format(similarity)} (1.0 = most similar)
                        Features:
                        - Danceability: ${features.danceability}
                        - Energy: ${features.energy}
                        - Tempo: ${features.tempo}
                        - Valence: ${features.valence}
                    """.trimIndent())
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error finding similar songs", e)
        }
    }

    fun getSongName(): String? = currentTrack.value?.name

    fun getArtistName(): String? = currentTrack.value?.artists?.firstOrNull()?.name

    fun getPopularity(): Int? = currentTrack.value?.popularity

    fun getAlbumArt(size: ImageSize = ImageSize.MEDIUM): String? {
        return currentTrack.value?.album?.images?.let { images ->
            when (size) {
                ImageSize.SMALL -> images.findBySize(64)
                ImageSize.MEDIUM -> images.findBySize(300)
                ImageSize.LARGE -> images.findBySize(640)
            }?.url
        }
    }

    fun getSimilarSongName(position: Int): String? = similarTracks.value.getOrNull(position)?.name

    fun getSimilarArtistName(position: Int): String? =
        similarTracks.value.getOrNull(position)?.artists?.firstOrNull()?.name

    fun getSimilarPopularity(position: Int): Int? =
        similarTracks.value.getOrNull(position)?.popularity

    fun getSimilarAlbumArt(position: Int, size: ImageSize = ImageSize.MEDIUM): String? {
        return similarTracks.value.getOrNull(position)?.album?.images?.let { images ->
            when (size) {
                ImageSize.SMALL -> images.findBySize(64)
                ImageSize.MEDIUM -> images.findBySize(300)
                ImageSize.LARGE -> images.findBySize(640)
            }?.url
        }
    }

    fun getAllSimilarTracks(): List<Track> = similarTracks.value

    enum class ImageSize {
        SMALL, MEDIUM, LARGE
    }

    private fun List<Image>.findBySize(targetSize: Int): Image? {
        return minByOrNull { abs(it.width - targetSize) }
    }
    private fun calculateSimilarityScore(original: AudioFeatures, sample: AudioFeatures): Float {
        val weights = mapOf(
            "danceability" to 0.25f,
            "energy" to 0.25f,
            "valence" to 0.20f,
            "tempo" to 0.15f,
            "key" to 0.10f,
            "loudness" to 0.05f
        )

        val distances = mapOf(
            "danceability" to abs(original.danceability - sample.danceability),
            "energy" to abs(original.energy - sample.energy),
            "valence" to abs(original.valence - sample.valence),
            "tempo" to abs(original.tempo - sample.tempo) / 200f,
            "key" to if (original.key == sample.key) 0f else 1f,
            "loudness" to abs(original.loudness - sample.loudness) / 60f
        )

        val weightedDistance = distances.map { (feature, distance) ->
            weights[feature]!! * distance * distance
        }.sum()

        return exp(-3f * weightedDistance).toFloat()
    }
}