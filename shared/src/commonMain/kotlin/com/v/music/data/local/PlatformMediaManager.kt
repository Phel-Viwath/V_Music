package com.v.music.data.local

import com.v.music.domain.model.Album
import com.v.music.domain.model.Music
import com.v.music.utils.MusicDeletionResult

interface PlatformMediaManager {
    suspend fun fetchExternalFiles(): List<Music>
    suspend fun fetchAlbums(): List<Album>
    suspend fun deletePhysicalFile(music: Music): Int
    suspend fun getDeletePermission(music: Music): MusicDeletionResult
    suspend fun clearCache()
}