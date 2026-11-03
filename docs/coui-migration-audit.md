# COUI 化审计

对照 COUI 17 目标，逐个可感知界面找出仍按 Material 3 / Miuix 渲染或组织的地方。本文是审计结论，改造按第 9 节的批次执行。

审计基线：`main` @ `26d7709bc6`，工作树含 4 个未提交改动（动态详情与评论输入）。扫描覆盖 `app/` 全部 1863 个源文件，其中 726 个 Compose 文件。

## 1 结论摘要

全库有三层债务，量级差了两个数量级：

| 层 | 含义 | 命中 |
| --- | --- | ---: |
| L1 组件直连 | 直接调用 M3 可见组件，没走 `App*` 门面 | 172 处 / 43 个文件 |
| L2 令牌直连 | 直接读 `MaterialTheme` 字阶配色形状，或写死颜色圆角 | 3380 处 |
| L3 版式范式 | 组件令牌都对了，但页面结构还是旧皮肤的组织方式 | 23 处结构指纹 + 视觉抽查 8 条 |

关键背景：**L2 的 3380 处里，`MaterialTheme.typography` 有 2113 处，而 `resolveMaterialTypography()` 对三种皮肤都返回 `Md3Typography`**（`AndroidNativeVariantThemePolicy.kt:83`）。也就是说在 COUI17 下，只要写了 `AppText(style = MaterialTheme.typography.x)`，字号、行高、字重仍然是 MD3 的。这是「看起来还是 Material」最主要的来源，比 L1 影响面大得多。

L1 的 172 处里，有 44 处只存在于已冻结的 MD3 渲染分支（MD3/MIUIX 存档才会走到），COUI17 用户看不到：

| 文件 | L1 | 冻结原因 |
| --- | ---: | --- |
| `feature/settings/update/Material3AppUpdateDialog.kt` | 27 | `AppUpdateDialogHost.kt:88` 只在 `AppUiStyle.MATERIAL3` 调用；COUI17 走 `MiuixAppUpdateDialog`，后者已经是纯 COUI 实现 |
| `feature/home/components/HomeTopTabFloatingDock.kt` | 5 | 命中位于 `AppMaterial3Toolbar` 分支，COUI17 走胶囊标签行 |
| `feature/home/components/BottomBar.kt` | 5 | 同上，`AppMaterial3Toolbar` 分支 |
| `feature/settings/share/MaterialSettingsTransferActions.kt` | 4 | `SettingsShareScreen.kt:191` 只在 `MATERIAL3` 调用 |
| `feature/home/components/MaterialSideBar.kt` | 2 | `SideBar.kt:129` 只在 `MATERIAL3` + 未开玻璃时调用 |
| `feature/settings/share/SettingsShareScreen.kt` | 1 | `ListItem` 在 `MATERIAL3` 分支内（`SettingsShareScreen.kt:261`） |

**真正会在 COUI17 里渲染成 Material 的 L1 是 128 处**，其中 58 处集中在播放器。

## 2 判定口径

### 2.1 三层

**L1 组件直连**：直接 `import androidx.compose.material3.Text` 之类，或写全限定名调用。别名 import（`AppText as Text`）不算，统计已做 alias 解析。

**L2 令牌直连**：`MaterialTheme.typography` / `MaterialTheme.colorScheme` / `MaterialTheme.shapes`、`Color(0x…)` 字面量、`RoundedCornerShape(n.dp)` 直接写死。

**L3 版式范式**：结构指纹（通栏 `HorizontalDivider`、`AlertDialog`、`DropdownMenu`、`NavigationRail`、`Scaffold`）加上真机截图的视觉判断。

### 2.2 冻结分支不算债务

`AppUiStyle` 三值里有 `MATERIAL3` 和 `MIUIX`。这两个存档不会再进新用户，所以落在它们分支里的 M3 代码属于「皮肤冻结」范围，本轮不动，也不算进 COUI17 的实际欠账。上一节已把这 44 处单列。

一个容易误判的现象：`MiuixAppUpdateDialog.kt` 名字里带 Miuix，内容却是 `io.github.suqi8.coui.kmp.*` 的纯 COUI 实现——COUI 就是从 Miuix fork 出来的，命名遗留了。审计里不能按文件名判皮肤。

### 2.3 一个必须点名的例外

