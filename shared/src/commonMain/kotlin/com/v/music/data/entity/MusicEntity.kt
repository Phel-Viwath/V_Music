package com.v.music.data.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import com.v.music.domain.model.Music

@Entity(tableName = "favorite_music")
data class MusicEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long,
    val duration: Long,
    val imagePath: String?,
    val uri: String,
    val addDate: Long
)

fun MusicEntity.toDomain(): Music = Music(
    id, title, artist, album, albumId,
    duration, imagePath, uri, addDate,
    isFavorite = true
)