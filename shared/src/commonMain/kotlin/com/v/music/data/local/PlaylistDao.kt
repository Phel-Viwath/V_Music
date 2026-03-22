package com.v.music.data.local

import androidx.room3.*
import com.v.music.data.entity.PlaylistEntity
import com.v.music.data.entity.PlaylistSongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {

    // Playlist
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun newPlaylist(playlist: PlaylistEntity)

    @Query("SELECT * FROM playlist")
    fun getAllPlayList(): Flow<List<PlaylistEntity>>

    @Delete
    suspend fun deletePlaylist(playlist: PlaylistEntity)

    @Query("SELECT * FROM playlist WHERE playlistId = :playlistId")
    suspend fun getPlaylist(playlistId: Long): PlaylistEntity?

    @Query("UPDATE playlist SET thumbnail = :thumbnail WHERE playlistId = :playlistId")
    suspend fun updateThumbnailUri(thumbnail: String, playlistId: Long): Int

    // playlist song
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addMusicToPlaylist(playlistSong: PlaylistSongEntity)

    @Query("SELECT * FROM playlist_song WHERE playlistId = :playlistId")
    fun getPlaylistSongs(playlistId: Long): Flow<List<PlaylistSongEntity>>

    @Query("DELETE FROM playlist_song WHERE playlistId = :playlistId AND musicId = :musicId")
    suspend fun removeFromPlaylist(playlistId: Long, musicId: String)

    @Query("SELECT * FROM playlist_song")
    fun getAllPlaylistSongs(): Flow<List<PlaylistSongEntity>>

}