`PlayerNativeSettings.kt:34` 的 `PlayerNativeMenuTheme` 会强制 `LocalAppUiStyle provides AppUiStyle.MATERIAL3`：

```kotlin
internal fun PlayerNativeMenuTheme(content: @Composable () -> Unit) {
    val config = LocalAppThemeConfig.current
    CompositionLocalProvider(
        LocalAppThemeConfig provides playerNativeMenuConfig(config),
        LocalAppUiStyle provides AppUiStyle.MATERIAL3,
        content = content,
    )
}
```

它包住了播放器设置面板、弹幕设置面板、播放信息对话框、竖屏互动区，一共 9 个调用点。**这意味着 COUI17 下整个播放器设置体系被钉死在 Material 3**，不只是零散的组件残留。第 5 节据此单独成组。

### 2.4 COUI 目标

参照物两套：`coui/component-catalog/shots/demo/` 的组件 demo，和 `shots/system/` 的 ColorOS 系统应用截图。判定差异时以这两套为准，不自创设计。

已复制到 `docs/images/coui-audit/reference/`，可直接对照。

## 3 全库总量

| 指标 | 数量 |
| --- | ---: |
| Compose 文件 | 726 |
| 可感知界面 | 68 |
| `MaterialTheme.typography` | 2113 |
| `MaterialTheme.colorScheme` | 885 |
| `Color(0x…)` 字面量 | 177 |
| `RoundedCornerShape(` | 205 |
| `AppUiStyle.MATERIAL3` 分支 | 91 处 / 101 个文件含皮肤分支 |
| `AppUiStyle.COUI17` 显式分支 | 46 |

## 4 页面总表

「L1 生效」= COUI17 下真的会渲染成 Material 的组件数；「L1 冻结」= 只在 MD3/MIUIX 分支里的数。「字号」= `MaterialTheme.typography` 处数，「配色」= `MaterialTheme.colorScheme` 处数，「写死色」= `Color(0x…)` 处数，「圆角」= `RoundedCornerShape(` 处数，「结构」= 结构指纹处数。

结论列：**A** 已 COUI 化 / **B** 局部补 / **C** 整体重做 / **D** 只有冻结分支。

