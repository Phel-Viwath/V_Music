package com.v.music.domain.model

enum class SortOrder {
    TITLE,
    DURATION,
    DATE;

    fun displayName() : String = name.lowercase().replaceFirstChar { it.uppercase() }
}