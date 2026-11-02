package com.android.purebilibili.core.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.android.purebilibili.core.theme.AppUiStyle
import com.android.purebilibili.core.theme.LocalAppUiStyle
import com.android.purebilibili.core.ui.currentAppContentColor
import io.github.suqi8.coui.kmp.basic.HighlightedIconButton
import io.github.suqi8.coui.kmp.basic.HighlightedIconButtonDefaults
import io.github.suqi8.coui.kmp.icon.COUIIcons
import io.github.suqi8.coui.kmp.icon.extended.More
import io.github.suqi8.coui.kmp.theme.COUITheme
import io.github.suqi8.coui.kmp.theme.darkColorScheme
import io.github.suqi8.coui.kmp.theme.LocalContentColor as CouiLocalContentColor
import io.github.suqi8.coui.kmp.utils.rememberGalleryDensity

/**
 * COUI 17 的光影图标按钮：40dp 圆形径向填充、边缘描边与外侧光晕，按下整体缩放到 0.89 并提亮。
 *
 * [forceDark] 用于压在视频/封面上的控件：圆盘固定用深色主题，
 * 否则浅色皮肤下会画出一块近白圆盘。
 *
 * 其它皮肤退回普通图标按钮，调用方不必分叉。
 */
@Composable
fun CouiHighlightedIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentDescription: String? = null,
    size: Dp = HighlightedIconButtonDefaults.Size,
    forceDark: Boolean = false,
    content: @Composable BoxScope.() -> Unit,
) {
    if (LocalAppUiStyle.current != AppUiStyle.COUI17) {
        AppIconButton(onClick = onClick, modifier = modifier, enabled = enabled) {
            Box(contentAlignment = Alignment.Center) { content() }
        }
        return
    }
    val click = appActionWithFeedback(onClick)
    val galleryDensity = rememberGalleryDensity()
    CouiDiscTheme(forceDark) {
        val scheme = COUITheme.colorScheme
        val discIsDark = scheme.background.luminance() < 0.5f
        // 暗色下 onBackground 有时仍是深色（壁纸取色/自定义种子），圆盘于是变成「全黑」。
        // 这里按圆底亮度兜底一次，保证图标和它所在的那块底色永远有对比。
        val onDisc = scheme.onBackground
        val contentColor = if ((onDisc.luminance() > 0.5f) == discIsDark) {
            onDisc
        } else if (discIsDark) {
            Color.White
        } else {
            Color.Black
        }
        CompositionLocalProvider(LocalDensity provides galleryDensity) {
            HighlightedIconButton(
                onClick = click,
                modifier = if (contentDescription == null) {
                    modifier
                } else {
                    modifier.semantics { this.contentDescription = contentDescription }
                },
                size = size,
                enabled = enabled,
            ) {
                CompositionLocalProvider(
                    CouiLocalContentColor provides contentColor,
                    androidx.compose.material3.LocalContentColor provides contentColor,
                    LocalAppActionContentColor provides contentColor,
                ) {
                    content()
                }
            }
        }
    }
}

/** COUI 17 的三点按钮，见 [CouiHighlightedIconButton]。 */
@Composable
fun CouiMoreButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentDescription: String? = null,
    tint: Color = Color.Unspecified,
    size: Dp = HighlightedIconButtonDefaults.Size,
    iconSize: Dp = 24.dp,
    forceDark: Boolean = false,
) = CouiHighlightedIconButton(
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
    contentDescription = contentDescription,
    size = size,
    forceDark = forceDark,
) {
    CouiMoreGlyph(tint = tint, iconSize = iconSize)
}

/**
 * 只有视觉的三点圆盘，不含点击与按压反馈。
 *
 * 用于外层已经承担手势的场景（按钮锚点、玻璃菜单源、自绘触控区），
 * 避免和外层 clickable 抢事件。
 */
@Composable
fun CouiMoreDisc(
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified,
    size: Dp = HighlightedIconButtonDefaults.Size,
    iconSize: Dp = 24.dp,
    forceDark: Boolean = false,
) {
    if (LocalAppUiStyle.current != AppUiStyle.COUI17) {
        AppIcon(
            imageVector = COUIIcons.More,
            contentDescription = null,
            tint = tint,
            modifier = modifier.size(iconSize),
        )
        return
    }
    val galleryDensity = rememberGalleryDensity()
    CouiDiscTheme(forceDark) {
        CompositionLocalProvider(LocalDensity provides galleryDensity) {
            HighlightedIconButton(
                onClick = {},
                modifier = modifier.size(size),
                size = size,
                enabled = false,
            ) {
                CouiMoreGlyph(tint = tint, iconSize = iconSize)
            }
        }
    }
}

@Composable
private fun CouiDiscTheme(forceDark: Boolean, content: @Composable () -> Unit) {
    if (forceDark) {
        COUITheme(colors = darkColorScheme(), content = content)
    } else {
        content()
    }
}

@Composable
private fun CouiMoreGlyph(tint: Color, iconSize: Dp) {
    AppIcon(
        imageVector = COUIIcons.More,
        contentDescription = null,
        tint = tint.takeOrElse { currentAppContentColor() },
        modifier = Modifier.size(iconSize),
    )
}
