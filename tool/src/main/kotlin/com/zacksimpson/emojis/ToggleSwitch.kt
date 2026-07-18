package com.zacksimpson.emojis

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.thelightphone.sdk.ui.LightIcon
import com.thelightphone.sdk.ui.LightIcons
import com.thelightphone.sdk.ui.LightText
import com.thelightphone.sdk.ui.LightTextVariant
import com.thelightphone.sdk.ui.gridUnitsAsDp

/**
 * Settings-row toggle: the SDK's own TOGGLE_ON/TOGGLE_OFF glyphs on the left, label (plus an
 * optional description subtitle) on the right. Layout ported from reminders-tool's
 * native-rewrite ToggleSwitch for a consistent settings-row style across Light tools.
 *
 * The icon selection is intentionally inverted from the glyph names: LightIcons.TOGGLE_ON's
 * knob actually renders on the left of the track and TOGGLE_OFF's on the right (checked the
 * raw vector paths in ic_toggle_on_white.xml/ic_toggle_off_white.xml directly) — backwards from
 * the near-universal "right = on" switch convention every other toggle uses, including
 * conversations-tool's own (non-Light-SDK) `if (enabled) ic_toggle_on else ic_toggle_off`.
 */
@Composable
fun ToggleSwitch(
    label: String,
    value: Boolean,
    onValueChange: (Boolean) -> Unit,
    description: String? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onValueChange(!value) }
            .padding(horizontal = 1.5f.gridUnitsAsDp(), vertical = 1f.gridUnitsAsDp()),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LightIcon(
            // 2f matches LightIcon's own default size, and how the SDK's icon gallery
            // (ui-demo's UiDemoIconsScreen) renders every icon, including this one.
            icon = if (value) LightIcons.TOGGLE_OFF else LightIcons.TOGGLE_ON,
            size = 2f,
            modifier = Modifier.padding(end = 1f.gridUnitsAsDp()),
        )
        Column {
            LightText(text = label, variant = LightTextVariant.Heading)
            if (description != null) {
                LightText(
                    text = description,
                    variant = LightTextVariant.Detail,
                    modifier = Modifier.padding(top = 0.15f.gridUnitsAsDp()),
                )
            }
        }
    }
}
