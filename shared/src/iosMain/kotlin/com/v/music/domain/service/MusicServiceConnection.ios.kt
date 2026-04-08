package com.v.music.domain.service

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class MusicServiceConnection {

    private val _player = MutableStateFlow<MusicPlayer?>(IosMusicPlayer())

    actual val player: StateFlow<MusicPlayer?>
        get() = _player

    actual fun bind() {}

    actual fun unbind() {
        player.value?.stop()
    }
}