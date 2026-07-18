package com.zacksimpson.emojis

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

enum class SortMode { TopUsed, MostRecent }

private const val MAX_DISPLAY = 24

@Serializable
private data class RecentsData(
    val counts: Map<String, Int> = emptyMap(),
    val recency: List<String> = emptyList(),
)

private val SortMode.storageValue: String
    get() = when (this) {
        SortMode.TopUsed -> "top-used"
        SortMode.MostRecent -> "most-recent"
    }

/**
 * Port of the RN app's TopUsedContext: usage counts + a most-recent-first list, both persisted,
 * with the displayed list derived from whichever the user picked in Settings. A singleton (not a
 * ViewModel) because it needs to survive EmojiToolScreen's own ViewModel being torn down and
 * recreated, exactly like SelectionStore — but unlike SelectionStore this one needs the SDK's
 * DataStore, which is only reachable from a LightScreen, hence the authenticator-style
 * getInstance(provider) lazy-init pattern.
 */
class RecentsStore private constructor(private val dataStore: DataStore<Preferences>) {
    private val json = Json { ignoreUnknownKeys = true }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _counts = MutableStateFlow<Map<String, Int>>(emptyMap())
    private val _recency = MutableStateFlow<List<String>>(emptyList())
    private val _sortMode = MutableStateFlow(SortMode.TopUsed)
    val sortMode: StateFlow<SortMode> = _sortMode.asStateFlow()

    val recents: StateFlow<List<String>> =
        combine(_counts, _recency, _sortMode) { counts, recency, mode ->
            when (mode) {
                SortMode.TopUsed -> counts.entries
                    .sortedByDescending { it.value }
                    .take(MAX_DISPLAY)
                    .map { it.key }
                SortMode.MostRecent -> recency.take(MAX_DISPLAY)
            }
        }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    init {
        scope.launch {
            val prefs = dataStore.data.first()
            prefs[RECENTS_DATA_KEY]?.let { raw ->
                runCatching { json.decodeFromString<RecentsData>(raw) }.getOrNull()?.let {
                    _counts.value = it.counts
                    _recency.value = it.recency
                }
            }
            prefs[SORT_MODE_KEY]?.let { raw ->
                SortMode.entries.find { mode -> mode.storageValue == raw }?.let { _sortMode.value = it }
            }
        }
    }

    fun track(emoji: String) {
        val newCounts = _counts.value + (emoji to (_counts.value[emoji] ?: 0) + 1)
        val newRecency = listOf(emoji) + _recency.value.filter { it != emoji }
        _counts.value = newCounts
        _recency.value = newRecency
        persist(newCounts, newRecency)
    }

    fun reset() {
        _counts.value = emptyMap()
        _recency.value = emptyList()
        persist(emptyMap(), emptyList())
    }

    fun setSortMode(mode: SortMode) {
        _sortMode.value = mode
        scope.launch {
            dataStore.edit { it[SORT_MODE_KEY] = mode.storageValue }
        }
    }

    private fun persist(counts: Map<String, Int>, recency: List<String>) {
        scope.launch {
            dataStore.edit { it[RECENTS_DATA_KEY] = json.encodeToString(RecentsData(counts, recency)) }
        }
    }

    companion object {
        private val RECENTS_DATA_KEY = stringPreferencesKey("recents_data")
        private val SORT_MODE_KEY = stringPreferencesKey("recents_sort_mode")

        @Volatile
        private var instance: RecentsStore? = null

        fun getInstance(dataStoreProvider: () -> DataStore<Preferences>): RecentsStore =
            instance ?: synchronized(this) {
                instance ?: RecentsStore(dataStoreProvider()).also { instance = it }
            }
    }
}
