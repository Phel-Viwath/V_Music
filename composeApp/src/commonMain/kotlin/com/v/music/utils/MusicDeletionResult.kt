package com.v.music.utils

sealed class MusicDeletionResult {
    data object Success : MusicDeletionResult()
    data class RequiresPermission(val platformIntent: Any?) : MusicDeletionResult()
    data class Failure(val error: String) : MusicDeletionResult()
}