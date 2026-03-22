package com.v.music.domain.use_case.music_use_case

import com.v.music.domain.model.Music
import com.v.music.domain.model.SortOrder
import com.v.music.domain.repository.MusicRepository
import com.v.music.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class GetMusicUseCase (
    private val repository: MusicRepository
) {
    operator fun invoke(sortOrder: SortOrder): Flow<Resource<List<Music>>> = flow {
        emit(Resource.Loading())
        try {
            val musicFiles = repository.getMusicFiles()
            val favoriteList = repository.getFavoriteMusic().first()
            val favoriteIds = favoriteList.map { it.id }.toSet()

            val mergedList = musicFiles.map { music ->
                music.copy(isFavorite = music.id in favoriteIds)
            }

            val sorted = when (sortOrder) {
                SortOrder.TITLE -> mergedList.sortedBy { it.title }
                SortOrder.DURATION -> mergedList.sortedBy { it.duration }
                SortOrder.DATE -> mergedList.sortedBy { it.addDate }
            }

            emit(Resource.Success(sorted))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Unknown error"))
        }
    }.flowOn(Dispatchers.IO)
}