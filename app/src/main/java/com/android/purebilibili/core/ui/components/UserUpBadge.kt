package com.android.purebilibili.core.ui.components

import io.github.suqi8.coui.kmp.theme.COUITheme
import com.android.purebilibili.core.ui.AppContentColors

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal data class UserUpBadgeVisualSpec(
    val cornerRadiusDp: Int,
    val horizontalPaddingDp: Int,
    val verticalPaddingDp: Int
)

internal fun resolveUserUpBadgeVisualSpec(): UserUpBadgeVisualSpec {
    return UserUpBadgeVisualSpec(
        cornerRadiusDp = 3,
        horizontalPaddingDp = 3,
        verticalPaddingDp = 2
    )
}

@Composable
fun UserUpBadge(
    modifier: Modifier = Modifier,
    containerColor: Color = AppContentColors.accent(),
    contentColor: Color = AppContentColors.onAccent()
) {
    val spec = remember { resolveUserUpBadgeVisualSpec() }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(spec.cornerRadiusDp.dp))
            .background(containerColor)
            .padding(
                horizontal = spec.horizontalPaddingDp.dp,
                vertical = spec.verticalPaddingDp.dp
            ),
        contentAlignment = Alignment.Center,
    ) {
        AppText(
            text = "UP",
            style = COUITheme.textStyles.footnote2.copy(
                lineHeight = COUITheme.textStyles.footnote2.fontSize,
                platformStyle = PlatformTextStyle(includeFontPadding = false),
                lineHeightStyle = LineHeightStyle(
                    alignment = LineHeightStyle.Alignment.Center,
                    trim = LineHeightStyle.Trim.Both,
                ),
            ),
            maxLines = 1,
            softWrap = false,
            fontWeight = FontWeight.Bold,
            color = contentColor
        )
    }
}
