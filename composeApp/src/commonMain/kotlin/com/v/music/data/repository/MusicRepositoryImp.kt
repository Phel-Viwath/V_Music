package com.v.music.data.repository

import com.v.music.data.entity.toDomain
import com.v.music.data.local.FavoriteDao
import com.v.music.data.local.PlatformMediaManager
import com.v.music.data.local.PlaylistDao
import com.v.music.domain.model.*
import com.v.music.domain.repository.MusicRepository
import com.v.music.utils.MusicDeletionResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MusicRepositoryImp(
    private val platformMediaManager: PlatformMediaManager,
    private val playlistDao: PlaylistDao,
    private val favoriteDao: FavoriteDao,
) : MusicRepository {

    private var cacheAlbumSong: Set<Pair<Long, List<Music>>> = emptySet()

    override suspend fun deleteMusic(music: Music): Int {
        return platformMediaManager.deletePhysicalFile(music)
    }

    override suspend fun getDeletePermissionIntent(music: Music): MusicDeletionResult {
        return platformMediaManager.getDeletePermission(music)
    }

    override suspend fun getMusicFiles(): List<Music> {
        return platformMediaManager.fetchExternalFiles()
    }

    override suspend fun getAlbums(): List<Album> {
        return platformMediaManager.fetchAlbums()
    }

    override suspend fun getSongByAlbumId(albumId: Long): List<Music> {
        if (!cacheAlbumSong.any { it.first == albumId }) {
            val musicList = getMusicFiles().filter { it.albumId == albumId }
            cacheAlbumSong += albumId to musicList
        }
        return cacheAlbumSong.find { it.first == albumId }?.second ?: emptyList()
    }

    override suspend fun clearCache() {
        platformMediaManager.clearCache()
        cacheAlbumSong = emptySet()
    }

    // ─── Favorite ─────────────────────────────────────────────────────────────

    override suspend fun addFavorite(music: Music): Long {
        return favoriteDao.addFavorite(music.toEntity())
    }

    override suspend fun removeFavorite(music: Music): Int {
        return favoriteDao.removeFavorite(music.toEntity())
    }

    override suspend fun getFavoriteMusicById(id: Long): Music? {
        return favoriteDao.getFavoriteMusicById(id)?.toDomain()
    }

    override fun getFavoriteMusic(): Flow<List<Music>> {
        return favoriteDao.getFavoriteMusic().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getFavoriteMusicByDate(): Flow<List<Music>> {
        return favoriteDao.getFavoriteMusicByDate().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getFavoriteMusicByTitle(): Flow<List<Music>> {
        return favoriteDao.getFavoriteMusicByTitle().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getFavoriteMusicByDuration(): Flow<List<Music>> {
        return favoriteDao.getFavoriteMusicByDuration().map { list ->
            list.map { it.toDomain() }
        }
    }

    // ─── Playlist ─────────────────────────────────────────────────────────────

    override suspend fun newPlayList(playlist: Playlist) {
        playlistDao.newPlaylist(playlist.toEntity())
    }

    override suspend fun addMusicToPlaylist(playlistSongs: List<PlaylistSong>) {
        playlistSongs.forEach { song ->
            playlistDao.addMusicToPlaylist(song.toEntity())
        }
    }

    override suspend fun getPlaylist(playlistId: Long): Playlist? {
        return playlistDao.getPlaylist(playlistId)?.toDomain()
    }

    override fun getPlaylists(): Flow<List<Playlist>> {
        return playlistDao.getAllPlayList().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getPlaylistSongs(playlistId: Long): Flow<List<PlaylistSong>> {
        return playlistDao.getPlaylistSongs(playlistId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getAllPlaylistSongs(): Flow<List<PlaylistSong>> {
        return playlistDao.getAllPlaylistSongs().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun deletePlaylist(playlist: Playlist) {
        playlistDao.deletePlaylist(playlist.toEntity())
    }

    override suspend fun removePlaylistSong(playlistId: Long, musicId: String) {
        playlistDao.removeFromPlaylist(playlistId, musicId)
    }

    override suspend fun updatePlaylistThumbnail(playlistId: Long, thumbnailUri: String): Int {
        return playlistDao.updateThumbnailUri(thumbnailUri, playlistId)
    }

}