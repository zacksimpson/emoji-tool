package com.zacksimpson.emojis

import androidx.lifecycle.viewModelScope
import com.thelightphone.sdk.LightViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Grid is the app's main/default content — not a "Home tab" among equals. */
enum class EmojiMode { Grid, Search, TopUsed, Settings }

private const val COPIED_RESET_DELAY_MS = 2000L

class EmojiToolViewModel : LightViewModel<Unit>() {
    private val _mode = MutableStateFlow(EmojiMode.Grid)
    val mode: StateFlow<EmojiMode> = _mode.asStateFlow()

    private val _copied = MutableStateFlow(false)
    val copied: StateFlow<Boolean> = _copied.asStateFlow()

    fun openSearch() {
        _mode.value = EmojiMode.Search
    }

    fun openTopUsed() {
        _mode.value = EmojiMode.TopUsed
    }

    fun openSettings() {
        _mode.value = EmojiMode.Settings
    }

    fun closeToGrid() {
        _mode.value = EmojiMode.Grid
    }

    fun selectEmoji(emoji: String) {
        SelectionStore.addEmoji(emoji)
    }

    fun clearSelection() {
        SelectionStore.clear()
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
