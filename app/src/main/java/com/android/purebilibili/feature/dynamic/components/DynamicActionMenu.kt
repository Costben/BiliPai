package com.android.purebilibili.feature.dynamic.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.android.purebilibili.core.ui.AppAlertDialog
import com.android.purebilibili.core.ui.AppContentColors
import com.android.purebilibili.core.ui.AppDialogAction
import com.android.purebilibili.core.ui.AppShapes
import com.android.purebilibili.core.ui.AppSpacingTokens
import com.android.purebilibili.core.ui.ContainerLevel
import com.android.purebilibili.core.ui.components.AppIcon
import com.android.purebilibili.core.ui.components.AppText
import com.android.purebilibili.core.ui.components.AppWindowAction
import com.android.purebilibili.core.ui.components.AppWindowActionMenu
import com.android.purebilibili.core.ui.components.appActionClickable
import com.android.purebilibili.core.ui.rememberAppChevronUpIcon
import com.android.purebilibili.core.ui.rememberAppCommentIcon
import com.android.purebilibili.core.ui.rememberAppDeleteIcon
import com.android.purebilibili.core.ui.rememberAppHistoryIcon
import com.android.purebilibili.core.ui.rememberAppLinkIcon
import com.android.purebilibili.core.ui.rememberAppShareIcon
import com.android.purebilibili.core.ui.rememberAppVisibilityOffIcon
import com.android.purebilibili.core.ui.rememberAppVisibilityOnIcon
import com.android.purebilibili.core.ui.rememberAppWarningIcon
import com.android.purebilibili.data.model.response.DynamicItem
import com.android.purebilibili.data.model.response.ReplyInteractionData
import com.android.purebilibili.data.repository.DynamicRepository
import com.android.purebilibili.feature.dynamic.DynamicDeleteAction
import com.android.purebilibili.feature.dynamic.resolveDynamicDeleteAction

/**
 * 动态的「更多」菜单：菜单项、二次确认弹窗和分享面板全部内聚在这里。
 *
 * 信息流卡片把它挂在作者行右侧，详情页把它挂在顶栏，两边共用同一套菜单与弹窗，
 * 详情页因此可以只保留顶栏那一个三点入口。
 */
