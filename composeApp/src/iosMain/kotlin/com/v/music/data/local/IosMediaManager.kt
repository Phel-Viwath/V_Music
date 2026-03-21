package com.v.music.data.local

import com.v.music.domain.model.Album
import com.v.music.domain.model.Music
import com.v.music.utils.MusicDeletionResult
import platform.Foundation.timeIntervalSince1970
import platform.MediaPlayer.MPMediaItem
import platform.MediaPlayer.MPMediaItemCollection
import platform.MediaPlayer.MPMediaQuery

class IosMediaManager : PlatformMediaManager {
    // cache
    private var cacheSong: List<Music> = emptyList()
    private var cacheAlbum: List<Album> = emptyList()

    override suspend fun fetchExternalFiles(): List<Music> {
        if (cacheSong.isEmpty()) {
            cacheSong = queryMusicLibrary()
        }
        return cacheSong
    }

    override suspend fun fetchAlbums(): List<Album> {
        if (cacheAlbum.isEmpty()) {
            cacheAlbum = queryAlbums()
        }
        return cacheAlbum
    }

    override suspend fun deletePhysicalFile(music: Music): Int {
        // iOS does not allow deleting music files
        return 0
    }

    override suspend fun getDeletePermission(music: Music): MusicDeletionResult {
        // iOS does not allow deleting music files
        return MusicDeletionResult.Failure("iOS does not support deleting music files")
    }

    override suspend fun clearCache() {
        cacheSong = emptyList()
        cacheAlbum = emptyList()
    }

    private fun queryMusicLibrary(): List<Music> {
        val musicList = mutableListOf<Music>()
        val query = MPMediaQuery.songsQuery()
        val items = query.items ?: return emptyList()

        for (item in items) {
            val mediaItem = item as? MPMediaItem ?: continue
            val uri = mediaItem.assetURL?.absoluteString ?: continue

            musicList.add(
                Music(
                    id = mediaItem.persistentID.toLong(),
                    title = mediaItem.title ?: "Unknown",
                    artist = mediaItem.artist ?: "Unknown",
                    album = mediaItem.albumTitle ?: "Unknown",
                    albumId = mediaItem.albumPersistentID.toLong(),
                    duration = (mediaItem.playbackDuration * 1000).toLong(),
                    imagePath = getArtworkUrl(mediaItem),
                    uri = uri,
                    addDate = mediaItem.dateAdded
                        .timeIntervalSince1970
                        .toLong()
                )
            )
        }
        return musicList
    }

    private fun queryAlbums(): List<Album> {
        val albumList = mutableListOf<Album>()
        val query = MPMediaQuery.albumsQuery()
        val collections = query.collections ?: return emptyList()

        for (collection in collections) {
            val mediaCollection = collection as? MPMediaItemCollection ?: continue
            val representativeItem = mediaCollection.representativeItem ?: continue

            albumList.add(
                Album(
                    albumId = representativeItem.albumPersistentID.toLong(),
                    albumName = representativeItem.albumTitle ?: "Unknown",
                    artist = representativeItem.artist ?: "Unknown",
                    albumArtUri = getArtworkUrl(representativeItem),
                    songCount = mediaCollection.count.toInt()
                )
            )
        }
        return albumList
    }

    private fun getArtworkUrl(item: MPMediaItem): String {
        // MPMediaItem does not provide a direct file path for artwork.
        // We store the persistentID as a string identifier
        // then use a custom image loader to fetch artwork on the UI side
        return "mpMediaItemArtwork://${item.persistentID}"
    }
}