| 界面 | 类别·优先级 | 文件 | L1 生效 | L1 冻结 | 字号 | 配色 | 写死色 | 圆角 | 结构 | 结论 |
| --- | --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | :--: |
| 首页信息流 | 一级 P0 | 49 | 2 | 12 | 81 | 45 | 5 | 15 | 1 | C |
| 动态 | 一级 P0 | 36 | 1 | 0 | 105 | 31 | 0 | 11 | 0 | B |
| 动态详情 | 二级 P0 | 1 | 0 | 0 | 0 | 3 | 0 | 0 | 0 | A |
| 搜索 | 一级 P0 | 6 | 0 | 0 | 55 | 46 | 0 | 0 | 0 | B |
| 我的 | 一级 P0 | 15 | 0 | 0 | 108 | 75 | 1 | 4 | 0 | C |
| 设置首页 | 一级 P0 | 4 | 0 | 0 | 28 | 2 | 0 | 0 | 2 | B |
| 视频详情 | 一级 P0 | 18 | 4 | 0 | 94 | 48 | 1 | 5 | 0 | B |
| 视频播放器与控制层 | 播放器 P0 | 162 | 58 | 0 | 355 | 146 | 56 | 98 | 16 | C |
| 直播列表/分区 | 一级 P1 | 7 | 0 | 0 | 22 | 3 | 0 | 2 | 0 | B |
| 直播间 | 二级 P1 | 20 | 3 | 0 | 34 | 5 | 0 | 9 | 0 | B |
| 消息中心 | 一级 P1 | 2 | 0 | 0 | 8 | 2 | 0 | 0 | 0 | B |
| 私信会话 | 二级 P1 | 2 | 0 | 0 | 16 | 2 | 0 | 0 | 0 | B |
| 消息子页（回复/@/点赞/系统通知） | 二级 P1 | 6 | 0 | 0 | 16 | 2 | 0 | 0 | 0 | B |
| 设置-外观 | 设置 P1 | 3 | 4 | 0 | 64 | 8 | 0 | 1 | 0 | B |
| 设置-播放 | 设置 P1 | 1 | 8 | 0 | 11 | 4 | 0 | 0 | 0 | B |
| 设置-底栏 | 设置 P1 | 1 | 0 | 0 | 15 | 4 | 0 | 0 | 0 | B |
| 设置-首页 | 设置 P1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | A |
| 设置-图标 | 设置 P1 | 1 | 0 | 0 | 3 | 1 | 0 | 0 | 0 | A |
| 设置-动画 | 设置 P1 | 1 | 0 | 0 | 9 | 1 | 0 | 0 | 0 | B |
| 设置-权限 | 设置 P1 | 1 | 0 | 0 | 4 | 0 | 0 | 0 | 0 | A |
| 设置-空间隐私 | 设置 P1 | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | A |
| 设置-登录活动 | 设置 P1 | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | A |
| 设置-消息通知 | 设置 P1 | 1 | 0 | 0 | 2 | 0 | 0 | 0 | 0 | A |
| 设置-插件 | 设置 P1 | 17 | 2 | 0 | 264 | 77 | 0 | 0 | 0 | C |
| 设置-开源许可 | 设置 P2 | 1 | 0 | 0 | 7 | 2 | 0 | 0 | 0 | B |
| 设置-提示 | 设置 P2 | 1 | 0 | 0 | 2 | 1 | 0 | 0 | 0 | A |
| 设置-WebDAV | 设置 P2 | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | A |
| 设置-分享 | 设置 P2 | 2 | 0 | 5 | 0 | 0 | 0 | 0 | 0 | D |
| 设置-搜索 | 设置 P1 | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | A |
| 设置-分类 | 设置 P1 | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | A |
| 设置-屏蔽名单 | 设置 P2 | 1 | 0 | 0 | 5 | 0 | 0 | 0 | 0 | A |
| 设置-评论反诈记录 | 设置 P2 | 1 | 0 | 0 | 15 | 1 | 0 | 0 | 0 | B |
| 设置-皮肤目录 | 设置 P2 | 1 | 0 | 0 | 8 | 2 | 0 | 0 | 0 | A |
| 空间（UP 主主页） | 二级 P1 | 6 | 2 | 0 | 71 | 66 | 5 | 8 | 2 | C |
| 番剧首页 | 一级 P1 | 2 | 0 | 0 | 15 | 6 | 0 | 0 | 0 | B |
| 番剧详情 | 二级 P1 | 1 | 0 | 0 | 46 | 17 | 0 | 0 | 0 | B |
| 番剧播放器 | 播放器 P1 | 7 | 1 | 0 | 32 | 20 | 0 | 1 | 0 | B |
| 番剧时间表 | 二级 P2 | 1 | 0 | 0 | 7 | 5 | 0 | 0 | 0 | A |
| 番剧评论 | 二级 P2 | 1 | 0 | 0 | 3 | 1 | 0 | 0 | 0 | A |
| 历史/收藏/稍后再看/追番 | 二级 P1 | 11 | 0 | 0 | 42 | 13 | 0 | 1 | 0 | C |
| 下载列表 | 二级 P2 | 1 | 0 | 0 | 8 | 3 | 0 | 0 | 0 | B |
| 离线播放 | 播放器 P2 | 1 | 0 | 0 | 9 | 0 | 0 | 0 | 0 | B |
| 音频模式 | 播放器 P1 | 1 | 0 | 0 | 3 | 1 | 0 | 0 | 0 | A |
| 听视频 | 一级 P1 | 1 | 0 | 0 | 10 | 4 | 0 | 0 | 0 | B |
| 音乐播放器 | 播放器 P1 | 2 | 0 | 0 | 44 | 14 | 2 | 7 | 0 | B |
| 音乐详情 | 二级 P2 | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | A |
| BGM 详情 | 二级 P2 | 2 | 0 | 0 | 14 | 1 | 0 | 2 | 0 | B |
| 文章详情 | 二级 P2 | 1 | 0 | 0 | 7 | 3 | 0 | 0 | 0 | A |
| 文章合集 | 二级 P2 | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | A |
| 评论详情 | 二级 P2 | 2 | 0 | 0 | 13 | 5 | 7 | 0 | 0 | B |
| 登录 | 二级 P1 | 4 | 6 | 0 | 68 | 5 | 0 | 1 | 0 | C |
| 引导页 | 二级 P2 | 2 | 0 | 0 | 23 | 10 | 0 | 1 | 0 | B |
| 关注/粉丝 | 二级 P2 | 4 | 0 | 0 | 11 | 1 | 0 | 0 | 0 | A |
| 分区 | 二级 P2 | 1 | 0 | 0 | 1 | 2 | 0 | 2 | 0 | A |
| 分类 | 二级 P2 | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | A |
| 每周必看 | 二级 P2 | 1 | 0 | 0 | 2 | 1 | 0 | 0 | 0 | A |
| 热搜/话题 | 二级 P2 | 2 | 0 | 0 | 7 | 13 | 0 | 0 | 0 | B |
| 网页/即时文章 | 二级 P2 | 2 | 3 | 0 | 1 | 2 | 0 | 0 | 0 | A |
| JS 插件内容页 | 二级 P2 | 2 | 0 | 0 | 12 | 1 | 0 | 0 | 0 | B |
| 外部媒体播放 | 播放器 P2 | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | A |
| 账号台账 | 二级 P2 | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | A |
| AICU | 二级 P2 | 1 | 0 | 0 | 10 | 0 | 0 | 0 | 0 | B |
| 充电榜/舰长 | 二级 P2 | 1 | 0 | 0 | 9 | 8 | 3 | 0 | 0 | A |
| Story | 播放器 P2 | 1 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | A |
| 比赛信息 | 二级 P2 | 1 | 0 | 0 | 4 | 0 | 0 | 0 | 0 | A |
| 协议/隐私 | 二级 P2 | 1 | 1 | 0 | 4 | 2 | 0 | 0 | 0 | A |
| 更新对话框 | 弹层 P1 | 4 | 20 | 27 | 18 | 4 | 0 | 0 | 1 | B |
| 通用对话框与弹层 | 弹层 P1 | 11 | 0 | 0 | 47 | 34 | 0 | 3 | 0 | B |

