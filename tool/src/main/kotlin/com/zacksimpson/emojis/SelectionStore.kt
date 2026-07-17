package com.zacksimpson.emojis

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * In-memory only, matching the RN app's SelectedContext (never persisted across process restarts).
 * Lives outside any ViewModel because LightScreen gives every screen instance a fresh ViewModelStore,
 * but selection needs to survive switching between tabs.
 */
object SelectionStore {
    private val _selected = MutableStateFlow<List<String>>(emptyList())
    val selected: StateFlow<List<String>> = _selected.asStateFlow()

    fun addEmoji(emoji: String) {
        _selected.value = _selected.value + emoji
    }

    fun clear() {
        _selected.value = emptyList()
    }
}
