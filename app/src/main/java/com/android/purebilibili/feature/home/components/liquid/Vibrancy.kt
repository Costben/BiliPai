package com.android.purebilibili.feature.home.components.liquid

import io.github.suqi8.coui.kmp.blur.BackdropEffectScope
import io.github.suqi8.coui.kmp.blur.colorControls

fun BackdropEffectScope.vibrancy(saturation: Float = 1.5f) {
    colorControls(
        brightness = 0f,
        contrast = 1f,
        saturation = saturation.coerceIn(0f, 2f),
    )
}
