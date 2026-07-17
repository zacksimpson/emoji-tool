package com.zacksimpson.emojis

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.thelightphone.sdk.ui.gridUnitsAsDp

@InitialScreen
class EmojiToolScreen(sealedActivity: SealedLightActivity) :
    LightScreen<Unit, EmojiToolViewModel>(sealedActivity) {

    override val viewModelClass: Class<EmojiToolViewModel>
        get() = EmojiToolViewModel::class.java

    override fun createViewModel(): EmojiToolViewModel = EmojiToolViewModel()

    @Composable
    override fun Content() {
        val themeColors by LightThemeController.colors.collectAsState()
        val tab by viewModel.currentTab.collectAsState()
        val selected by SelectionStore.selected.collectAsState()
        val copied by viewModel.copied.collectAsState()

        LightTheme(colors = themeColors) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(LightThemeTokens.colors.background),
            ) {
                LightTopBar(
                    leftButton = LightBarButton.Text(
                        text = if (copied) "COPIED" else "COPY",
                        onClick = viewModel::copySelection,
                    ),
                    rightButton = LightBarButton.Text(
                        text = "CLEAR",
                        onClick = viewModel::clearSelection,
                    ),
                )

                if (selected.isNotEmpty()) {
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

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                ) {
                    when (tab) {
                        EmojiTab.Home -> HomeTab(
                            onEmojiTap = viewModel::selectEmoji,
                            modifier = Modifier.fillMaxSize(),
                        )
                        EmojiTab.Search -> PlaceholderTab("Search — coming soon")
                        EmojiTab.TopUsed -> PlaceholderTab("Top Used — coming soon")
                        EmojiTab.Settings -> PlaceholderTab("Settings — coming soon")
                    }
                }

                LightBottomBar(
                    items = listOf(
                        LightBarButton.LightIcon(
                            icon = LightIcons.LIST,
                            onClick = { viewModel.selectTab(EmojiTab.Home) },
                        ),
                        LightBarButton.LightIcon(
                            icon = LightIcons.SEARCH,
                            onClick = { viewModel.selectTab(EmojiTab.Search) },
                        ),
                        LightBarButton.LightIcon(
                            icon = LightIcons.STAR,
                            onClick = { viewModel.selectTab(EmojiTab.TopUsed) },
                        ),
                        LightBarButton.LightIcon(
                            icon = LightIcons.SETTINGS,
                            onClick = { viewModel.selectTab(EmojiTab.Settings) },
                        ),
                    ),
                )
            }
        }
    }
}

@Composable
private fun PlaceholderTab(text: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        LightText(text = text, variant = LightTextVariant.Copy, lighten = true)
    }
}
