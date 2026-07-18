package com.zacksimpson.emojis

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.thelightphone.sdk.ui.designVerticalPxToSp

// Placeholder size — revisit against the RN app's spacing during the Phase 6 visual pass.
private const val EMOJI_FONT_SIZE_PX = 34f

// Matches the LP3 keyboard's own key-press feedback (Lp3Keyboard.kt's Key composable): an
// instant scale flip read at draw time via graphicsLayer, no animation curve.
private const val PRESSED_SCALE = 1.25f

/** Shared by every emoji grid (Grid, Search, Recents) so cells line up identically across screens. */
@Composable
fun rememberEmojiCellSize(columns: Int, horizontalPadding: Dp): Dp {
    val screenWidthDp = LocalConfiguration.current.screenWidthDp.dp
    return (screenWidthDp - horizontalPadding * 2) / columns
}

/**
 * Fixed [cellSize] (rather than fillMaxWidth + aspectRatio) avoids an extra intrinsic-measure
 * pass per cell — aspectRatio was causing visible scroll jank on real LP3 hardware once a
 * LazyVerticalGrid full of color-emoji glyphs got going.
 *
 * Tap handling is a raw pointerInput gesture (mirroring the LP3 keyboard's own `keyInput`
 * modifier), not Modifier.clickable: clickable delays/suppresses its press interaction inside
 * scrollable containers — like the LazyVerticalGrid every emoji cell lives in — to avoid a
 * ripple flash during scroll flings, so a fast enough tap never reported isPressed = true in
 * time for the scale animation to show. Handling the pointer events ourselves reports the press
 * the instant a finger goes down, no delay.
 */
@Composable
fun EmojiCell(
    emoji: String,
    cellSize: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isPressed by remember { mutableStateOf(false) }
    val currentOnClick = rememberUpdatedState(onClick)

    Box(
        modifier = modifier
            .size(cellSize)
            .pointerInput(emoji) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    isPressed = true
                    val up = waitForUpOrCancellation()
                    isPressed = false
                    if (up != null) currentOnClick.value()
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = emoji,
            style = TextStyle(fontSize = EMOJI_FONT_SIZE_PX.designVerticalPxToSp()),
            modifier = Modifier.graphicsLayer {
                val scale = if (isPressed) PRESSED_SCALE else 1f
                scaleX = scale
                scaleY = scale
            },
        )
    }
}
