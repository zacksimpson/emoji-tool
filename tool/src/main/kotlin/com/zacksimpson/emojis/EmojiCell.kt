package com.zacksimpson.emojis

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import com.thelightphone.sdk.ui.designVerticalPxToSp
import com.thelightphone.sdk.ui.lightClickable

// Placeholder size — revisit against the RN app's spacing during the Phase 6 visual pass.
private const val EMOJI_FONT_SIZE_PX = 34f

/**
 * Fixed [cellSize] (rather than fillMaxWidth + aspectRatio) avoids an extra intrinsic-measure
 * pass per cell — aspectRatio was causing visible scroll jank on real LP3 hardware once a
 * LazyVerticalGrid full of color-emoji glyphs got going.
 */
@Composable
fun EmojiCell(
    emoji: String,
    cellSize: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(cellSize)
            .lightClickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = emoji,
            style = TextStyle(fontSize = EMOJI_FONT_SIZE_PX.designVerticalPxToSp()),
        )
    }
}
