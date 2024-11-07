package com.mobileapp.spoofyrez.data.models

data class AudioFeatures(
    val id: String,
    val danceability: Float,
    val energy: Float,
    val key: Int,
    val loudness: Float,
    val mode: Int,
    val tempo: Float,
    val valence: Float
)