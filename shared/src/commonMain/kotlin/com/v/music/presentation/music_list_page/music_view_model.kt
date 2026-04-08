package com.v.music.presentation.music_list_page

import androidx.lifecycle.viewModelScope
import com.v.music.domain.model.Music
import com.v.music.domain.model.PlaybackStates
import com.v.music.domain.model.SortOrder
import com.v.music.domain.service.MusicPlayer
import com.v.music.domain.service.MusicServiceConnection
import com.v.music.domain.use_case.music_use_case.MusicUseCase
import com.v.music.utils.BaseViewModel
import com.v.music.utils.DeleteResult
import com.v.music.utils.Resources
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MusicListState(
    val music: Resources<List<Music>>? = null,
    val searchText: String = "",
    val sortOrder: SortOrder = SortOrder.TITLE,

    /** Non-null while waiting for the user to grant OS delete permission. */
    val pendingDeleteMusic: Music? = null,

    /** Shown in a snackbar / toast after a delete attempt. */
    val deleteResult: DeleteResult? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
class MusicViewModel(
    private val musicUseCase: MusicUseCase,
    private val connection: MusicServiceConnection
) : BaseViewModel<MusicListState>(), MusicListPageIntent {

    val sortOrder: StateFlow<SortOrder>
        field = MutableStateFlow(SortOrder.TITLE)

    val playBackState: StateFlow<PlaybackStates> = connection.player
        .flatMapLatest { player ->
            player?.playbackStates ?: flowOf(PlaybackStates())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PlaybackStates()
        )

    private val player: MusicPlayer?
        get() = connection.player.value

    init {
        connection.bind()
        loadMusic()
    }

    override fun initialState(): MusicListState = MusicListState()

    override fun onHandleIntent(intent: MusicListPageIntent.Intent) {
        when (intent) {

            // ── Load / order ────────────────────────────────────────────
            is MusicListPageIntent.Intent.OnLoadMusic -> loadMusic()

            is MusicListPageIntent.Intent.GetOrder -> {
                /* already stored in state.sortOrder; UI can read it directly */
            }

            is MusicListPageIntent.Intent.Order -> {
                setState { copy(sortOrder = intent.sortOrder) }
                loadMusic(intent.sortOrder)
            }

            // ── Search ──────────────────────────────────────────────────
            is MusicListPageIntent.Intent.SearchTextChange ->
                setState { copy(searchText = intent.searchText) }

            is MusicListPageIntent.Intent.SearchClick ->
                loadMusic(filterText = state.value.searchText)

            // ── Playback ────────────────────────────────────────────────
            is MusicListPageIntent.Intent.OnPlay -> {
                val musics = intent.musics.ifEmpty {
                    // fall back to the currently loaded list
                    (state.value.music as? Resources.Success)?.data ?: listOf(intent.music)
                }
                player?.setPlaylist(musics)
                player?.play(intent.music)
            }

            is MusicListPageIntent.Intent.OnPause -> player?.pause()

            is MusicListPageIntent.Intent.OnResume -> player?.resume()

            is MusicListPageIntent.Intent.OnPlayNext -> player?.next()

            is MusicListPageIntent.Intent.OnPlayPrevious -> player?.previous()

            is MusicListPageIntent.Intent.OnSeekTo -> player?.seekTo(intent.position)

            is MusicListPageIntent.Intent.OnRepeatOne -> player?.setRepeatOne()

            is MusicListPageIntent.Intent.OnRepeatAll -> player?.setRepeatAll()

            is MusicListPageIntent.Intent.ShuffleMode ->
                player?.setShuffle(intent.isShuffle)

            is MusicListPageIntent.Intent.AddToPlayNext -> {
                // If a playlist was supplied and differs from the current one, replace it first
                if (intent.musics.isNotEmpty()) player?.setPlaylist(intent.musics)
                player?.addToPlayNext(intent.music)
            }

            is MusicListPageIntent.Intent.AddToPlayLast ->
                player?.playLast(intent.music)

            // ── Delete ──────────────────────────────────────────────────
            is MusicListPageIntent.Intent.DeleteMusic -> deleteMusic(intent.music)

            is MusicListPageIntent.Intent.OnDeletePermissionGranted -> {
                val pending = state.value.pendingDeleteMusic ?: return
                executeDeleteAfterPermission(pending)
            }
        }
    }


    // ── Public helpers (called from UI if needed) ──────────────────────────

    fun setOrder(order: SortOrder) = onHandleIntent(MusicListPageIntent.Intent.Order(order))

    // ── Private helpers ────────────────────────────────────────────────────

    private fun loadMusic(
        order: SortOrder = state.value.sortOrder,
        filterText: String = "",
    ) {
        viewModelScope.launch {
            musicUseCase.getMusicsUseCase(order).collect { resource ->
                val filtered = when {
                    filterText.isBlank() || resource !is Resources.Success -> resource
                    else -> Resources.Success(
                        resource.data.filter { music ->
                            music.title.contains(filterText, ignoreCase = true) ||
                                    music.artist.contains(filterText, ignoreCase = true)
                        }
                    )
                }
                setState { copy(music = filtered) }
            }
        }
    }

    private fun deleteMusic(music: Music) {
        viewModelScope.launch {
            musicUseCase.deleteMusicsUseCase(music).collect { resource ->
                when (resource) {
                    is Resources.Loading -> { /* show loader if desired */ }

                    is Resources.Success -> when (val result = resource.data) {
                        is DeleteResult.NeedPermission -> {
                            // Store the music so we can finish deletion once permission returns
                            setState { copy(pendingDeleteMusic = music, deleteResult = result) }
                        }
                        is DeleteResult.Success -> {
                            setState { copy(pendingDeleteMusic = null, deleteResult = result) }
                            loadMusic() // refresh list
                        }
                    }

                    is Resources.Error ->
                        setState { copy(pendingDeleteMusic = null) }
                }
            }
        }
    }

    private fun executeDeleteAfterPermission(music: Music) {
        viewModelScope.launch {
            musicUseCase.deleteMusicsUseCase.executeDelete(music).collect { resource ->
                when (resource) {
                    is Resources.Success -> {
                        setState { copy(pendingDeleteMusic = null, deleteResult = resource.data) }
                        loadMusic()
                    }
                    is Resources.Error ->
                        setState { copy(pendingDeleteMusic = null) }
                    else -> Unit
                }
            }
        }
    }

    // ── Cleanup ────────────────────────────────────────────────────────────

    override fun onCleared() {
        super.onCleared()
        connection.unbind()
    }

}