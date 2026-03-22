package com.v.music.domain.model

import androidx.compose.runtime.Immutable
import com.v.music.data.entity.MusicEntity

@Immutable
data class Music(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long,
    val duration: Long,
    val imagePath: String?,
    val uri: String,
    val addDate: Long,
    val isFavorite: Boolean = false
)

fun Music.toEntity(): MusicEntity = MusicEntity(
    id, title, artist, album, albumId,
    duration, imagePath, uri, addDate
)