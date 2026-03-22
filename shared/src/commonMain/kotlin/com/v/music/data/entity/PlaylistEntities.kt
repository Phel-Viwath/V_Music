package com.v.music.data.entity

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.PrimaryKey
import com.v.music.domain.model.Playlist
import com.v.music.domain.model.PlaylistSong

@Entity(tableName = "playlist")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true)
    val playlistId: Long = 0,
    val name: String,
    val createAt: Long,
    val thumbnail: String?
)

@Entity(
    tableName = "playlist_song",
    primaryKeys = ["playlistId", "musicId"],
    foreignKeys = [
        ForeignKey(
            entity = PlaylistEntity::class,
            parentColumns = ["playlistId"],
            childColumns = ["playlistId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PlaylistSongEntity(
    val playlistId: Long,
    val musicId: String,
    val musicUri: String
)


fun PlaylistEntity.toDomain() = Playlist(
    id = playlistId,
    name = name,
    createdAt = createAt,
    thumbnail = thumbnail
)

fun PlaylistSongEntity.toDomain() = PlaylistSong(
    playlistId = playlistId,
    musicId = musicId,
    musicUri = musicUri
)