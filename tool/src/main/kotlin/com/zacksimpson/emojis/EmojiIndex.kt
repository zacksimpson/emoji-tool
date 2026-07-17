package com.zacksimpson.emojis

data class EmojiEntry(val emoji: String, val name: String, val keywords: List<String>)

// Auto-generated from the RN app's utils/emojiData.ts via scripts/convert-emoji-index.js — do not hand-edit.
// 1816 emoji with names and keywords, assembled from EmojiIndexPart*.kt
val EMOJI_INDEX: List<EmojiEntry> =
    EMOJI_INDEX_PART_0 +
    EMOJI_INDEX_PART_1 +
    EMOJI_INDEX_PART_2 +
    EMOJI_INDEX_PART_3 +
    EMOJI_INDEX_PART_4 +
    EMOJI_INDEX_PART_5 +
    EMOJI_INDEX_PART_6 +
    EMOJI_INDEX_PART_7 +
    EMOJI_INDEX_PART_8 +
    EMOJI_INDEX_PART_9 +
    EMOJI_INDEX_PART_10 +
    EMOJI_INDEX_PART_11 +
    EMOJI_INDEX_PART_12

fun searchEmoji(query: String): List<EmojiEntry> {
    val q = query.trim().lowercase()
    if (q.isEmpty()) return emptyList()
    return EMOJI_INDEX.filter { e ->
        e.name.lowercase().contains(q) || e.keywords.any { it.lowercase().contains(q) }
    }
}
