package com.android.purebilibili.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import com.android.purebilibili.core.theme.AppUiStyle
import com.android.purebilibili.core.theme.LocalAppUiStyle

/**
 * Canonical decision for which preset's renderer a shared adaptive primitive
 * should dispatch to. Each primitive may collapse [MATERIAL3] and [MIUIX_BRIDGED]
 * onto the same code path (e.g. AdaptiveTopAppBar) or split them (e.g. SuperDialog
 * vs AlertDialog). Use this enum at the entry point of every preset-aware primitive
 * so the dispatch is testable in plain Kotlin and consistent across primitives.
 */
enum class PresetPrimitiveRenderer {
    /** Material 3 native components. MATERIAL3 style. */
    MATERIAL3,
    /** Miuix native components. MIUIX style. */
    MIUIX_BRIDGED,
    /**
     * COUI 17 native components. COUI17 style.
     *
     * 迁移期的收敛点：还没换成 COUI 的共享组件把 COUI17 当 MATERIAL3 处理，
     * 保证选中 COUI 17 时不会出现半套 Miuix 的半成品外观。
     */
    COUI_BRIDGED
}

fun resolvePresetPrimitiveRenderer(
    uiStyle: AppUiStyle
): PresetPrimitiveRenderer = when (uiStyle) {
    AppUiStyle.MIUIX -> PresetPrimitiveRenderer.MIUIX_BRIDGED
    AppUiStyle.MATERIAL3 -> PresetPrimitiveRenderer.MATERIAL3
    AppUiStyle.COUI17 -> PresetPrimitiveRenderer.MIUIX_BRIDGED
}

@Composable
@ReadOnlyComposable
fun rememberPresetPrimitiveRenderer(): PresetPrimitiveRenderer =
    resolvePresetPrimitiveRenderer(LocalAppUiStyle.current)
