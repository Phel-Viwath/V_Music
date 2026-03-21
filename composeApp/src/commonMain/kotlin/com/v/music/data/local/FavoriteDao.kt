package com.v.music.data.local

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy.Companion.REPLACE
import androidx.room3.Query
import com.v.music.data.entity.MusicEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Insert(onConflict = REPLACE)
    suspend fun addFavorite(music: MusicEntity): Long

    @Delete
    suspend fun removeFavorite(music: MusicEntity): Int

    @Query("SELECT * FROM favorite_music")
    fun getFavoriteMusic(): Flow<List<MusicEntity>>

    @Query("SELECT * FROM favorite_music WHERE id = :id")
    fun getFavoriteMusicById(id: Long): MusicEntity?

    @Query("SELECT * FROM favorite_music ORDER BY addDate DESC")
    fun getFavoriteMusicByDate(): Flow<List<MusicEntity>>

    @Query("SELECT * FROM favorite_music ORDER BY title DESC")
    fun getFavoriteMusicByTitle(): Flow<List<MusicEntity>>

    @Query("SELECT * FROM favorite_music ORDER BY duration DESC")
    fun getFavoriteMusicByDuration(): Flow<List<MusicEntity>>
}