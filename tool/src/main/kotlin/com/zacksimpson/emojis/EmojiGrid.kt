package com.zacksimpson.emojis

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.thelightphone.sdk.ui.LightText
import com.thelightphone.sdk.ui.LightTextVariant
import com.thelightphone.sdk.ui.gridUnitsAsDp

private const val COLUMNS = 6
private const val PADDING_GRID_UNITS = 1f
private const val HORIZONTAL_HEADER_HEIGHT_GRID_UNITS = 3f

@Composable
fun EmojiGrid(
    onEmojiTap: (String) -> Unit,
    showTopUsedPreview: Boolean,
    topUsedPreview: List<String>,
    horizontalLayout: Boolean,
    modifier: Modifier = Modifier,
) {
    if (horizontalLayout) {
        EmojiGridHorizontal(onEmojiTap, showTopUsedPreview, topUsedPreview, modifier)
    } else {
        EmojiGridVertical(onEmojiTap, showTopUsedPreview, topUsedPreview, modifier)
    }
}

@Composable
private fun EmojiGridVertical(
    onEmojiTap: (String) -> Unit,
    showTopUsedPreview: Boolean,
    topUsedPreview: List<String>,
    modifier: Modifier,
) {
    val padding = PADDING_GRID_UNITS.gridUnitsAsDp()
    val cellSize = rememberEmojiCellSize(COLUMNS, padding)

    LazyVerticalGrid(
        columns = GridCells.Fixed(COLUMNS),
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = padding),
    ) {
        if (showTopUsedPreview && topUsedPreview.isNotEmpty()) {
            item(key = "header-top-used", span = { GridItemSpan(maxLineSpan) }) {
                SectionHeaderRow("Top Used")
            }
            itemsIndexed(
                items = topUsedPreview,
                key = { index, _ -> "top-used-$index" },
            ) { _, emoji ->
                EmojiCell(emoji = emoji, cellSize = cellSize, onClick = { onEmojiTap(emoji) })
            }
        }
        EMOJI_CATEGORIES.forEach { category ->
            item(
                key = "header-${category.label}",
                span = { GridItemSpan(maxLineSpan) },
            ) {
                SectionHeaderRow(category.label)
            }
            itemsIndexed(
                items = category.emojis,
                key = { index, _ -> "${category.label}-$index" },
            ) { _, emoji ->
                EmojiCell(emoji = emoji, cellSize = cellSize, onClick = { onEmojiTap(emoji) })
            }
        }
    }
}

/**
 * UX experiment: same category ordering as the vertical grid, just laid out as columns you swipe
 * through left-to-right instead of rows you scroll up-through. Each category is one LazyRow item
 * — a label row above that category's own block of columns, scrolling normally with the rest of
 * the content (no sticky/pinned behavior). Cell size matches the vertical grid's exactly (same
 * COLUMNS divisor against screen width) so spacing between emoji looks identical either way; the
 * row count is instead whatever fits the container's actual measured height (via
 * BoxWithConstraints), since forcing a fixed row count here — the way COLUMNS does for the
 * vertical grid's fixed screen width — was shrinking cells and eating into that spacing.
 */
@Composable
private fun EmojiGridHorizontal(
    onEmojiTap: (String) -> Unit,
    showTopUsedPreview: Boolean,
    topUsedPreview: List<String>,
    modifier: Modifier,
) {
    val padding = PADDING_GRID_UNITS.gridUnitsAsDp()
    val headerHeight = HORIZONTAL_HEADER_HEIGHT_GRID_UNITS.gridUnitsAsDp()
    val cellSize = rememberEmojiCellSize(COLUMNS, padding)

    BoxWithConstraints(modifier = modifier) {
        val availableHeight = maxHeight - padding * 2 - headerHeight
        val rows = (availableHeight / cellSize).toInt().coerceAtLeast(1)

        LazyRow(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = padding, vertical = padding),
        ) {
            if (showTopUsedPreview && topUsedPreview.isNotEmpty()) {
                item(key = "top-used") {
                    CategoryColumnBlock(
                        label = "Top Used",
                        emojis = topUsedPreview,
                        cellSize = cellSize,
                        rows = rows,
                        headerHeight = headerHeight,
                        onEmojiTap = onEmojiTap,
                    )
                }
            }
            items(EMOJI_CATEGORIES, key = { it.label }) { category ->
                CategoryColumnBlock(
                    label = category.label,
                    emojis = category.emojis,
                    cellSize = cellSize,
                    rows = rows,
                    headerHeight = headerHeight,
                    onEmojiTap = onEmojiTap,
                )
            }
        }
    }
}

@Composable
private fun CategoryColumnBlock(
    label: String,
    emojis: List<String>,
    cellSize: Dp,
    rows: Int,
    headerHeight: Dp,
    onEmojiTap: (String) -> Unit,
) {
    Column {
        Box(
            modifier = Modifier.height(headerHeight),
            contentAlignment = Alignment.CenterStart,
        ) {
            LightText(text = label, variant = LightTextVariant.Detail, maxLines = 1)
        }
        Row {
            emojis.chunked(rows).forEach { columnEmojis ->
                Column {
                    columnEmojis.forEach { emoji ->
                        EmojiCell(emoji = emoji, cellSize = cellSize, onClick = { onEmojiTap(emoji) })
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeaderRow(label: String) {
    LightText(
        text = label,
        variant = LightTextVariant.Detail,
        modifier = Modifier.padding(
            top = 1.5f.gridUnitsAsDp(),
            bottom = 0.5f.gridUnitsAsDp(),
        ),
    )
}
