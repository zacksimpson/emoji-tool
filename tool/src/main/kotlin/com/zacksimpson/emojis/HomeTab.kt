package com.zacksimpson.emojis

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.thelightphone.sdk.ui.LightText
import com.thelightphone.sdk.ui.LightTextVariant
import com.thelightphone.sdk.ui.gridUnitsAsDp

private const val COLUMNS = 6

@Composable
fun HomeTab(
    onEmojiTap: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(COLUMNS),
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 1f.gridUnitsAsDp()),
    ) {
        EMOJI_CATEGORIES.forEach { category ->
            item(span = { GridItemSpan(maxLineSpan) }) {
                LightText(
                    text = category.label,
                    variant = LightTextVariant.Detail,
                    lighten = true,
                    modifier = Modifier.padding(
                        top = 1.5f.gridUnitsAsDp(),
                        bottom = 0.5f.gridUnitsAsDp(),
                    ),
                )
            }
            items(category.emojis) { emoji ->
                EmojiCell(emoji = emoji, onClick = { onEmojiTap(emoji) })
            }
        }
    }
}
