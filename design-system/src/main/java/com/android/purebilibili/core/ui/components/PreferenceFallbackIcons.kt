package com.android.purebilibili.core.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.ImageVector
import com.android.purebilibili.core.ui.AppIcons

/**
 * 设置二级页只画字形、且没有显式图标时，按标题关键字补一枚 COUI 图标。
 *
 * 只在 [AppPreferenceIconTreatment.PLAIN] 下生效，其他皮肤与一级页保持原样。
 */
@Composable
internal fun rememberPreferenceFallbackIcon(title: String?): ImageVector? {
    if (LocalAppPreferenceIconTreatment.current != AppPreferenceIconTreatment.PLAIN) return null
    val key = title?.trim().orEmpty()
    if (key.isEmpty()) return null
    return remember(key) { resolvePreferenceFallbackIcon(key) }
}

/** 标题里带当前值时只比较冒号前的功能名，避免换值换图标。 */
internal fun resolvePreferenceFallbackIcon(title: String): ImageVector {
    val key = title.substringBefore('：').substringBefore(':').trim()
    fun has(vararg words: String) = words.any { key.contains(it) }
    return when {
        has("深色", "夜间", "暗色") -> AppIcons.Outlined.DarkMode
        has("时间", "定时", "开始", "结束", "停留", "自动退出", "分钟", "小时") -> AppIcons.Outlined.Schedule
        has("图标") -> AppIcons.Outlined.Apps
        has("主题", "外观", "风格", "样式", "预设", "颜色", "色彩", "取色", "调色", "色板") -> AppIcons.Outlined.Palette
        has("动画", "过渡", "动效") -> AppIcons.Outlined.Animation
        has("振动", "触感", "震动", "反馈", "玻璃", "模糊", "透明") -> AppIcons.Outlined.Tune
        has("刷新率", "屏幕", "显示", "缩放", "分辨率", "DPI", "字体大小") -> AppIcons.Outlined.Phone
        has("字幕") -> AppIcons.Outlined.Subtitles
        has("倍速", "速度", "播放", "解码", "画质", "连播", "小窗", "后台") -> AppIcons.Outlined.PlayCircle
        has("音量", "音频", "声音", "响度") -> AppIcons.Outlined.VolumeUp
        has("手势", "触摸", "点击", "滑动") -> AppIcons.Outlined.Tune
        has("隐私", "权限", "安全", "屏蔽", "锁定") -> AppIcons.Outlined.Lock
        has("缓存", "清理", "存储", "空间") -> AppIcons.Outlined.DeleteSweep
        has("下载", "保存", "路径", "目录") -> AppIcons.Outlined.Download
        has("备份", "WebDAV", "云端", "同步") -> AppIcons.Outlined.Cloud
        has("插件", "扩展") -> AppIcons.Outlined.Extension
        has("通知", "消息", "提醒") -> AppIcons.Outlined.Notifications
        has("评论", "弹幕", "互动", "私信") -> AppIcons.Outlined.ChatBubble
        has("搜索") -> AppIcons.Outlined.Search
        has("语言", "翻译") -> AppIcons.Outlined.Translate
        has("壁纸", "图片", "封面") -> AppIcons.Outlined.Image
        has("首页", "动态") -> AppIcons.Outlined.Home
        has("关于", "版本", "更新", "构建") -> AppIcons.Outlined.Info
        has("网络", "流量", "CDN") -> AppIcons.Outlined.Cloud
        else -> AppIcons.Outlined.Settings
    }
}
