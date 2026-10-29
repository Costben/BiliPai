package com.android.purebilibili.core.ui.renderer.coui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.android.purebilibili.core.ui.components.AppSliderColors
import com.android.purebilibili.core.ui.components.appDesktopFocusableItemVisuals
import io.github.suqi8.coui.kmp.basic.Slider
import io.github.suqi8.coui.kmp.basic.SliderDefaults

@Composable
internal fun AppCouiSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    onValueChangeFinished: (() -> Unit)?,
    colors: AppSliderColors?,
) {
    val nativeColors = colors?.let {
        SliderDefaults.sliderColors(
            foregroundColor = it.activeTrackColor,
            backgroundColor = it.inactiveTrackColor,
            thumbColor = it.thumbColor,
        )
    } ?: if (com.android.purebilibili.core.ui.isMiuixNonGlassEnabled()) {
        SliderDefaults.sliderColors()
    } else SliderDefaults.sliderColors(thumbColor = com.android.purebilibili.core.ui.components.appControlThumbColor())
    Slider(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.appDesktopFocusableItemVisuals(enabled),
        enabled = enabled,
        valueRange = valueRange,
        steps = steps,
        onValueChangeFinished = onValueChangeFinished,
        colors = nativeColors,
    )
}
