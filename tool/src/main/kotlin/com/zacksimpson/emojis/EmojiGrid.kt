package com.zacksimpson.emojis

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.thelightphone.sdk.ui.LightText
import com.thelightphone.sdk.ui.LightTextVariant
import com.thelightphone.sdk.ui.gridUnitsAsDp

private const val COLUMNS = 6
private const val HORIZONTAL_PADDING_GRID_UNITS = 1f

@Composable
fun EmojiGrid(
    onEmojiTap: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val horizontalPadding = HORIZONTAL_PADDING_GRID_UNITS.gridUnitsAsDp()
    val screenWidthDp = LocalConfiguration.current.screenWidthDp.dp
    val cellSize = (screenWidthDp - horizontalPadding * 2) / COLUMNS

    LazyVerticalGrid(
        columns = GridCells.Fixed(COLUMNS),
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = horizontalPadding),
    ) {
        EMOJI_CATEGORIES.forEach { category ->
            item(
                key = "header-${category.label}",
                span = { GridItemSpan(maxLineSpan) },
            ) {
                LightText(
                    text = category.label,
                    variant = LightTextVariant.Detail,
                    modifier = Modifier.padding(
                        top = 1.5f.gridUnitsAsDp(),
                        bottom = 0.5f.gridUnitsAsDp(),
                    ),
                )
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
