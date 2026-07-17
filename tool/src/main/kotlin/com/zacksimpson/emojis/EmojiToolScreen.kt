package com.zacksimpson.emojis

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.thelightphone.sdk.InitialScreen
import com.thelightphone.sdk.LightScreen
import com.thelightphone.sdk.SealedLightActivity
import com.thelightphone.sdk.ui.LightBarButton
import com.thelightphone.sdk.ui.LightBottomBar
import com.thelightphone.sdk.ui.LightIcons
import com.thelightphone.sdk.ui.LightText
import com.thelightphone.sdk.ui.LightTextVariant
import com.thelightphone.sdk.ui.LightTheme
import com.thelightphone.sdk.ui.LightThemeController
import com.thelightphone.sdk.ui.LightThemeTokens
import com.thelightphone.sdk.ui.LightTopBar
import com.thelightphone.sdk.ui.LightTopBarCenter
import com.thelightphone.sdk.ui.gridUnitsAsDp
import com.thelightphone.sdk.ui.lightClickable

private const val HEADER_HEIGHT_GRID_UNITS = 3f
private const val HEADER_HORIZONTAL_PADDING_GRID_UNITS = 1f

@InitialScreen
class EmojiToolScreen(sealedActivity: SealedLightActivity) :
    LightScreen<Unit, EmojiToolViewModel>(sealedActivity) {

    override val viewModelClass: Class<EmojiToolViewModel>
        get() = EmojiToolViewModel::class.java

    override fun createViewModel(): EmojiToolViewModel = EmojiToolViewModel()

    @Composable
    override fun Content() {
        val themeColors by LightThemeController.colors.collectAsState()
        val mode by viewModel.mode.collectAsState()
        val selected by SelectionStore.selected.collectAsState()
        val copied by viewModel.copied.collectAsState()

        LightTheme(colors = themeColors) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(LightThemeTokens.colors.background),
            ) {
                when (mode) {
                    EmojiMode.Grid -> GridModeContent(
                        selected = selected,
                        copied = copied,
                        onCopy = viewModel::copySelection,
                        onClear = viewModel::clearSelection,
                        onEmojiTap = viewModel::selectEmoji,
                        onOpenTopUsed = viewModel::openTopUsed,
                        onOpenSearch = viewModel::openSearch,
                        onOpenSettings = viewModel::openSettings,
                    )

                    EmojiMode.Search -> EmojiSearchContent(
                        onBack = viewModel::closeToGrid,
                        onEmojiTap = viewModel::selectEmoji,
                    )

                    EmojiMode.TopUsed -> TopUsedModeContent(
                        selected = selected,
                        copied = copied,
                        onCopy = viewModel::copySelection,
                        onClear = viewModel::clearSelection,
                        onBack = viewModel::closeToGrid,
                    )

                    EmojiMode.Settings -> SettingsModeContent(onBack = viewModel::closeToGrid)
                }
            }
        }
    }
}

@Composable
private fun GridModeContent(
    selected: List<String>,
    copied: Boolean,
    onCopy: () -> Unit,
    onClear: () -> Unit,
    onEmojiTap: (String) -> Unit,
    onOpenTopUsed: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        EmojiHeaderBar(
            leftLabel = if (copied) "COPIED" else "COPY",
            onLeftClick = onCopy,
            rightLabel = "CLEAR",
            onRightClick = onClear,
        )

        SelectionTray(selected)

        EmojiGrid(
            onEmojiTap = onEmojiTap,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        )

        LightBottomBar(
            items = listOf(
                LightBarButton.LightIcon(icon = LightIcons.STAR, onClick = onOpenTopUsed),
                LightBarButton.LightIcon(icon = LightIcons.SEARCH, onClick = onOpenSearch),
                LightBarButton.LightIcon(icon = LightIcons.SETTINGS, onClick = onOpenSettings),
            ),
        )
    }
}

@Composable
private fun TopUsedModeContent(
    selected: List<String>,
    copied: Boolean,
    onCopy: () -> Unit,
    onClear: () -> Unit,
    onBack: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        EmojiHeaderBar(
            leftLabel = if (copied) "COPIED" else "COPY",
            onLeftClick = onCopy,
            rightLabel = "CLEAR",
            onRightClick = onClear,
        )

        SelectionTray(selected)

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            PlaceholderContent("Top Used — coming soon (Phase 4)")
        }

        LightBottomBar(
            items = listOf(
                LightBarButton.LightIcon(icon = LightIcons.CLOSE, onClick = onBack),
            ),
        )
    }
}

@Composable
private fun SettingsModeContent(onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        LightTopBar(
            leftButton = LightBarButton.LightIcon(icon = LightIcons.BACK, onClick = onBack),
            center = LightTopBarCenter.Text("Settings"),
            modifier = Modifier.padding(bottom = 1f.gridUnitsAsDp()),
        )
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            PlaceholderContent("Settings — coming soon (Phase 5)")
        }
    }
}

@Composable
private fun SelectionTray(selected: List<String>) {
    if (selected.isEmpty()) return
    LightText(
        text = selected.joinToString(""),
        variant = LightTextVariant.Subtitle,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 1f.gridUnitsAsDp(),
                vertical = 0.5f.gridUnitsAsDp(),
            ),
    )
}

@Composable
private fun PlaceholderContent(text: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        LightText(
            text = text,
            variant = LightTextVariant.Copy,
            align = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 2f.gridUnitsAsDp()),
        )
    }
}

/**
 * A plain LightTopBar hardcodes its buttons to LightTextVariant.Fine internally (not
 * overridable), but the SDK's own "primary bottom action" buttons — e.g. the weather
 * example's "THIS WEEK" — use LightTextVariant.Button. COPY/CLEAR are that same kind of
 * primary action, so we render them directly instead of going through LightTopBar.
 */
@Composable
private fun EmojiHeaderBar(
    leftLabel: String,
    onLeftClick: () -> Unit,
    rightLabel: String,
    onRightClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(HEADER_HEIGHT_GRID_UNITS.gridUnitsAsDp())
            .padding(horizontal = HEADER_HORIZONTAL_PADDING_GRID_UNITS.gridUnitsAsDp()),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LightText(
            text = leftLabel,
            variant = LightTextVariant.Button,
            modifier = Modifier.lightClickable(onClick = onLeftClick),
        )
        LightText(
            text = rightLabel,
            variant = LightTextVariant.Button,
            modifier = Modifier.lightClickable(onClick = onRightClick),
        )
    }
}