@Composable
fun DynamicActionMenu(
    item: DynamicItem,
    modifier: Modifier = Modifier,
    currentUserMid: Long? = null,
    onManageAction: (DynamicManageAction) -> Unit = {},
    onDeleteClick: ((DynamicDeleteAction) -> Unit)? = null,
    onWatchLaterClick: ((Long) -> Unit)? = null,
    onShareToMessageClick: (() -> Unit)? = null,
    onCheckDynamicClick: (() -> Unit)? = null,
    onLoadReplyInteractionStatus: ((Long, Int, (ReplyInteractionData?) -> Unit) -> Unit)? = null,
    /** 追加到第一组的展示类操作，例如详情页的「切换图片展示」。 */
    extraViewActions: List<AppWindowAction> = emptyList(),
    moreTint: Color = Color.Unspecified,
    moreSize: Dp? = null,
    moreIconSize: Dp? = null,
    moreDescription: String = "更多",
    onExpandedChange: ((Boolean) -> Unit)? = null,
) {
    val context = LocalContext.current
    val content = item.modules.module_dynamic
    val author = item.modules.module_author
    val watchLaterAid = remember(item) { resolveDynamicWatchLaterAid(item) }
    val deleteAction = remember(item) { resolveDynamicDeleteAction(item) }
    val menuCapabilities = remember(item, currentUserMid) {
        resolveDynamicMenuCapabilities(item, currentUserMid)
    }
    val isCurrentlyTop = item.modules.module_tag?.text == "置顶"

    var shareImagePreview by remember(item.id_str) { mutableStateOf(false) }
    var pendingDeleteAction by remember(item.id_str) { mutableStateOf<DynamicDeleteAction?>(null) }
    var pendingBlockAuthor by remember(item.id_str) {
        mutableStateOf<DynamicManageAction.BlockAuthor?>(null)
    }
    var showReplyInteractionDialog by remember(item.id_str) {
        mutableStateOf<ReplyInteractionData?>(null)
    }
    var replyInteractionOid by remember(item.id_str) { mutableStateOf(0L) }
    var replyInteractionType by remember(item.id_str) { mutableStateOf(0) }

    if (shareImagePreview) {
        com.android.purebilibili.feature.share.ShareImagePreviewSheet(
            document = com.android.purebilibili.feature.share.dynamicShareDocument(item),
            loadDocument = {
                val complete = DynamicRepository.getDynamicDetail(item.id_str).getOrThrow()
                com.android.purebilibili.feature.share.dynamicShareDocument(complete)
            },
            onDismiss = { shareImagePreview = false },
        )
    }

    pendingDeleteAction?.let { action ->
        AppAlertDialog(
            onDismissRequest = { pendingDeleteAction = null },
            title = { AppText(action.title) },
            text = { AppText(action.content) },
            confirmButton = {
                AppDialogAction(
                    onClick = {
                        pendingDeleteAction = null
                        onDeleteClick?.invoke(action)
                    }
                ) {
                    AppText(action.confirmText, color = AppContentColors.error())
                }
            },
            dismissButton = {
                AppDialogAction(onClick = { pendingDeleteAction = null }) {
                    AppText(action.cancelText)
                }
            }
        )
    }

    pendingBlockAuthor?.let { action ->
        AppAlertDialog(
            onDismissRequest = { pendingBlockAuthor = null },
            title = { AppText("屏蔽 UP 主") },
            text = { AppText("屏蔽后将不再推荐 ${action.authorName} 的内容，并同步到哔哩哔哩黑名单。") },
            confirmButton = {
                AppDialogAction(
                    onClick = {
                        pendingBlockAuthor = null
                        onManageAction(action)
                    },
                ) { AppText("屏蔽", color = AppContentColors.error()) }
            },
            dismissButton = {
                AppDialogAction(onClick = { pendingBlockAuthor = null }) { AppText("取消") }
            },
        )
    }

    showReplyInteractionDialog?.let { interactionData ->
        val selection = interactionData.up_reply_selection
        val reply = interactionData.up_reply
        val selectionEnabled = selection?.status == 1
        val replyEnabled = reply?.status == 1
        AppAlertDialog(
            onDismissRequest = { showReplyInteractionDialog = null },
            title = { AppText("评论互动设置") },
            text = {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(AppShapes.container(ContainerLevel.Chip))
                            .appActionClickable(enabled = selection?.can_modify == true) {
                                showReplyInteractionDialog = null
                                onManageAction(
                                    DynamicManageAction.SetReplySubject(
                                        oid = replyInteractionOid,
                                        replyType = replyInteractionType,
                                        action = resolveDynamicReplySelectionAction(selectionEnabled)
                                    )
                                )
                            }
                            .padding(vertical = AppSpacingTokens.Medium),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppIcon(
                            rememberAppCommentIcon(),
                            contentDescription = null,
                            modifier = Modifier.size(AppSpacingTokens.Large),
                            tint = AppContentColors.primaryText()
                        )
                        Spacer(modifier = Modifier.width(AppSpacingTokens.Small))
                        AppText(
                            if (selectionEnabled) "停止评论精选" else "开启评论精选",
                            color = if (selection?.can_modify == true) {
                                AppContentColors.primaryText()
                            } else {
                                AppContentColors.secondaryText().copy(0.5f)
                            }
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(AppShapes.container(ContainerLevel.Chip))
                            .appActionClickable(enabled = reply?.can_modify == true) {
                                showReplyInteractionDialog = null
                                onManageAction(
                                    DynamicManageAction.SetReplySubject(
                                        oid = replyInteractionOid,
                                        replyType = replyInteractionType,
                                        action = resolveDynamicReplyOpenAction(replyEnabled)
                                    )
                                )
                            }
                            .padding(vertical = AppSpacingTokens.Medium),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppIcon(
                            rememberAppVisibilityOffIcon(),
                            contentDescription = null,
                            modifier = Modifier.size(AppSpacingTokens.Large),
                            tint = AppContentColors.primaryText()
                        )
                        Spacer(modifier = Modifier.width(AppSpacingTokens.Small))
                        AppText(
                            if (replyEnabled) "关闭评论" else "恢复评论",
                            color = if (reply?.can_modify == true) {
                                AppContentColors.primaryText()
                            } else {
                                AppContentColors.secondaryText().copy(0.5f)
                            }
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                AppDialogAction(onClick = { showReplyInteractionDialog = null }) {
                    AppText("取消")
                }
            }
        )
    }

    val linkIcon = rememberAppLinkIcon()
    val shareIcon = rememberAppShareIcon()
    val historyIcon = rememberAppHistoryIcon()
    val deleteIcon = rememberAppDeleteIcon()
    val visibilityOffIcon = rememberAppVisibilityOffIcon()
    val chevronUpIcon = rememberAppChevronUpIcon()
    val visibilityOnIcon = rememberAppVisibilityOnIcon()
    val commentIcon = rememberAppCommentIcon()
    val warningIcon = rememberAppWarningIcon()
    val errorTint = AppContentColors.error()

    AppWindowActionMenu(
        modifier = modifier,
        groups = listOf(
            buildList {
                add(
                    AppWindowAction(
                        label = "复制链接",
                        icon = linkIcon,
                        onClick = {
                            val dynamicUrl = "https://t.bilibili.com/${item.id_str}"
                            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE)
                                as android.content.ClipboardManager
                            clipboard.setPrimaryClip(
                                android.content.ClipData.newPlainText("动态链接", dynamicUrl)
                            )
                            android.widget.Toast.makeText(
                                context,
                                "已复制链接",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        },
                    )
                )
                add(
                    AppWindowAction(
                        label = "分享动态",
                        icon = shareIcon,
                        onClick = {
                            val dynamicUrl = "https://t.bilibili.com/${item.id_str}"
                            val descText = content?.desc?.text?.take(100).orEmpty()
                            val shareText = if (descText.isNotBlank()) {
                                "$descText\n$dynamicUrl"
                            } else {
                                "分享动态\n$dynamicUrl"
                            }
                            val shareIntent = android.content.Intent(
                                android.content.Intent.ACTION_SEND
                            ).apply {
                                this.type = "text/plain"
                                putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                            }
                            context.startActivity(
                                android.content.Intent.createChooser(shareIntent, "分享动态")
                            )
                        },
                    )
                )
                if (watchLaterAid != null && onWatchLaterClick != null) {
                    add(
                        AppWindowAction(
                            label = "稍后再看",
                            icon = historyIcon,
                            onClick = { onWatchLaterClick(watchLaterAid) },
                        )
                    )
                }
                add(
                    AppWindowAction(
                        label = "存图 / 分享图片",
                        icon = historyIcon,
                        onClick = { shareImagePreview = true },
                    )
                )
                val canShareToMessage = item.basic?.comment_type in setOf(11, 17) &&
                    item.modules.module_author?.mid != null
                if (onShareToMessageClick != null && canShareToMessage) {
                    add(
                        AppWindowAction(
                            label = "分享至消息",
                            icon = shareIcon,
                            onClick = onShareToMessageClick,
                        )
                    )
                }
                if (onCheckDynamicClick != null && menuCapabilities.isOwnDynamic) {
                    add(
                        AppWindowAction(
                            label = "检查动态",
                            icon = warningIcon,
                            onClick = onCheckDynamicClick,
                        )
                    )
                }
                addAll(extraViewActions)
            },
            buildList {
                if (!menuCapabilities.isOwnDynamic) {
                    add(
                        AppWindowAction(
                            label = "不感兴趣",
                            icon = visibilityOffIcon,
                            onClick = {
                                onManageAction(DynamicManageAction.NotInterested(item.id_str))
                            },
                        )
                    )
                }
                if (menuCapabilities.canToggleTop) {
                    add(
                        AppWindowAction(
                            label = resolveDynamicPinnedMenuLabel(isCurrentlyTop),
                            icon = chevronUpIcon,
                            onClick = {
                                onManageAction(
                                    DynamicManageAction.ToggleTop(item.id_str, isCurrentlyTop)
                                )
                            },
                        )
                    )
                }
                if (menuCapabilities.canSetVisibility) {
                    add(
                        AppWindowAction(
                            label = resolveDynamicVisibilityMenuLabel(
                                isPrivate = menuCapabilities.isPrivate
                            ),
                            icon = visibilityOnIcon,
                            onClick = {
                                onManageAction(
                                    DynamicManageAction.SetVisibility(
                                        dynamicId = item.id_str,
                                        dynType = resolveDynamicDynType(item),
                                        isPrivate = !menuCapabilities.isPrivate
                                    )
                                )
                            },
                        )
                    )
                }
                if (menuCapabilities.canManageComments) {
                    add(
                        AppWindowAction(
                            label = "评论互动设置",
                            icon = commentIcon,
                            onClick = {
                                val oid = resolveDynamicReplySubjectOid(item)
                                if (oid == null || onLoadReplyInteractionStatus == null) {
                                    android.widget.Toast.makeText(
                                        context,
                                        "该动态暂不支持互动设置",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    onLoadReplyInteractionStatus(
                                        oid,
                                        resolveDynamicReplySubjectType(item)
                                    ) { data ->
                                        if (data == null) {
                                            android.widget.Toast.makeText(
                                                context,
                                                "获取互动设置失败",
                                                android.widget.Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            replyInteractionOid = oid
                                            replyInteractionType = resolveDynamicReplySubjectType(item)
                                            showReplyInteractionDialog = data
                                        }
                                    }
                                }
                            },
                        )
                    )
                }
                if (menuCapabilities.canBlockAuthor) {
                    add(
                        AppWindowAction(
                            label = "屏蔽该 UP 主",
                            icon = visibilityOffIcon,
                            onClick = {
                                pendingBlockAuthor = DynamicManageAction.BlockAuthor(
                                    authorMid = author?.mid ?: 0L,
                                    authorName = author?.name.orEmpty().ifBlank { "该用户" },
                                    authorFace = author?.face.orEmpty(),
                                )
                            },
                        )
                    )
                }
            },
            buildList {
                if (menuCapabilities.canEdit) {
                    add(
                        AppWindowAction(
                            label = "编辑动态",
                            onClick = {
                                onManageAction(
                                    DynamicManageAction.Edit(
                                        dynamicId = item.id_str,
                                        initialDraft = resolveDynamicEditDraft(item),
                                        replyOid = resolveDynamicReplySubjectOid(item),
                                        replyType = resolveDynamicReplySubjectType(item),
                                    )
                                )
                            },
                        )
                    )
                }
                if (deleteAction != null && onDeleteClick != null) {
                    add(
                        AppWindowAction(
                            label = deleteAction.label,
                            icon = deleteIcon,
                            iconTint = errorTint,
                            onClick = { pendingDeleteAction = deleteAction },
                        )
                    )
                }
                if (menuCapabilities.canReport) {
                    add(
                        AppWindowAction(
                            label = "举报",
                            icon = warningIcon,
                            iconTint = errorTint,
                            onClick = {
                                onManageAction(
                                    DynamicManageAction.Report(
                                        dynamicId = item.id_str,
                                        authorMid = author?.mid ?: 0L
                                    )
                                )
                            },
                        )
                    )
                }
            },
        ),
        couiMore = true,
        moreTint = moreTint,
        moreSize = moreSize,
        moreIconSize = moreIconSize,
        moreDescription = moreDescription,
        onExpandedChange = onExpandedChange,
    )
}


