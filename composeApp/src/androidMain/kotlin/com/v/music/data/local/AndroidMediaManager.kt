package com.v.music.data.local

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.os.Build
import android.provider.MediaStore
import androidx.core.net.toUri
import com.v.music.domain.model.Album
import com.v.music.domain.model.Music
import com.v.music.utils.MusicDeletionResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidMediaManager(
    private val context: Context,
) : PlatformMediaManager {

    private var cacheSong: List<Music> = emptyList()
    private var cacheAlbum: List<Album> = emptyList()

    override suspend fun fetchExternalFiles(): List<Music> {
        if (cacheSong.isEmpty()) {
            cacheSong = queryMediaStore()
        }
        return cacheSong
    }

    override suspend fun fetchAlbums(): List<Album> {
        if (cacheAlbum.isEmpty()) {
            cacheAlbum = getAlbumsList()
        }
        return cacheAlbum
    }

    override suspend fun deletePhysicalFile(music: Music): Int {
        return withContext(Dispatchers.IO) {
            val uri = ContentUris.withAppendedId(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                music.id
            )
            context.contentResolver.delete(uri, null, null)
        }
    }

    override suspend fun getDeletePermission(music: Music): MusicDeletionResult {
        return withContext(Dispatchers.IO) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                try {
                    val uri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        music.id
                    )
                    val pendingIntent = MediaStore.createDeleteRequest(
                        context.contentResolver,
                        listOf(uri)
                    )
                    MusicDeletionResult.RequiresPermission(pendingIntent.intentSender)
                } catch (e: Exception) {
                    MusicDeletionResult.Failure(e.message ?: "Unknown error")
                }
            } else {
                val deleted = deletePhysicalFile(music)
                if (deleted > 0) MusicDeletionResult.Success
                else MusicDeletionResult.Failure("Failed to delete file")
            }
        }
    }

    override suspend fun clearCache() {
        cacheSong = emptyList()
        cacheAlbum = emptyList()
    }

    private suspend fun queryMediaStore(): List<Music> = withContext(Dispatchers.IO) {
        val musicList = mutableListOf<Music>()

        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        else
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        context.contentResolver.query(
            collection,
            musicProjection,
            selection,
            null,
            sortOrder
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val id = cursor.getLong(cursor.idCol())
                val albumId = cursor.getLong(cursor.albumIdCol())
                val data = cursor.getString(cursor.dataCol())
                val albumArtUri = ContentUris.withAppendedId(
                    "content://media/external/audio/albumart".toUri(),
                    albumId
                ).toString()

                musicList.add(
                    Music(
                        id = id,
                        title = cursor.getString(cursor.titleCol()) ?: "Unknown",
                        artist = cursor.getString(cursor.artistCol()) ?: "Unknown",
                        album = cursor.getString(cursor.albumCol()) ?: "Unknown",
                        albumId = albumId,
                        duration = cursor.getLong(cursor.durationCol()),
                        imagePath = albumArtUri,
                        uri = data,
                        addDate = cursor.getLong(cursor.dateCol())
                    )
                )
            }
        }
        return@withContext musicList
    }

    private suspend fun getAlbumsList(): List<Album> = withContext(Dispatchers.IO) {
        val albums = mutableListOf<Album>()
        val albumSongCountMap = mutableMapOf<Long, Int>()

        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        else
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"

        // count songs per album
        context.contentResolver.query(
            collection,
            arrayOf(MediaStore.Audio.Media.ALBUM_ID),
            selection,
            null,
            null
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val albumId = cursor.getLong(cursor.albumIdCol())
                albumSongCountMap[albumId] = (albumSongCountMap[albumId] ?: 0) + 1
            }
        }

        // get album details
        context.contentResolver.query(
            collection,
            arrayOf(
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ARTIST
            ),
            selection,
            null,
            "${MediaStore.Audio.Media.ALBUM} ASC"
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val albumId = cursor.getLong(cursor.albumIdCol())
                val albumArtUri = ContentUris.withAppendedId(
                    "content://media/external/audio/albumart".toUri(),
                    albumId
                ).toString()

                albums.add(
                    Album(
                        albumId = albumId,
                        albumName = cursor.getString(cursor.albumCol()) ?: "Unknown",
                        artist = cursor.getString(cursor.artistCol()) ?: "Unknown",
                        albumArtUri = albumArtUri,
                        songCount = albumSongCountMap[albumId] ?: 0
                    )
                )
            }
        }

        return@withContext albums.distinctBy { it.albumId }
    }

    // Cursor extension functions
    private fun Cursor.idCol() = getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
    private fun Cursor.titleCol() = getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
    private fun Cursor.artistCol() = getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
    private fun Cursor.albumCol() = getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
    private fun Cursor.albumIdCol() = getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
    private fun Cursor.dataCol() = getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
    private fun Cursor.durationCol() = getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
    private fun Cursor.dateCol() = getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)

    companion object {
        private val musicProjection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DATE_ADDED
        )
    }


}