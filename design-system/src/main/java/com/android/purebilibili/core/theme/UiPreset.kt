package com.android.purebilibili.core.theme

import androidx.compose.runtime.staticCompositionLocalOf

enum class UiPreset(val value: Int, val label: String) {
    IOS(0, "iOS"),
    MD3(1, "安卓原生");

    companion object {
        fun fromValue(value: Int): UiPreset = fromValueOrNull(value) ?: MD3

        fun fromValueOrNull(value: Int): UiPreset? = entries.find { it.value == value }
    }
}

enum class AndroidNativeVariant(val value: Int, val label: String) {
    MATERIAL3(0, "Material 3"),
    MIUIX(1, "Miuix"),
    // COUI 17 是 Miuix 的 ColorOS 风格分支；值 3 是新增变体的第一个空闲位。
    COUI17(3, "COUI 17");

    companion object {
        private const val LEGACY_REMOVED_VARIANT_VALUE = 2

        fun fromValue(value: Int): AndroidNativeVariant = fromValueOrNull(value) ?: MIUIX

        /** 历史已移除的变体值 2 仍保留其原有映射为 MATERIAL3。 */
        fun fromValueOrNull(value: Int): AndroidNativeVariant? {
            if (value == LEGACY_REMOVED_VARIANT_VALUE) return MATERIAL3
            return entries.find { it.value == value }
        }
    }
}

/**
 * 运行时主题选择：Material 3 / Miuix / COUI 17。
 * 历史 iOS 值在迁移边界（fromLegacyValues / resolveUiStyle）解析为 MIUIX，
 * 不再进入运行时。
 */
enum class AppUiStyle {
    MATERIAL3,
    MIUIX,
    COUI17;

    companion object {
        /**
         * 按迁移表解析历史旧键：全量缺失使用新用户默认的 Material 3；
         * 历史 iOS 或非法组合迁移为 MIUIX；
         * COUI 17 变体单独存在时也按 COUI 17 恢复；
         * 仅 MD3 + 合法变体保留有效选择。
         */
        fun fromLegacyValues(
            rawUiPreset: Int?,
            rawAndroidNativeVariant: Int?
        ): AppUiStyle {
            val uiPreset = rawUiPreset?.let(UiPreset::fromValueOrNull)
            val androidNativeVariant = rawAndroidNativeVariant?.let(
                AndroidNativeVariant::fromValueOrNull
            )
            return when {
                rawUiPreset == null && rawAndroidNativeVariant == null -> AppUiStyle.MATERIAL3
                // 老版本拆成两个键写库，新变体可能只留下变体键，不能因此退化成 MIUIX。
                androidNativeVariant == AndroidNativeVariant.COUI17 -> AppUiStyle.COUI17
                // 非法或历史 iOS 组合保留旧用户的 MIUIX 兼容行为。
                uiPreset == null || androidNativeVariant == null -> AppUiStyle.MIUIX
                else -> resolveUiStyle(uiPreset, androidNativeVariant)
            }
        }
    }
}

fun resolveUiStyle(
    uiPreset: UiPreset,
    androidNativeVariant: AndroidNativeVariant
): AppUiStyle = when (uiPreset) {
    // 单向迁移：历史 iOS 值统一迁移为 MIUIX。
    UiPreset.IOS -> AppUiStyle.MIUIX
    UiPreset.MD3 -> when (androidNativeVariant) {
        AndroidNativeVariant.MATERIAL3 -> AppUiStyle.MATERIAL3
        AndroidNativeVariant.MIUIX -> AppUiStyle.MIUIX
        AndroidNativeVariant.COUI17 -> AppUiStyle.COUI17
    }
}

/**
 * COUI 17 由 Miuix fork 而来，两者共用同一套几何、动效与「自绘皮肤」判定；
 * 只有真正挑选组件实现时才需要区分是哪一套皮肤。
 */
val AppUiStyle.usesMiuixFamilySkin: Boolean
    get() = this == AppUiStyle.MIUIX || this == AppUiStyle.COUI17

/** 运行时主题 Local。 */
val LocalAppUiStyle = staticCompositionLocalOf { AppUiStyle.MATERIAL3 }
val LocalDynamicColorActive = staticCompositionLocalOf { false }
val LocalSettingsLiquidGlassEnabled = staticCompositionLocalOf { false }
