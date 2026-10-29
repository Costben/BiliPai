package com.android.purebilibili.core.ui.renderer.coui

import com.android.purebilibili.core.ui.AppContentColors

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.purebilibili.core.ui.AppChromeSizeTokens
import com.android.purebilibili.core.ui.AppSpacingTokens
import com.android.purebilibili.core.ui.isMiuixNonGlassEnabled
import com.android.purebilibili.core.ui.components.AppMiuixActionTone
import com.android.purebilibili.core.ui.components.appDesktopInteractionVisuals
import com.android.purebilibili.core.ui.components.resolveMiuixChipActionTone
import com.android.purebilibili.core.ui.components.resolveMiuixFabMinSizeDp
import com.android.purebilibili.core.ui.components.resolveMiuixNonGlassChipMetrics
import io.github.suqi8.coui.kmp.basic.BasicComponent
import io.github.suqi8.coui.kmp.basic.Button as CouiButton
import io.github.suqi8.coui.kmp.basic.ButtonColors as CouiButtonColors
import io.github.suqi8.coui.kmp.basic.ButtonDefaults as CouiButtonDefaults
import io.github.suqi8.coui.kmp.basic.FloatingActionButton as CouiFloatingActionButton
import io.github.suqi8.coui.kmp.basic.FloatingActionButtonDefaults as CouiFloatingActionButtonDefaults
import io.github.suqi8.coui.kmp.basic.Icon as CouiIcon
import io.github.suqi8.coui.kmp.basic.SnackbarDefaults as CouiSnackbarDefaults
import io.github.suqi8.coui.kmp.basic.Text as CouiText
import io.github.suqi8.coui.kmp.basic.TextButton as CouiTextButton
import io.github.suqi8.coui.kmp.icon.COUIIcons
import io.github.suqi8.coui.kmp.icon.basic.Close
import io.github.suqi8.coui.kmp.squircle.squircleBackground
import io.github.suqi8.coui.kmp.squircle.squircleBorder
import io.github.suqi8.coui.kmp.theme.LocalContentColor as CouiLocalContentColor
import io.github.suqi8.coui.kmp.theme.COUITheme

@Composable
internal fun AppCouiButton(
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    colors: CouiButtonColors,
    insideMargin: PaddingValues,
    interactionSource: MutableInteractionSource,
    border: BorderStroke? = null,
    content: @Composable RowScope.() -> Unit,
) {
    CouiButton(
        onClick = onClick,
        modifier = modifier.then(
            if (border == null) {
                Modifier
            } else {
                val brush = border.brush
                if (brush is SolidColor) {
                    Modifier.squircleBorder(
                        width = border.width,
                        color = brush.value,
                        cornerRadius = CouiButtonDefaults.CornerRadius,
                    )
                } else {
                    Modifier.border(border, RoundedCornerShape(CouiButtonDefaults.CornerRadius))
                }
            }
        ),
        enabled = enabled,
        colors = colors,
        insideMargin = insideMargin,
        minHeight = CouiButtonDefaults.MinHeight,
        minWidth = CouiButtonDefaults.MinWidth,
        cornerRadius = CouiButtonDefaults.CornerRadius,
        interactionSource = interactionSource,
        content = {
            // Slots can contain Material Text/Icon alongside native Miuix content.
            CompositionLocalProvider(
                androidx.compose.material3.LocalContentColor provides CouiLocalContentColor.current,
                com.android.purebilibili.core.ui.LocalAppButtonSlotPalette provides
                    com.android.purebilibili.core.ui.AppButtonSlotPalette(
                        containerColor = if (enabled) colors.color else colors.disabledColor,
                        contentColor = CouiLocalContentColor.current,
                        enabled = enabled,
                    ),
            ) {
                content()
            }
        },
    )
}