另有 13 处 L1 落在共享组件里，不属于任何单页，但被多页复用，改造收益最高：

| 文件 | L1 | 影响面 |
| --- | ---: | --- |
| `core/ui/components/UpBadgeName.kt` | 3 | 所有 UP 主名字 |
| `core/ui/components/UserUpBadge.kt` | 1 | 认证徽章 |
| `core/ui/components/UserLevelBadge.kt` | 1 | 等级徽章 |
| `core/ui/components/AnimatedCountText.kt` | 1 | 所有数字滚动 |
| `feature/home/HomeCategoryPage.kt` | 1 | 首页分类页 |
| `feature/home/HomeFeedPinchZoomPolicy.kt` | 2 | 首页双指缩放浮层 |
| `feature/video/screen/VideoDetailScreenStateHolder.kt` | 3 | 视频详情返回/首页按钮 |
| `feature/settings/SettingsOptionPreviewThumb.kt` | 1 | 设置预览缩略图 |

## 5 明细：L1 组件直连

### 5.1 播放器设置体系（被 `PlayerNativeMenuTheme` 钉死）

| ID | 文件:行 | 现状 | COUI 目标 |
| --- | --- | --- | --- |
| P1 | `feature/video/ui/components/PlayerNativeSettings.kt:34` | 强制 `AppUiStyle.MATERIAL3`，9 个调用点被波及 | 删除强制注入，让面板走 COUI17 分发；或改为「面板只在 `MATERIAL3` 存档下生效」 |
| P2 | `PlayerNativeSettings.kt:120` `Surface` | M3 实色底板 | `AppSurfaceCard` / COUI `Card` |
| P3 | `PlayerNativeSettings.kt:244` `ListItem` | M3 `ListItem`（headline+supporting+trailing） | COUI `ArrowPreference` / `BasicComponent` |
| P4 | `PlayerSleepTimerSettings.kt:37` `ListItem` | 定时关闭用 M3 `ListItem` | 同上 |
| P5 | `VideoSettingsPanel.kt:429` | M3 `AlertDialog` + M3 `Text` 标题 | `AppAlertDialog` / COUI `WindowDialog` |
| P6 | `PlayerSelectionPopup.kt:115` | 面板标题用 `MaterialTheme.typography.titleMedium` | `AppText` 默认样式或 COUI `textStyles.title` |
| P7 | `PlayerEpisodeMenu.kt:126/172/215/428/448/449/451/452` | 8 处 `Text` + `fontSize = 16.sp` 直接写死 | `AppText` + COUI 字阶 |
| P8 | `PlayerEpisodeMenu.kt:336` | `androidx.compose.material3.Surface` | `AppSurfaceCard` |
| P9 | `PiliSubtitleTools.kt:276/288` | 两个字幕对话框用 M3 `AlertDialog` | `AppAlertDialog` |
| P10 | `PiliSubtitleTools.kt:277/291/294/323` | 对话框正文 4 处 M3 `Text` | `AppText` |
| P11 | `PiliPlaybackInformationDialog.kt:89` | M3 `Surface` 弹层 | COUI `WindowDialog` |
| P12 | `PiliPlaybackInformationDialog.kt:131` | M3 `TextButton`「确定」 | `AppButton` 胶囊 |
| P13 | `PlayerSponsorSegmentsPanel.kt:169` | M3 `Surface` | `AppSurfaceCard` |
| P14 | `PlayerSponsorSegmentsPanel.kt:114/117/188/194` | 4 处 M3 `Text` | `AppText` |
| P15 | `LargeScreenVideoLayout.kt:285/297/326/361/400` | 5 条 M3 `VerticalDivider` | COUI 无竖分割线范式，改留白或胶囊分组 |
| P16 | `DanmakuPoolSheet.kt:327` | `HorizontalDivider(color = outlineVariant.copy(alpha=…))` | `AppPreferenceDivider` 内缩分割线 |
| P17 | `FullscreenPlayerOverlay.kt:1179` | 手势逃生提示用 M3 `Surface` + `secondaryContainer` | COUI 提示胶囊（`Chip` / `Badge`） |
| P18 | `PlayerSelectionPopup.kt:255/305/394` | `AlertDialog` / `DropdownMenu` / `DropdownMenuItem` | 仅在被强制 M3 的宿主管辖下触发，P1 修好后自动消失 |

