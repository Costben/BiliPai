package com.android.purebilibili.core.ui.renderer.coui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.state.ToggleableState
import com.android.purebilibili.core.ui.components.AppCheckboxColors
import com.android.purebilibili.core.ui.components.appDesktopFocusableItemVisuals
import io.github.suqi8.coui.kmp.basic.Checkbox
import io.github.suqi8.coui.kmp.basic.CheckboxDefaults
import io.github.suqi8.coui.kmp.theme.COUITheme
import com.android.purebilibili.core.theme.resolveControlGlyphColor

@Composable
internal fun AppCouiCheckbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier,
    enabled: Boolean,
    colors: AppCheckboxColors?,
) {
    val nativeColors = colors?.let {
        CheckboxDefaults.checkboxColors(
            // Explicit fills need their own paired checkmark; unedited native Miuix stays native.
            checkedForegroundColor = resolveControlGlyphColor(
                COUITheme.colorScheme.onPrimary, it.checkedColor, COUITheme.colorScheme.surface,
            ),
            checkedBackgroundColor = it.checkedColor,
            uncheckedBackgroundColor = it.uncheckedColor,
        )
    } ?: CheckboxDefaults.checkboxColors()
    ProvideAppCouiHapticFeedback {
        Checkbox(
            state = if (checked) ToggleableState.On else ToggleableState.Off,
            onClick = onCheckedChange?.let { callback -> { callback(!checked) } },
            modifier = modifier.appDesktopFocusableItemVisuals(enabled && onCheckedChange != null),
            colors = nativeColors,
            enabled = enabled,
        )
    }
}
