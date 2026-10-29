package com.android.purebilibili.feature.dynamic

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.android.purebilibili.core.theme.AppUiStyle
import com.android.purebilibili.core.theme.LocalAppUiStyle
import io.github.suqi8.coui.kmp.theme.COUITheme
import androidx.compose.ui.graphics.Color
import com.android.purebilibili.core.theme.usesMiuixFamilySkin

internal fun resolveDynamicLikedColor(colorScheme: ColorScheme): Color = colorScheme.tertiary

internal object DynamicStatusPalette {
    @Composable
    fun liked(): Color = if (LocalAppUiStyle.current.usesMiuixFamilySkin) {
        COUITheme.colorScheme.primary
    } else {
        resolveDynamicLikedColor(MaterialTheme.colorScheme)
    }
}