### 5.2 更新与高亮

| ID | 文件:行 | 现状 | COUI 目标 |
| --- | --- | --- | --- |
| U1 | `feature/settings/highlights/UpdateHighlightsHost.kt:143–232` | 外壳已是 `AppAlertDialog`，正文 20 处 M3 `Text` | 正文换 `AppText`，行内 `MaterialTheme.typography.titleMedium/bodySmall` 换 COUI 字阶 |
| U2 | `UpdateHighlightsHost.kt:230–232` | 预览色块直接读 `scheme.onSurface` 等 | 用 `AppSurfaceTokens`，否则预览与实际渲染不一致 |

`Material3AppUpdateDialog.kt` 的 27 处属冻结分支，本轮不动。

### 5.3 设置

| ID | 文件:行 | 现状 | COUI 目标 |
| --- | --- | --- | --- |
| S1 | `settings/screen/PlaybackSettingsScreen.kt:461/466/476/507/512/522` | 6 处 M3 `Text` + `MaterialTheme.typography.bodyLarge` | `AppText` + COUI 字阶 |
| S2 | `PlaybackSettingsScreen.kt:472/518` | 2 处 M3 `Surface` 做说明块 | `AppSurfaceCard` |
| S3 | `settings/ThemeCustomColorPickerDialog.kt:231` | 全限定名 `androidx.compose.material3.Text("Aa 示例文字")` | `AppText` |
| S4 | `settings/screen/AppearanceSettingsScreen.kt:2778–2780` | 主题预览 3 处 M3 `Text` | `AppText` |
| S5 | `plugin/EyeProtectionSettings.kt:526` | 全限定 `androidx.compose.material3.AlertDialog` | `AppAlertDialog` |
| S6 | `plugin/BiliPaiFeedFilterPlugin.kt:272` | M3 `Surface` | `AppSurfaceCard` |
| S7 | `feature/settings/update/MiuixAppUpdateDialog.kt:285` | COUI `HorizontalDivider()` 无内缩 | `AppPreferenceDivider` 或加 `padding(start=…)` |

### 5.4 一级页与共享组件

