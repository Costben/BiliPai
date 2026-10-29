package com.android.purebilibili.core.ui.renderer.coui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.android.purebilibili.core.ui.components.appDesktopFocusableItemVisuals
import io.github.suqi8.coui.kmp.basic.RadioButton
import io.github.suqi8.coui.kmp.basic.RadioButtonDefaults
import io.github.suqi8.coui.kmp.theme.COUITheme
import com.android.purebilibili.core.theme.resolveControlGlyphColor

@Composable
internal fun AppCouiRadioButton(
    selected: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier,
    enabled: Boolean,
) {
    val scheme = COUITheme.colorScheme
    val colors = if (com.android.purebilibili.core.ui.isMiuixNonGlassEnabled()) {
        RadioButtonDefaults.radioButtonColors()
    } else RadioButtonDefaults.radioButtonColors(
        selectedColor = resolveControlGlyphColor(scheme.primary, scheme.surface, scheme.surface),
    )
    ProvideAppCouiHapticFeedback(scene = com.android.purebilibili.core.ui.AppHapticScene.MENUS) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            modifier = modifier.appDesktopFocusableItemVisuals(enabled && onClick != null),
            enabled = enabled,
            colors = colors,
        )
    }
}
