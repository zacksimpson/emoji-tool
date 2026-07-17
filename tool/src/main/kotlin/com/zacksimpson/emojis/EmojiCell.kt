package com.zacksimpson.emojis

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import com.thelightphone.sdk.ui.designVerticalPxToSp
import com.thelightphone.sdk.ui.lightClickable

// Placeholder size — revisit against the RN app's spacing during the Phase 6 visual pass.
private const val EMOJI_FONT_SIZE_PX = 34f

@Composable
fun EmojiCell(
    emoji: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .lightClickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = emoji,
            style = TextStyle(fontSize = EMOJI_FONT_SIZE_PX.designVerticalPxToSp()),
        )
    }
}
