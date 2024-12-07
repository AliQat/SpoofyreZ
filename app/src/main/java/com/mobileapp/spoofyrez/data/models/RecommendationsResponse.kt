package com.mobileapp.spoofyrez.data.models

data class RecommendationsResponse(
    val tracks: List<Track>,
    val seeds: List<RecommendationSeed>
)

data class RecommendationSeed(
    val id: String,
    val type: String,
    val initialPoolSize: Int
)