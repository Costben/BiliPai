package com.android.purebilibili.core.ui.renderer.coui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.android.purebilibili.core.ui.components.appDesktopFocusableItemVisuals
import io.github.suqi8.coui.kmp.basic.Switch
import io.github.suqi8.coui.kmp.basic.SwitchDefaults

@Composable
internal fun AppCouiSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier,
    enabled: Boolean,
) {
    ProvideAppCouiHapticFeedback {
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.switchColors(),
            modifier = modifier.appDesktopFocusableItemVisuals(enabled && onCheckedChange != null),
        )
    }
}
