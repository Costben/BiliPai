package com.android.purebilibili.feature.download

import io.github.suqi8.coui.kmp.theme.COUITheme
import com.android.purebilibili.core.ui.AppIcons

import com.android.purebilibili.core.ui.AppContentColors
import com.android.purebilibili.core.ui.components.AppIcon
import com.android.purebilibili.core.ui.AppSurfaceTokens
import com.android.purebilibili.core.ui.components.AppSingleChoiceRow
import com.android.purebilibili.core.ui.components.AppText
import com.android.purebilibili.core.ui.AppSpacingTokens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import com.android.purebilibili.core.ui.components.AppLazyColumn as LazyColumn
import androidx.compose.foundation.lazy.items
//  Material Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.android.purebilibili.core.ui.appContentDialogWidth
import com.android.purebilibili.core.ui.components.AppCheckbox
import com.android.purebilibili.core.ui.components.AppIconButton
import com.android.purebilibili.core.ui.components.AppOutlinedButton
import com.android.purebilibili.core.ui.components.AppSurface
import com.android.purebilibili.core.ui.resolveAppContentDialogLayoutPolicy
import com.android.purebilibili.core.ui.resolveAppContentDialogProperties
import com.android.purebilibili.core.ui.AppShapes
import com.android.purebilibili.core.ui.AppPopupSurface
import com.android.purebilibili.core.ui.AppPopupSurfaceType
import com.android.purebilibili.core.ui.ContainerLevel

/**
 *  下载画质选择对话框
 */
@Composable
fun DownloadQualityDialog(
    title: String,
    qualityOptions: List<Pair<Int, String>>,  // (qualityId, qualityLabel)
    currentQuality: Int,
    onQualitySelected: (Int, DownloadOptions) -> Unit,
    onDismiss: () -> Unit
) {
    var includeDanmaku by remember { mutableStateOf(true) }
    val dialogLayout = remember { resolveAppContentDialogLayoutPolicy(maxWidthDp = 420) }
    Dialog(
        onDismissRequest = onDismiss,
        properties = resolveAppContentDialogProperties(
            usePlatformDefaultWidth = dialogLayout.usePlatformDefaultWidth,
        ),
    ) {
        com.android.purebilibili.core.ui.ModalWindowBlurBehindEffect()
        AppPopupSurface(
            type = AppPopupSurfaceType.DIALOG,
            modifier = Modifier.appContentDialogWidth(policy = dialogLayout),
            shape = AppShapes.container(ContainerLevel.Card),
            containerColor = AppSurfaceTokens.surface(),
        ) {
            Column(modifier = Modifier.padding(AppSpacingTokens.Large)) {
                // 标题行
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppText(
                        text = "选择下载画质",
                        style = COUITheme.textStyles.headline1,
                        fontWeight = FontWeight.Bold,
                        color = AppContentColors.primaryText()
                    )
                    AppIconButton(onClick = onDismiss) {
                        AppIcon(
                            imageVector = AppIcons.Outlined.Close,
                            contentDescription = "取消",
                            tint = AppContentColors.secondaryText()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(AppSpacingTokens.Small))

                // 视频标题
                AppText(
                    text = title,
                    style = COUITheme.textStyles.body1,
                    color = AppContentColors.secondaryText(),
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(AppSpacingTokens.Large))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(AppShapes.container(ContainerLevel.Chip))
                        .clickable { includeDanmaku = !includeDanmaku }
                        .padding(vertical = AppSpacingTokens.Small),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppCheckbox(
                        checked = includeDanmaku,
                        onCheckedChange = { includeDanmaku = it }
                    )
                    AppText(
                        text = "同时缓存弹幕",
                        style = COUITheme.textStyles.body1,
                        color = AppContentColors.primaryText()
                    )
                }

                Spacer(modifier = Modifier.height(AppSpacingTokens.Small))

                // 画质列表
                qualityOptions.forEach { (qualityId, qualityLabel) ->
                    val isSelected = qualityId == currentQuality
                    val isVip = qualityLabel.contains("4K") || qualityLabel.contains("HDR") || qualityLabel.contains("杜比")

                    AppSingleChoiceRow(
                        selected = isSelected,
                        onClick = {
                            onQualitySelected(qualityId, DownloadOptions(includeDanmaku = includeDanmaku))
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AppText(
                                text = qualityLabel,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = AppSurfaceTokens.onSurfaceContainerHigh()
                            )
                            if (isVip) {
                                Spacer(modifier = Modifier.width(AppSpacingTokens.Small))
                                AppSurface(
                                    color = MaterialTheme.colorScheme.tertiary,
                                    shape = AppShapes.container(ContainerLevel.Tag)
                                ) {
                                    AppText(
                                        text = "VIP",
                                        style = COUITheme.textStyles.footnote2,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onTertiary,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(AppSpacingTokens.Small))
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 取消按钮
                AppOutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = AppShapes.container(ContainerLevel.Chip)
                ) {
                    AppText("取消")
                }
            }
        }
    }
}
