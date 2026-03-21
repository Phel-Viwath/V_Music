package com.v.music.domain.model

data class Album(
    val albumId: Long,
    val albumName: String,
    val albumArtUri: String? = null,
    val artist: String? = null,
    val songCount: Int? = 0,
)