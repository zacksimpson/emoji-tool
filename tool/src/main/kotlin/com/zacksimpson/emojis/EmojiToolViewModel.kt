package com.zacksimpson.emojis

import androidx.lifecycle.viewModelScope
import com.thelightphone.sdk.LightViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Grid is the app's main/default content — not a "Home tab" among equals. */
enum class EmojiMode { Grid, Search, Recents, Settings }

private const val COPIED_RESET_DELAY_MS = 2000L

class EmojiToolViewModel(private val recentsStore: RecentsStore) : LightViewModel<Unit>() {
    private val _mode = MutableStateFlow(EmojiMode.Grid)
    val mode: StateFlow<EmojiMode> = _mode.asStateFlow()

    private val _copied = MutableStateFlow(false)
    val copied: StateFlow<Boolean> = _copied.asStateFlow()

    val recents: StateFlow<List<String>> = recentsStore.recents
    val sortMode: StateFlow<SortMode> = recentsStore.sortMode
    val showTopUsedPreview: StateFlow<Boolean> = recentsStore.showTopUsedPreview

    // A snapshot, not a live view of RecentsStore — refreshed only when arriving at Grid
    // (including cold start), not on every tap while already sitting there. Gives it a
    // different, slower-changing character than the always-fresh Recents tab.
    private val _topUsedPreview = MutableStateFlow<List<String>>(emptyList())
    val topUsedPreview: StateFlow<List<String>> = _topUsedPreview.asStateFlow()

    init {
        viewModelScope.launch {
            recentsStore.awaitLoaded()
            _topUsedPreview.value = recentsStore.topUsedPreview.value
        }
    }

    fun openSearch() {
        _mode.value = EmojiMode.Search
    }

    fun openRecents() {
        _mode.value = EmojiMode.Recents
    }

    fun openSettings() {
        _mode.value = EmojiMode.Settings
    }

    fun closeToGrid() {
        _topUsedPreview.value = recentsStore.topUsedPreview.value
        _mode.value = EmojiMode.Grid
    }

    fun selectEmoji(emoji: String) {
        SelectionStore.addEmoji(emoji)
        recentsStore.track(emoji)
    }

    fun clearSelection() {
        SelectionStore.clear()
    }

    fun resetRecents() {
        recentsStore.reset()
    }

    fun setSortMode(mode: SortMode) {
        recentsStore.setSortMode(mode)
    }

    fun setShowTopUsedPreview(value: Boolean) {
        recentsStore.setShowTopUsedPreview(value)
    }

    fun copySelection() {
        if (SelectionStore.selected.value.isEmpty()) return
        // TODO(clipboard): the Light SDK has no clipboard API yet (no LightServiceMethod, and
        // getSystemService()/Context access are blocked by the plugin's lint rules). Light's
        // internal team is already working on it — wire the real call in here once it ships,
        // following the shape of LightServiceMethod.SetRingtone.
        _copied.value = true
        viewModelScope.launch {
            delay(COPIED_RESET_DELAY_MS)
            _copied.value = false
        }
    }
}
