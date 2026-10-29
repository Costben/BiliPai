package com.android.purebilibili.core.ui

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.suqi8.coui.kmp.basic.CircularProgressIndicator as CouiCircularProgressIndicator
import io.github.suqi8.coui.kmp.basic.InfiniteProgressIndicator as CouiInfiniteProgressIndicator
import io.github.suqi8.coui.kmp.basic.ProgressIndicatorDefaults as CouiProgressIndicatorDefaults

/**
 * Preset-aware indeterminate loading indicator.
 *
 * - Material 3: official [CircularProgressIndicator] at page or compact sizes,
 *   colored with [AppContentColors.accent()] so dynamic color applies automatically.
 * - Miuix: official Miuix infinite / circular progress indicators with theme primary.
 *
 * Prefer this over bare Material/Miuix progress widgets in feature screens so theme
 * switches stay consistent.
 *
 * @param size optional visual size. When omitted, each native component uses its own default.
 *   Sizes ≤ [AdaptiveLoadingCompactSizeThresholdDp] select the compact visual recipe.
 * @param color optional tint. [Color.Unspecified] keeps each theme's default (dynamic primary
 *   on MD3, Miuix primary on Miuix).
 * @param strokeWidth only applied to stroke-based visuals (circular variants).
 * @param density force [AdaptiveLoadingDensity.PAGE] or [AdaptiveLoadingDensity.COMPACT];
 *   when null, inferred from [size].
 */
@Composable
fun AdaptiveLoadingIndicator(
    modifier: Modifier = Modifier,
    size: Dp? = null,
    color: Color = Color.Unspecified,
    strokeWidth: Dp = 2.dp,
    density: AdaptiveLoadingDensity? = null,
) {
    val resolvedDensity = density
        ?: resolveAdaptiveLoadingDensity(size?.value)
    val visual = resolveAdaptiveLoadingVisual(
        renderer = rememberPresetPrimitiveRenderer(),
        density = resolvedDensity,
    )
    val resolvedColor = if (color == Color.Unspecified) {
        resolveAdaptiveLoadingDefaultColor(visual)
    } else {
        color
    }

    when (visual) {
        AdaptiveLoadingVisual.MATERIAL3_PAGE_CIRCULAR -> {
            CircularProgressIndicator(
                modifier = modifier.size(size ?: 40.dp),
                color = resolvedColor,
                strokeWidth = strokeWidth,
            )
        }

        AdaptiveLoadingVisual.MATERIAL3_CIRCULAR -> {
            val indicatorSize = size ?: 24.dp
            CircularProgressIndicator(
                modifier = modifier.size(indicatorSize),
                color = resolvedColor,
                strokeWidth = strokeWidth,
            )
        }

        AdaptiveLoadingVisual.MIUIX_INFINITE -> {
            val indicatorSize = size
                ?: CouiProgressIndicatorDefaults.DefaultInfiniteProgressIndicatorSize * 1.6f
            CouiInfiniteProgressIndicator(
                modifier = modifier,
                color = resolvedColor,
                size = indicatorSize,
            )
        }

        AdaptiveLoadingVisual.MIUIX_CIRCULAR -> {
            val indicatorSize = size
                ?: CouiProgressIndicatorDefaults.DefaultCircularProgressIndicatorSize
            CouiCircularProgressIndicator(
                modifier = modifier,
                progress = null,
                colors = CouiProgressIndicatorDefaults.progressIndicatorColors(
                    foregroundColor = resolvedColor,
                ),
                strokeWidth = strokeWidth.coerceAtLeast(
                    CouiProgressIndicatorDefaults.DefaultCircularProgressIndicatorStrokeWidth * 0.5f
                ),
                size = indicatorSize,
            )
        }
    }
}

@Composable
private fun resolveAdaptiveLoadingDefaultColor(visual: AdaptiveLoadingVisual): Color {
    return when (visual) {
        AdaptiveLoadingVisual.MATERIAL3_PAGE_CIRCULAR,
        AdaptiveLoadingVisual.MATERIAL3_CIRCULAR -> AppContentColors.accent()

        AdaptiveLoadingVisual.MIUIX_INFINITE,
        AdaptiveLoadingVisual.MIUIX_CIRCULAR -> AppSurfaceTokens.primary()
    }
}