| ID | 文件:行 | 现状 | COUI 目标 |
| --- | --- | --- | --- |
| H1 | `feature/home/subscription/SubscriptionFeedPage.kt:624` | M3 `Surface` 错误态容器 | `AppSurfaceCard` |
| H2 | `SubscriptionFeedPage.kt:1541` | M3 `Text` | `AppText` |
| D1 | `feature/dynamic/DynamicScreen.kt:1891` | `OldContentDivider` 内 M3 `Surface` | 删除或换 `AppPreferenceDivider` |
| V1 | `video/ui/section/VideoNoteSection.kt:559/684` | 笔记侧栏/编辑用 M3 `Surface` | COUI `Card` |
| V2 | `video/ui/section/VideoPlayerGestureFeedback.kt:110` | 手势反馈 M3 `Surface` | COUI `Chip` |
| V3 | `video/ui/section/PartResumeNoticeChip.kt:43` | M3 `Text` | `AppText` |
| V4 | `video/screen/VideoDetailScreenStateHolder.kt:351/371/398` | 2 个 `IconButton` + 1 个 `Text("立即播放")` | `AppIconButton` + `AppButton` |
| L1 | `live/components/LivePlayerStatus.kt:49/50` | `Text(color = Color.White, fontSize = 12.sp)` 写死 | `AppText` + 遮罩令牌 |
| L2 | `live/components/LiveMedalBadge.kt:44` | M3 `Text` | `AppText` |
| W1 | `web/WebViewScreen.kt:149/165/425` | 3 处 M3 `Text` | `AppText` |
| G1 | `login/BiliPaiTransferScanner.kt:134/136/141/143/284/292` | 6 处全限定 `androidx.compose.material3.Text` | `AppText`，按钮外壳换 `AppButton` |
| B1 | `bangumi/BangumiPlayerScreen.kt:1535` | M3 `IconButton` 返回 | `AppIconButton` |
| A1 | `agreement/UserAgreementGate.kt:82` | `Surface(color = colorScheme.background)` | `AppScaffold` 默认容器 |
| C1 | `core/ui/components/UpBadgeName.kt:96/110/122` 等 4 个共享组件 | 共 6 处 M3 `Text` | `AppText`，一处改动覆盖全站 |
| P19 | `space/SpaceScreen.kt:4017/4022` | 玻璃关闭时用 M3 `DropdownMenu` | 统一到 `AppDropdownMenuItem`，删掉 `!glassSortMenu` 分支 |

## 6 明细：L2 令牌直连

L2 是量最大、视觉最直接的一层。按改法分四类，逐页 Top 违规文件如下。

### 6.1 字号：`MaterialTheme.typography`（2113 处）

根因见第 1 节：COUI17 下它返回 MD3 字阶。改法是把 `AppText(style = MaterialTheme.typography.x)` 改成 `AppText()` 默认样式，或显式取 COUI `textStyles`。

| 界面 | 主要违规文件（处数） |
| --- | --- |
| 播放器 | `DanmakuSettingsPanel.kt`(39)、`CommentSearchSheet.kt`(19)、`DanmakuPoolSheet.kt`(18)、`CommentInputDialog.kt`(14)、`VideoPlayerOverlay.kt`(14) |
| 设置-插件 | `PluginsScreen.kt`(110)、`SponsorBlockPlugin.kt`(27)、`TodayWatchPlugin.kt`(23)、`AdFilterPlugin.kt`(19) |
| 我的 | `ProfileScreen.kt`(71)、`WallpaperAdjustmentSheet.kt`(12)、`OfficialWallpaperSheet.kt`(10) |
| 动态 | `DynamicCard.kt`(25)、`ImagePreviewDialog.kt`(20) |
| 视频详情 | `VideoInfoSection.kt`(40)、`VideoPlayerComponents.kt`(18)、`VideoNoteSection.kt`(17) |
| 搜索 | `SearchScreen.kt`(26)、`SearchLandingUi.kt`(18) |
| 空间 | `SpaceScreen.kt`(59) |
| 番剧详情 | `BangumiDetailScreen.kt`(46) |
| 音乐播放器 | `MusicPlayerContent.kt`(42) |
| 设置-外观 | `LiquidGlassLivePreview.kt`(23)、`AppearanceSettingsScreen.kt`(22)、`ThemeCustomColorPickerDialog.kt`(19) |
| 登录 | `LoginScreen.kt`(34)、`LoginComponents.kt`(25) |
| 首页 | `SubscriptionFeedPage.kt`(13)、`BottomBar.kt`(8)、`HomeHeroCarousel.kt`(8) |
| 设置首页 | `SettingsSections.kt`(20)、`SettingsScreen.kt`(8) |
| 历史等列表 | `HistoryRecapCard.kt`(11)、`CommonListScreen.kt`(10)、`PersonalRecapCharts.kt`(8) |

