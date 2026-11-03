package com.android.purebilibili.feature.video.ui.components

import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import io.github.suqi8.coui.kmp.theme.COUITheme
import com.android.purebilibili.core.ui.AppContentColors
import com.android.purebilibili.core.ui.AppSurfaceTokens

internal enum class VideoCommentTextRole { AUTHOR, METADATA, BODY, ACTION, ACTION_COUNT, SUB_REPLY }

@Composable
@ReadOnlyComposable
internal fun resolveVideoCommentFontSize(role: VideoCommentTextRole): TextUnit {
    val styles = COUITheme.textStyles
    return when (role) {
        VideoCommentTextRole.AUTHOR -> styles.footnote1.fontSize
        // 评论正文与楼中楼都是主要阅读内容，跟随页面正文的字阶。
        VideoCommentTextRole.SUB_REPLY,
        VideoCommentTextRole.BODY -> styles.body1.fontSize
        VideoCommentTextRole.METADATA, VideoCommentTextRole.ACTION_COUNT -> styles.footnote2.fontSize
        VideoCommentTextRole.ACTION -> styles.footnote1.fontSize
    }
}

internal object VideoCommentTypographyTokens {
    val author: TextUnit
        @Composable @ReadOnlyComposable
        get() = resolveVideoCommentFontSize(VideoCommentTextRole.AUTHOR)
    val metadata: TextUnit
        @Composable @ReadOnlyComposable
        get() = resolveVideoCommentFontSize(VideoCommentTextRole.METADATA)
    val body: TextUnit
        @Composable @ReadOnlyComposable
        get() = resolveVideoCommentFontSize(VideoCommentTextRole.BODY)
    val action: TextUnit
        @Composable @ReadOnlyComposable
        get() = resolveVideoCommentFontSize(VideoCommentTextRole.ACTION)
    val actionCount: TextUnit
        @Composable @ReadOnlyComposable
        get() = resolveVideoCommentFontSize(VideoCommentTextRole.ACTION_COUNT)
    val subReply: TextUnit
        @Composable @ReadOnlyComposable
        get() = resolveVideoCommentFontSize(VideoCommentTextRole.SUB_REPLY)
    val metadataLineHeight: TextUnit
        @Composable @ReadOnlyComposable
        get() = COUITheme.textStyles.footnote2.lineHeight
}

internal data class VideoCommentAppearance(
    val panelColor: Color,
    val dividerColor: Color,
    val sectionDividerColor: Color,
    val primaryTextColor: Color,
    val secondaryTextColor: Color,
    val accentColor: Color,
    val actionTint: Color,
    val sortTint: Color,
    val auxiliaryTint: Color,
    val placeholderColor: Color,
    val composerHintBackgroundColor: Color,
    val toggleCheckedBackgroundColor: Color,
    val toggleCheckedContentColor: Color,
    val toggleUncheckedBackgroundColor: Color,
    val toggleUncheckedContentColor: Color
)

internal fun resolveVideoCommentAppearance(
    surfaceColor: Color,
    surfaceVariantColor: Color,
    surfaceContainerHighColor: Color,
    outlineVariantColor: Color,
    onSurfaceColor: Color,
    onSurfaceVariantColor: Color,
    primaryColor: Color,
    onPrimaryColor: Color
): VideoCommentAppearance {
    return VideoCommentAppearance(
        panelColor = surfaceColor,
        dividerColor = outlineVariantColor,
        sectionDividerColor = surfaceContainerHighColor,
        primaryTextColor = onSurfaceColor,
        secondaryTextColor = onSurfaceVariantColor,
        accentColor = primaryColor,
        actionTint = onSurfaceVariantColor,
        sortTint = primaryColor.copy(alpha = 0.86f),
        auxiliaryTint = primaryColor.copy(alpha = 0.72f),
        placeholderColor = surfaceVariantColor,
        composerHintBackgroundColor = surfaceVariantColor.copy(alpha = 0.40f),
        toggleCheckedBackgroundColor = primaryColor,
        toggleCheckedContentColor = onPrimaryColor,
        toggleUncheckedBackgroundColor = surfaceVariantColor.copy(alpha = 0.50f),
        toggleUncheckedContentColor = onSurfaceVariantColor
    )
}

@Composable
internal fun rememberVideoCommentAppearance(): VideoCommentAppearance {
    val surfaceColor = AppSurfaceTokens.surface()
    val surfaceVariantColor = AppSurfaceTokens.surfaceContainer()
    val surfaceContainerHighColor = AppSurfaceTokens.surfaceContainerHigh()
    val outlineVariantColor = AppSurfaceTokens.outline()
    val onSurfaceColor = AppContentColors.primaryText()
    val onSurfaceVariantColor = AppContentColors.secondaryText()
    val primaryColor = AppContentColors.accent()
    val onPrimaryColor = AppContentColors.onAccent()
    return remember(
        surfaceColor,
        surfaceVariantColor,
        surfaceContainerHighColor,
        outlineVariantColor,
        onSurfaceColor,
        onSurfaceVariantColor,
        primaryColor,
        onPrimaryColor
    ) {
        resolveVideoCommentAppearance(
            surfaceColor = surfaceColor,
            surfaceVariantColor = surfaceVariantColor,
            surfaceContainerHighColor = surfaceContainerHighColor,
            outlineVariantColor = outlineVariantColor,
            onSurfaceColor = onSurfaceColor,
            onSurfaceVariantColor = onSurfaceVariantColor,
            primaryColor = primaryColor,
            onPrimaryColor = onPrimaryColor
        )
    }
}
