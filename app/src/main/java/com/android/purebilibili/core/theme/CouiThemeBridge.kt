package com.android.purebilibili.core.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import io.github.suqi8.coui.kmp.theme.Colors as CouiColors
import io.github.suqi8.coui.kmp.theme.TextStyles as CouiTextStyles
import io.github.suqi8.coui.kmp.theme.darkColorScheme as couiDarkColorScheme
import io.github.suqi8.coui.kmp.theme.lightColorScheme as couiLightColorScheme
import top.yukonga.miuix.kmp.theme.Colors as MiuixColors
import top.yukonga.miuix.kmp.theme.TextStyles as MiuixTextStyles

// COUI 17 的辨识度在于灰底白卡与 ColorOS 中性色，整套搬 BiliPai 调色板会退化成 Miuix 观感；
// 因此只把用户选的强调色灌进 primary 家族，表面与文字色保持 COUI 原生。
// 深色下 COUI 原生的容器与强调前景都比 primary 更亮一档，这里按同样的关系推导。
internal fun resolveCouiColorsFromAccent(
    accent: Color,
    onAccent: Color,
    darkTheme: Boolean,
): CouiColors {
    val base = if (darkTheme) couiDarkColorScheme() else couiLightColorScheme()
    val brightAccent = if (darkTheme) lerp(accent, Color.White, 0.25f) else accent
    val tint = accent.copy(alpha = if (darkTheme) 0.25f else 0.15f)
    return base.copy(
        primary = accent,
        onPrimary = onAccent,
        primaryVariant = accent,
        onPrimaryVariant = lerp(accent, Color.White, 0.6f),
        disabledPrimary = accent.copy(alpha = 0.3f),
        disabledPrimaryButton = accent.copy(alpha = 0.3f),
        primaryContainer = brightAccent,
        onPrimaryContainer = onAccent,
        tertiaryContainer = tint,
        onTertiaryContainer = brightAccent,
        tertiaryContainerVariant = tint,
        sliderKeyPointForeground = lerp(accent, Color.White, 0.4f),
    )
}

// 两套 Colors 角色一一对应；COUI 多出的两个角色按其自身的兼容回退规则取值。
internal fun MiuixColors.toCouiColors(): CouiColors = CouiColors(
    primary = primary,
    onPrimary = onPrimary,
    primaryVariant = primaryVariant,
    onPrimaryVariant = onPrimaryVariant,
    error = error,
    onError = onError,
    errorContainer = errorContainer,
    onErrorContainer = onErrorContainer,
    disabledPrimary = disabledPrimary,
    disabledOnPrimary = disabledOnPrimary,
    disabledPrimaryButton = disabledPrimaryButton,
    disabledOnPrimaryButton = disabledOnPrimaryButton,
    disabledPrimarySlider = disabledPrimarySlider,
    primaryContainer = primaryContainer,
    onPrimaryContainer = onPrimaryContainer,
    secondary = secondary,
    onSecondary = onSecondary,
    secondaryVariant = secondaryVariant,
    onSecondaryVariant = onSecondaryVariant,
    disabledSecondary = disabledSecondary,
    disabledOnSecondary = disabledOnSecondary,
    disabledSecondaryVariant = disabledSecondaryVariant,
    disabledOnSecondaryVariant = disabledOnSecondaryVariant,
    secondaryContainer = secondaryContainer,
    onSecondaryContainer = onSecondaryContainer,
    secondaryContainerVariant = secondaryContainerVariant,
    onSecondaryContainerVariant = onSecondaryContainerVariant,
    tertiaryContainer = tertiaryContainer,
    onTertiaryContainer = onTertiaryContainer,
    tertiaryContainerVariant = tertiaryContainerVariant,
    background = background,
    onBackground = onBackground,
    onBackgroundVariant = onBackgroundVariant,
    surface = surface,
    onSurface = onSurface,
    surfaceVariant = surfaceVariant,
    onSurfaceSecondary = onSurfaceSecondary,
    onSurfaceVariantSummary = onSurfaceVariantSummary,
    onSurfaceVariantActions = onSurfaceVariantActions,
    disabledOnSurface = disabledOnSurface,
    surfaceContainer = surfaceContainer,
    onSurfaceContainer = onSurfaceContainer,
    onSurfaceContainerVariant = onSurfaceContainerVariant,
    surfaceContainerHigh = surfaceContainerHigh,
    onSurfaceContainerHigh = onSurfaceContainerHigh,
    surfaceContainerHighest = surfaceContainerHighest,
    onSurfaceContainerHighest = onSurfaceContainerHighest,
    outline = outline,
    dividerLine = dividerLine,
    windowDimming = windowDimming,
    sliderKeyPoint = sliderKeyPoint,
    sliderKeyPointForeground = sliderKeyPointForeground,
    sliderBackground = sliderBackground,
    bar = surface,
    labelPrimaryVariant = onSurfaceVariantSummary,
)