### 6.2 配色：`MaterialTheme.colorScheme`（885 处）

改法是换 `AppContentColors` / `AppSurfaceTokens` / `AppSurfaceTokens.onSurfaceVariantSummary()`。COUI17 的色板由 `AppThemeEndpointPolicy.kt` 用强调色构建，绕过它取 M3 色会出现色相不一致。

| 界面 | 主要违规文件 |
| --- | --- |
| 我的 | `ProfileScreen.kt`(51) |
| 播放器 | `ReplyComponents.kt`(12)、`PlayerEpisodeMenu.kt`(11)、`SponsorSkipUI.kt`(10)、`CommentInputDialog.kt`(9)、`PlayerNativeSettings.kt`(9) |
| 视频详情 | `VideoInfoSection.kt`(21)、`VideoPlayerComponents.kt`(11) |
| 设置-插件 | `PluginsScreen.kt`(33)、`SponsorBlockPlugin.kt`(21) |
| 搜索 | `SearchScreen.kt`(26)、`SearchTrendingScreen.kt`(10) |
| 动态 | `DynamicCard.kt`(8)、`DynamicSidebar.kt`(6) |
| 通用弹层 | `WallpaperAdjustmentSheet.kt`(11)、`VideoShareSheet.kt`(10)、`CommentInputDialog.kt`(9) |

### 6.3 写死颜色（177 处）与写死圆角（205 处）

集中在播放器（56 + 98）。播放器进度条、遮罩、按钮描边大量直接写 `Color(0x…)` 和 `RoundedCornerShape(n.dp)`，不走 `AppShapes`，所以在 COUI17 的连续曲率与色板下不变形也不同步。同类集中在 `MusicPlayerContent.kt`（2 + 7）、`CommentDetail`（7）、`SpaceScreen.kt`（5 + 8）。

## 7 明细：L3 版式范式

结构指纹 23 处：`HorizontalDivider` 10、`AlertDialog` 7、`DropdownMenu` 4、`Scaffold` 1、`NavigationRail` 1。其中 `NavigationRail` 与 `Scaffold` 属冻结分支或已命名 COUI 宿主。

真正需要改版式的是视觉抽查发现的问题。抽查了 132 真机 10 个界面，与 COUI demo/系统截图对照：

| ID | 界面 | 现状 | COUI 目标 |
| --- | --- | --- | --- |
| X1 | 全站 | 深色主题下胶囊 chip 选中态只靠亮度差（灰对灰），看不出层级 | COUI 基准是「实心强调色胶囊 + 勾选」，这是全站最大的一致性缺口；浅色主题需同样复核 |
| X2 | 全站 | 二级页顶栏返回是描边圆环 `IconButton` | COUI 是无描边圆形按钮；首页/我的/搜索同样残留 M3 outlined icon button 形态 |
| X3 | 我的、历史 | 「去登录」「重试」是淡紫 tonal 胶囊（M3 secondary-container 配色） | 中性或强调色实心胶囊（`AppButton`），紫底与 COUI 色板无关 |
| X4 | 首页 feed、我的、历史、视频详情、相关推荐 | 黑/灰底裸列表，靠留白分行 | COUI 卡片分组：白卡 + 内缩分割线；目前只有设置系列做到了 |
| X5 | 历史 | 「搜索历史」是无边框行内搜索行，下接通栏细分割线 | 胶囊搜索框（`SearchBar`），去掉通栏分割线 |
| X6 | 设置子页 | 行是 MIUIX 两行式（粗标题 + 两行灰色说明），中栏被挤窄，右侧值几乎贴边、行高不齐 | COUI 单行标题 + 右侧值 + 可选单行说明 |
| X7 | 全站 | 底部悬浮 dock 是常驻覆盖层，压住「立即登录」文字，浮在键盘上方 | 按页面显示/隐藏，并预留底部安全内边距 |
| X8 | 播放器 | 竖屏详情页控制层是自造最小层：无进度条、无播放暂停、无底部面板、无弹幕设置入口 | COUI 玻璃底部面板 + 悬浮圆钮体系 |

另外真机抽查时发现一个布局缺陷，不属于 COUI 差异但同批修：动态页竖屏下左侧多出一条纯黑窄栏（返回圆钮 + 动态圆钮 + 全部动态），内容区被压到约三分之二宽。

