package com.zacksimpson.emojis

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

enum class SortMode { TopUsed, MostRecent }

private const val MAX_DISPLAY = 24
private const val PREVIEW_DISPLAY = 12

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

    private val _showTopUsedPreview = MutableStateFlow(false)
    val showTopUsedPreview: StateFlow<Boolean> = _showTopUsedPreview.asStateFlow()

    private val _horizontalLayout = MutableStateFlow(false)
    val horizontalLayout: StateFlow<Boolean> = _horizontalLayout.asStateFlow()

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

    // Always ranked by count, independent of sortMode — the Grid screen's "Top Used" preview
    // section means literally that, regardless of how the user has the Recents tab sorted.
    val topUsedPreview: StateFlow<List<String>> = _counts
        .map { counts -> counts.entries.sortedByDescending { it.value }.take(PREVIEW_DISPLAY).map { it.key } }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    private val loaded = CompletableDeferred<Unit>()

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
            prefs[SHOW_TOP_USED_PREVIEW_KEY]?.let { _showTopUsedPreview.value = it }
            prefs[HORIZONTAL_LAYOUT_KEY]?.let { _horizontalLayout.value = it }
            loaded.complete(Unit)
        }
    }

    /** Suspends until the initial DataStore read has populated counts/recency/settings. */
    suspend fun awaitLoaded() = loaded.await()

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

    fun setShowTopUsedPreview(value: Boolean) {
        _showTopUsedPreview.value = value
        scope.launch {
            dataStore.edit { it[SHOW_TOP_USED_PREVIEW_KEY] = value }
        }
    }

    fun setHorizontalLayout(value: Boolean) {
        _horizontalLayout.value = value
        scope.launch {
            dataStore.edit { it[HORIZONTAL_LAYOUT_KEY] = value }
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
        private val SHOW_TOP_USED_PREVIEW_KEY = booleanPreferencesKey("show_top_used_preview")
        private val HORIZONTAL_LAYOUT_KEY = booleanPreferencesKey("horizontal_layout")

        @Volatile
        private var instance: RecentsStore? = null

        fun getInstance(dataStoreProvider: () -> DataStore<Preferences>): RecentsStore =
            instance ?: synchronized(this) {
                instance ?: RecentsStore(dataStoreProvider()).also { instance = it }
            }
    }
}