// Miuix 色板没有 bar / labelPrimaryVariant，经过它中转后会退化成页面底色，悬浮底栏就和画布融成一片；
// COUI 17 下把这两个导航角色还原成 COUI 原生值。
internal fun CouiColors.withNativeNavigationRoles(darkTheme: Boolean): CouiColors {
    val native = if (darkTheme) couiDarkColorScheme() else couiLightColorScheme()
    return copy(bar = native.bar, labelPrimaryVariant = native.labelPrimaryVariant)
}

internal fun CouiColors.toMiuixColors(): MiuixColors = MiuixColors(
    primary = primary,
    onPrimary = onPrimary,
    primaryVariant = primaryVariant,
    onPrimaryVariant = onPrimaryVariant,
    error = error,
    onError = onError,
    errorContainer = errorContainer,
    onErrorContainer = onErrorContainer,
    disabledPrimary = disabledPrimary,
    disabledOnPrimary = disabledOnPrimary,
    disabledPrimaryButton = disabledPrimaryButton,
    disabledOnPrimaryButton = disabledOnPrimaryButton,
    disabledPrimarySlider = disabledPrimarySlider,
    primaryContainer = primaryContainer,
    onPrimaryContainer = onPrimaryContainer,
    secondary = secondary,
    onSecondary = onSecondary,
    secondaryVariant = secondaryVariant,
    onSecondaryVariant = onSecondaryVariant,
    disabledSecondary = disabledSecondary,
    disabledOnSecondary = disabledOnSecondary,
    disabledSecondaryVariant = disabledSecondaryVariant,
    disabledOnSecondaryVariant = disabledOnSecondaryVariant,
    secondaryContainer = secondaryContainer,
    onSecondaryContainer = onSecondaryContainer,
    secondaryContainerVariant = secondaryContainerVariant,
    onSecondaryContainerVariant = onSecondaryContainerVariant,
    tertiaryContainer = tertiaryContainer,
    onTertiaryContainer = onTertiaryContainer,
    tertiaryContainerVariant = tertiaryContainerVariant,
    background = background,
    onBackground = onBackground,
    onBackgroundVariant = onBackgroundVariant,
    surface = surface,
    onSurface = onSurface,
    surfaceVariant = surfaceVariant,
    onSurfaceSecondary = onSurfaceSecondary,
    onSurfaceVariantSummary = onSurfaceVariantSummary,
    onSurfaceVariantActions = onSurfaceVariantActions,
    disabledOnSurface = disabledOnSurface,
    surfaceContainer = surfaceContainer,
    onSurfaceContainer = onSurfaceContainer,
    onSurfaceContainerVariant = onSurfaceContainerVariant,
    surfaceContainerHigh = surfaceContainerHigh,
    onSurfaceContainerHigh = onSurfaceContainerHigh,
    surfaceContainerHighest = surfaceContainerHighest,
    onSurfaceContainerHighest = onSurfaceContainerHighest,
    outline = outline,
    dividerLine = dividerLine,
    windowDimming = windowDimming,
    sliderKeyPoint = sliderKeyPoint,
    sliderKeyPointForeground = sliderKeyPointForeground,
    sliderBackground = sliderBackground,
)

// 字号档位与自定义字体已经折算进 COUI 文字样式；仍在用 Miuix 的导航栏与自研导航模块读同一份，避免两处字号不一致。
internal fun CouiTextStyles.toMiuixTextStyles(): MiuixTextStyles = MiuixTextStyles(
    main = main,
    paragraph = paragraph,
    body1 = body1,
    body2 = body2,
    button = button,
    footnote1 = footnote1,
    footnote2 = footnote2,
    headline1 = headline1,
    headline2 = headline2,
    subtitle = subtitle,
    title1 = title1,
    title2 = title2,
    title3 = title3,
    title4 = title4,
)
