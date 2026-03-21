package com.v.music.data.local

import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.v.music.data.entity.MusicEntity
import com.v.music.data.entity.PlaylistEntity
import com.v.music.data.entity.PlaylistSongEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

@Database(
    entities = [MusicEntity::class, PlaylistEntity::class, PlaylistSongEntity::class],
    version = 1,
    exportSchema = true,
)
@ConstructedBy(MusicDatabaseConstructor::class)
abstract class MusicDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
    abstract fun playlistDao(): PlaylistDao
}

// Room generates the actual implementations
expect object MusicDatabaseConstructor : RoomDatabaseConstructor<MusicDatabase> {
    override fun initialize(): MusicDatabase
}

fun getMusicDatabase(
    builder: RoomDatabase.Builder<MusicDatabase>,
): MusicDatabase = builder.addMigrations()
    .fallbackToDestructiveMigrationOnDowngrade(true)
    .setDriver(BundledSQLiteDriver())
    .setQueryCoroutineContext(Dispatchers.IO)
    .build()

fun getFavoriteDao(db: MusicDatabase): FavoriteDao = db.favoriteDao()
fun getPlaylistDao(db: MusicDatabase): PlaylistDao = db.playlistDao()