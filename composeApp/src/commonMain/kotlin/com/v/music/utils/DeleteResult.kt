package com.v.music.utils

sealed class DeleteResult {
    data object Success : DeleteResult()
    data class NeedPermission(val platformIntent: Any?) : DeleteResult()
}