@Composable
internal fun AppCouiChip(
    onClick: () -> Unit,
    selected: Boolean,
    enabled: Boolean,
    modifier: Modifier,
    leadingIcon: (@Composable () -> Unit)?,
    trailingIcon: (@Composable () -> Unit)?,
    interactionSource: MutableInteractionSource?,
    navigationColors: Boolean = false,
    label: @Composable () -> Unit,
) {
    val colors = when (resolveMiuixChipActionTone(selected)) {
        AppMiuixActionTone.PRIMARY -> CouiButtonDefaults.buttonColorsPrimary()
        AppMiuixActionTone.SECONDARY -> CouiButtonDefaults.buttonColors()
    }
    val scheme = androidx.compose.material3.MaterialTheme.colorScheme
    val selectedContainer = colors.color
    val selectedContent = colors.contentColor
    val navigation = if (navigationColors && selected && isMiuixNonGlassEnabled()) {
        com.android.purebilibili.core.ui.AppInteractionColors.nativeNavigation()
    } else null
    val readableColors = if (navigation != null) CouiButtonColors(
        color = navigation.selectedContainer,
        contentColor = navigation.selectedContent,
        disabledColor = colors.disabledColor,
        disabledContentColor = colors.disabledContentColor,
    ) else if (isMiuixNonGlassEnabled()) colors else CouiButtonColors(
        color = selectedContainer,
        contentColor = com.android.purebilibili.core.ui.resolveButtonContentColor(selectedContainer, selectedContent, scheme),
        disabledColor = colors.disabledColor,
        disabledContentColor = colors.disabledContentColor,
    )
    val metrics = resolveMiuixNonGlassChipMetrics()
    CouiButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = readableColors,
        insideMargin = if (isMiuixNonGlassEnabled()) CouiButtonDefaults.InsideMargin else PaddingValues(
            horizontal = metrics.horizontalPaddingDp.dp,
            vertical = AppSpacingTokens.None,
        ),
        minHeight = if (isMiuixNonGlassEnabled()) CouiButtonDefaults.MinHeight else metrics.minHeightDp.dp,
        minWidth = if (isMiuixNonGlassEnabled()) CouiButtonDefaults.MinWidth else metrics.minWidthDp.dp,
        cornerRadius = if (isMiuixNonGlassEnabled()) CouiButtonDefaults.CornerRadius else metrics.cornerRadiusDp.dp,
        interactionSource = interactionSource,
    ) {
        CompositionLocalProvider(
            androidx.compose.material3.LocalContentColor provides CouiLocalContentColor.current,
        ) {
            if (leadingIcon != null) {
                leadingIcon()
                Spacer(modifier = Modifier.width(metrics.iconGapDp.dp))
            }
            label()
            if (trailingIcon != null) {
                Spacer(modifier = Modifier.width(metrics.iconGapDp.dp))
                trailingIcon()
            }
        }
    }
}

@Composable
internal fun AppCouiFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier,
    containerColor: Color,
    contentColor: Color,
    small: Boolean,
    content: @Composable () -> Unit,
) {
    val minSize = resolveMiuixFabMinSizeDp(small).dp
    CouiFloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        shape = CircleShape,
        containerColor = containerColor,
        shadowElevation = CouiFloatingActionButtonDefaults.ShadowElevation,
        minWidth = minSize,
        minHeight = minSize,
    ) {
        // Native FAB's internal Surface resets LocalContentColor; provide the pair inside it.
        CompositionLocalProvider(
            CouiLocalContentColor provides contentColor,
            androidx.compose.material3.LocalContentColor provides contentColor,
        ) {
            content()
        }
    }
}

@Composable
internal fun AppCouiSnackbar(
    modifier: Modifier,
    action: (@Composable () -> Unit)?,
    content: @Composable () -> Unit,
) {
    val colors = CouiSnackbarDefaults.snackbarColors()
    AppCouiSnackbarContainer(modifier = modifier, containerColor = colors.containerColor) {
        CompositionLocalProvider(
            CouiLocalContentColor provides colors.contentColor,
            androidx.compose.material3.LocalContentColor provides colors.contentColor,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .defaultMinSize(minHeight = AppChromeSizeTokens.MinimumTouchTarget)
                    .padding(CouiSnackbarDefaults.InsideMargin),
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    content()
                }
                if (action != null) {
                    Spacer(modifier = Modifier.width(AppSpacingTokens.Medium))
                    action()
                }
            }
        }
    }
}

