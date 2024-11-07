package com.mobileapp.spoofyrez.data.models

data class Track(
    val id: String,
    val name: String,
    val artists: List<Artist>,
    val previewUrl: String?
)