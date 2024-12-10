package com.mobileapp.spoofyrez.data.models

data class Track(
    val id: String,
    val name: String,
    val artists: List<Artist>,
    val previewUrl: String?,
    val popularity: Int,
    val album: Album
)

data class Album(
    val id: String,
    val name: String,
    val images: List<Image>
)

data class Image(
    val url: String,
    val height: Int,
    val width: Int
)