## 8 真机截图对照

现状截图（132 / 1240×2772 / 深色）：

| 文件 | 界面 |
| --- | --- |
| `current/01-home.png` | 首页推荐 |
| `current/02-dynamic.png` | 动态（空态，左侧窄栏异常） |
| `current/03-profile.png` | 我的（未登录，立即登录被 dock 遮挡） |
| `current/04-history.png` | 历史（行内搜索 + 通栏分割线） |
| `current/05-settings-home.png` | 设置首页（已 COUI 化，作为正面样本） |
| `current/06-settings-appearance.png` | 设置-外观（中栏被挤窄） |
| `current/07-settings-playback.png` | 设置-播放（同上） |
| `current/08-search.png` | 搜索（胶囊搜索框 + 分类 track） |
| `current/09-video-detail.png` | 视频详情 |
| `current/10-player.png` | 播放器控制层 |

COUI 参照：`reference/card.png`、`chip.png`、`chiptab.png`、`dialog.png`、`bottomsheet.png`、`button.png`、`switch.png`、`slider.png`、`arrow.png`，系统应用参照 `reference/system-settings_home.png`、`system-settings_display.png`、`system-settings_search.png`、`system-gallery_home.png`、`system-filemanager_home.png`。

## 9 改造批次

按层推进，每批一个 commit，验收后才进下一批。

### 批次 0：棘轮重写（必须先做）

`app/src/test/java/com/android/purebilibili/core/ui/migration/NativeThemeMigrationBoundaryTest.kt` 当前 14 条里 10 条失败，且语义有假阳性：

- `import ...AppSlider as Slider`、`AppCard as Card`、`AppAlertDialog as AlertDialog` 都被当成「直连原生组件」。
- COUI 组件 import（`io.github.suqi8.coui.kmp.*`）被计成债务。

先修语义：解析 `import … as Alias`，把 COUI `basic` / `preference` 包排除，再按现状重新冻结上限。之后每批收工下调一次，只降不升。

### 批次 1：L1 组件

顺序按「影响面 × 改动量」：

1. 共享组件 6 处（`UpBadgeName` 等），一处改动覆盖全站。
2. 播放器设置体系 P1–P12：先拆 `PlayerNativeMenuTheme` 的强制 M3，再逐面板换 COUI。
3. 更新与高亮 U1–U2。
4. 设置 S1–S7。
5. 一级页 H1–H2、D1、V1–V4、G1、W1、L1–L2、B1、A1、P19。

验收：`./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug` + 132 真机截图。

### 批次 2：L2 令牌

顺序：播放器 → 设置-插件 → 我的 → 动态 → 视频详情 → 搜索 → 空间 → 番剧 → 音乐 → 首页。

机械替换为主，但 `MaterialTheme.typography` 换掉后行高会变，需要逐页看截图确认不回流。

### 批次 3：L3 版式

X1（chip 选中态）→ X2（描边圆钮）→ X4/X5（卡片分组与胶囊搜索）→ X6（设置行重排）→ X3/X7 → X8（播放器控制层重做）。

X8 单独一个 commit，改动面最大。

## 10 与已锁定决策的偏差

审计过程中发现一处实现与 ADR 不一致：

`AppUiStyle.fromLegacyValues`（`UiPreset.kt:61`）在「两个键都缺失」时返回 `AppUiStyle.MATERIAL3`——那是全新安装。而 `docs/adr/0001-coui17-only-skin.md` 锁定的是新用户默认 COUI17。批次 0 一并修掉，改为 `COUI17`。

## 11 复现方式

审计数据来自一次性扫描脚本（未入库），口径写在第 2 节。需要重跑时按同样规则重新统计：

- L1：`(Text|Surface|IconButton|TextButton|Button|OutlinedButton|FilledTonalButton|FilledTonalIconButton|Item|AlertDialog|Slider|OutlinedTextField|TextField|Card|Switch|Checkbox|FilterChip|Divider|ProgressIndicator|NavigationRail|NavigationRailItem|Scaffold|TopAppBar|TabRow|FloatingActionButton)`，需解析 alias import，排除 `io.github.suqi8.coui.kmp.*`。
- L2：`MaterialTheme.(typography|colorScheme|shapes)`、`Color(0x`、`RoundedCornerShape(`。
- L3：结构指纹 + 真机截图人工判定。
