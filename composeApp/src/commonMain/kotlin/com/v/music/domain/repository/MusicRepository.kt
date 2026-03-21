package com.v.music.domain.repository

import com.v.music.domain.model.Album
import com.v.music.domain.model.Music
import com.v.music.domain.model.Playlist
import com.v.music.domain.model.PlaylistSong
import com.v.music.utils.MusicDeletionResult
import kotlinx.coroutines.flow.Flow

interface MusicRepository {
    //delete music from storage
    suspend fun deleteMusic(music: Music): Int
    suspend fun getDeletePermissionIntent(music: Music): MusicDeletionResult
    //
    suspend fun getMusicFiles(): List<Music>
    suspend fun getAlbums(): List<Album>

    suspend fun getSongByAlbumId(albumId: Long): List<Music>

    suspend fun clearCache()
    // favorite
    suspend fun addFavorite(music: Music): Long
    suspend fun removeFavorite(music: Music): Int
    suspend fun getFavoriteMusicById(id: Long): Music?
    fun getFavoriteMusic(): Flow<List<Music>>
    fun getFavoriteMusicByDate(): Flow<List<Music>>
    fun getFavoriteMusicByTitle(): Flow<List<Music>>
    fun getFavoriteMusicByDuration():Flow<List<Music>>

    // playlist
    suspend fun newPlayList(playlist: Playlist)
    suspend fun addMusicToPlaylist(playlistSongs: List<PlaylistSong>)
    suspend fun getPlaylist(playlistId: Long): Playlist?

    fun getPlaylists(): Flow<List<Playlist>>
    fun getPlaylistSongs(playlistId: Long): Flow<List<PlaylistSong>>
    fun getAllPlaylistSongs(): Flow<List<PlaylistSong>>

    suspend fun deletePlaylist(playlist: Playlist)
    suspend fun removePlaylistSong(playlistId: Long, musicId: String)

    suspend fun updatePlaylistThumbnail(playlistId: Long, thumbnailUri: String): Int

}