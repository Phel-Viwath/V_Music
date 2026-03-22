package com.v.music.domain.model

import com.v.music.data.entity.PlaylistEntity
import com.v.music.data.entity.PlaylistSongEntity

data class Playlist(
    val id: Long,
    val name: String,
    val createdAt: Long,
    val thumbnail: String?
)

data class PlaylistSong(
    val playlistId: Long,
    val musicId: String,
    val musicUri: String
)

fun Playlist.toEntity() = PlaylistEntity(
    playlistId = id,
    name = name,
    createAt = createdAt,
    thumbnail = thumbnail
)

fun PlaylistSong.toEntity() = PlaylistSongEntity(
    playlistId = playlistId,
    musicId = musicId,
    musicUri = musicUri
)