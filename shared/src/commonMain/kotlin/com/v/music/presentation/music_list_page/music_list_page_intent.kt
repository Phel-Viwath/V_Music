package com.v.music.presentation.music_list_page

import com.v.music.domain.model.Music
import com.v.music.domain.model.SortOrder

interface MusicListPageIntent {

    fun onHandleIntent(intent: Intent)

    sealed class Intent {
        // ── Sorting / Search ──────────────────────────────────────────────
        data class Order(val sortOrder: SortOrder) : Intent()
        data class SearchTextChange(val searchText: String) : Intent()
        data object SearchClick : Intent()

        // ── Load ──────────────────────────────────────────────────────────
        data object OnLoadMusic : Intent()
        data object GetOrder : Intent()

        // ── Playback controls ─────────────────────────────────────────────
        data object OnPlayNext : Intent()
        data object OnPlayPrevious : Intent()
        data object OnPause : Intent()
        data object OnResume : Intent()
        data object OnRepeatOne : Intent()
        data object OnRepeatAll : Intent()

        data class ShuffleMode(val isShuffle: Boolean) : Intent()
        data class OnSeekTo(val position: Long) : Intent()

        /** Play [music] and (optionally) replace the queue with [musics]. */
        data class OnPlay(val music: Music, val musics: List<Music> = emptyList()) : Intent()

        data class AddToPlayNext(val music: Music, val musics: List<Music> = emptyList()) : Intent()
        data class AddToPlayLast(val music: Music) : Intent()

        // ── Delete ────────────────────────────────────────────────────────
        data class DeleteMusic(val music: Music) : Intent()

        /**
         * Called after the OS permission dialog returns successfully
         * (Android R+ only). The VM remembers the pending music internally.
         */
        data object OnDeletePermissionGranted : Intent()
    }
}