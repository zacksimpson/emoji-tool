package com.zacksimpson.emojis

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.thelightphone.lp3Keyboard.ui.DefaultLp3KeyboardViewModel
import com.thelightphone.lp3Keyboard.ui.Layout
import com.thelightphone.lp3Keyboard.ui.LayoutOptions
import com.thelightphone.sdk.rememberKeyboardOptions
import com.thelightphone.sdk.ui.LightBarButton
import com.thelightphone.sdk.ui.LightText
import com.thelightphone.sdk.ui.LightTextVariant
import com.thelightphone.sdk.ui.LightTopBar
import com.thelightphone.sdk.ui.LightTopBarCenter
import com.thelightphone.sdk.ui.LightIcons
import com.thelightphone.sdk.ui.LightThemeTokens
import com.thelightphone.sdk.ui.designVerticalPxToDp
import com.thelightphone.sdk.ui.gridUnitsAsDp
import com.thelightphone.sdk.ui.keyboard.LightEmbeddedLp3Keyboard
import com.thelightphone.sdk.ui.lightClickable

private const val QUERY_UNDERLINE_THICKNESS_PX = 3f

private const val COLUMNS = 6
private const val HORIZONTAL_PADDING_GRID_UNITS = 1f

@Composable
fun EmojiSearchContent(
    onBack: () -> Unit,
    onEmojiTap: (String) -> Unit,
) {
    val textFieldState = rememberTextFieldState("")
    val keyboardOptionsFlow = rememberKeyboardOptions()
    val results by remember {
        derivedStateOf { searchEmoji(textFieldState.text.toString()) }
    }

    var keyboardVisible by remember { mutableStateOf(true) }

    val callback = remember(textFieldState) {
        EmojiSearchInputCallback(textFieldState, onClose = { keyboardVisible = false })
    }
    // Constructed directly (not via the ViewModelProvider/viewModel() composable) so it's
    // recreated fresh every time this composable enters composition, matching callback's and
    // textFieldState's lifetime. Going through viewModel() with a constant key would cache it
    // in the screen-level ViewModelStore forever — after the first Search visit, every later
    // visit would keep reusing that first instance, still wired to an already-disposed
    // TextFieldState, leaving the keyboard visually present but functionally dead.
    val keyboardViewModel = remember(callback, keyboardOptionsFlow) {
        DefaultLp3KeyboardViewModel(
            callback,
            keyboardOptionsFlow = keyboardOptionsFlow,
            // Always show the close key, on every sub-layout (letters/numbers/symbols/emoji) —
            // unlike a submit-flow screen (e.g. LightTextInputEditor), this screen wants the
            // keyboard collapsible at any time so more results are visible.
            optionsForLayout = { _: Layout -> LayoutOptions(displayCloseButton = true) },
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LightTopBar(
            leftButton = LightBarButton.LightIcon(icon = LightIcons.BACK, onClick = onBack),
            center = LightTopBarCenter.Text("Search"),
            modifier = Modifier.padding(bottom = 1f.gridUnitsAsDp()),
        )

        QueryDisplay(
            query = textFieldState.text.toString(),
            onClick = { keyboardVisible = true },
        )

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (textFieldState.text.isNotEmpty() && results.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    LightText(text = "No results", variant = LightTextVariant.Copy, align = TextAlign.Center)
                }
            } else if (results.isNotEmpty()) {
                SearchResultsGrid(results = results, onEmojiTap = onEmojiTap)
            }
        }

        if (keyboardVisible) {
            LightEmbeddedLp3Keyboard(viewModel = keyboardViewModel)
        }
    }
}

@Composable
private fun QueryDisplay(query: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .lightClickable(onClick = onClick)
            .padding(horizontal = 1f.gridUnitsAsDp(), vertical = 0.25f.gridUnitsAsDp()),
    ) {
        LightText(
            text = query,
            variant = LightTextVariant.Heading,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(0.5f.gridUnitsAsDp()))
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(QUERY_UNDERLINE_THICKNESS_PX.designVerticalPxToDp())
                .background(LightThemeTokens.colors.content),
        )
    }
}

@Composable
private fun SearchResultsGrid(
    results: List<EmojiEntry>,
    onEmojiTap: (String) -> Unit,
) {
    val horizontalPadding = HORIZONTAL_PADDING_GRID_UNITS.gridUnitsAsDp()
    val screenWidthDp = LocalConfiguration.current.screenWidthDp.dp
    val cellSize = (screenWidthDp - horizontalPadding * 2) / COLUMNS

    LazyVerticalGrid(
        columns = GridCells.Fixed(COLUMNS),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = horizontalPadding),
    ) {
        items(results, key = { it.emoji }) { entry ->
            EmojiCell(emoji = entry.emoji, cellSize = cellSize, onClick = { onEmojiTap(entry.emoji) })
        }
    }
}