@Composable
internal fun AppCouiMessageSnackbar(
    message: String,
    actionLabel: String?,
    onAction: (() -> Unit)?,
    withDismissAction: Boolean,
    onDismiss: (() -> Unit)?,
    modifier: Modifier,
) {
    val colors = CouiSnackbarDefaults.snackbarColors()
    AppCouiSnackbarContainer(modifier = modifier, containerColor = colors.containerColor) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .defaultMinSize(minHeight = AppChromeSizeTokens.MinimumTouchTarget)
                .padding(CouiSnackbarDefaults.InsideMargin),
        ) {
            CouiText(
                text = message,
                color = colors.contentColor,
                style = COUITheme.textStyles.body2,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            if (!actionLabel.isNullOrEmpty() && onAction != null) {
                CouiTextButton(
                    text = actionLabel,
                    onClick = onAction,
                    modifier = Modifier.padding(start = AppSpacingTokens.Medium),
                    cornerRadius = CouiSnackbarActionCornerRadius,
                    minWidth = 26.dp,
                    minHeight = 26.dp,
                    colors = CouiButtonDefaults.textButtonColorsPrimary(
                        color = COUITheme.colorScheme.primary,
                        textColor = colors.actionContentColor,
                    ),
                    insideMargin = CouiSnackbarActionInsideMargin,
                    textStyle = COUITheme.textStyles.button,
                )
            }
            if (withDismissAction && onDismiss != null) {
                CouiIcon(
                    imageVector = COUIIcons.Basic.Close,
                    contentDescription = "关闭",
                    tint = colors.dismissActionContentColor,
                    modifier = Modifier
                        .padding(start = AppSpacingTokens.Small)
                        .size(20.dp)
                        .clickable(
                            indication = null,
                            interactionSource = null,
                            onClick = onDismiss,
                        ),
                )
            }
        }
    }
}

@Composable
internal fun AppCouiNavigationDrawerItem(
    label: @Composable () -> Unit,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier,
    icon: (@Composable () -> Unit)?,
    badge: (@Composable () -> Unit)?,
    interactionSource: MutableInteractionSource,
) {
    val navigationColors = com.android.purebilibili.core.ui.AppInteractionColors.nativeNavigation()
    val selectedContent = navigationColors?.selectedContent ?: COUITheme.colorScheme.onPrimaryContainer
    val itemModifier = if (selected) modifier.squircleBackground(
        color = navigationColors?.selectedContainer ?: COUITheme.colorScheme.primaryContainer,
        cornerRadius = CouiButtonDefaults.CornerRadius,
    ) else modifier
    BasicComponent(
        modifier = itemModifier.appDesktopInteractionVisuals(interactionSource),
        startAction = icon?.let { action ->
            {
                com.android.purebilibili.core.ui.components.ProvideAppActionContentColor(
                    if (selected) selectedContent else AppContentColors.secondaryText(),
                    action,
                )
            }
        },
        endActions = badge?.let { slot -> { slot() } },
        onClick = onClick,
        holdDownState = selected,
        interactionSource = interactionSource,
    ) {
        com.android.purebilibili.core.ui.components.ProvideAppActionContentColor(
            if (selected) selectedContent else AppContentColors.primaryText(),
            label,
        )
    }
}

@Composable
private fun AppCouiSnackbarContainer(
    modifier: Modifier,
    containerColor: Color,
    content: @Composable () -> Unit,
) {
    val cornerRadius = CouiSnackbarDefaults.CornerRadius
    Box(
        modifier = modifier
            .semantics(mergeDescendants = false) {
                isTraversalGroup = true
                liveRegion = LiveRegionMode.Polite
            }
            .padding(CouiSnackbarOuterPadding)
            .dropShadow(
                shape = RoundedCornerShape(cornerRadius),
                shadow = Shadow(
                    radius = 10.dp,
                    color = Color.Black,
                    alpha = 0.1f,
                ),
            )
            .squircleBackground(color = containerColor, cornerRadius = cornerRadius),
    ) {
        content()
    }
}

// COUI 未公开 Snackbar action 与浮层外边距的 token，这里按 miuix 同角色取值补齐。
private val CouiSnackbarActionCornerRadius = 50.dp
private val CouiSnackbarActionInsideMargin = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
private val CouiSnackbarOuterPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp)
