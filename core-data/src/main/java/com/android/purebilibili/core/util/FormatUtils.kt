package com.android.purebilibili.core.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object FormatUtils {
    private const val COVER_IMAGE_LOW_WIDTH = 240
    private const val COVER_IMAGE_LOW_HEIGHT = 150
    private val detailedCommentTimeFormatter =
        java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ROOT)

    // 列表滚动时每张卡片每帧都会走到这些格式化函数。`ZoneId.systemDefault()` 要读系统
    // 时区、`DateTimeFormatter.ofPattern` 要重新编译模式，两者都比拼接字符串贵一个数量级，
    // 因此按 (pattern, locale) 缓存；时区取进程级快照，与手写一遍的语义一致。
    private val systemZoneId: java.time.ZoneId by lazy { java.time.ZoneId.systemDefault() }
    private val dateTimeFormatters =
        java.util.concurrent.ConcurrentHashMap<String, java.time.format.DateTimeFormatter>()
    private val simpleDateFormatters =
        ThreadLocal.withInitial { HashMap<String, SimpleDateFormat>() }

    private fun dateTimeFormatter(
        pattern: String,
        locale: Locale,
    ): java.time.format.DateTimeFormatter {
        val key = "$pattern\u0000${locale.toLanguageTag()}"
        return dateTimeFormatters.getOrPut(key) {
            java.time.format.DateTimeFormatter.ofPattern(pattern, locale)
        }
    }

    private fun simpleDateFormat(pattern: String, locale: Locale): SimpleDateFormat {
        val cache = simpleDateFormatters.get() ?: HashMap<String, SimpleDateFormat>().also {
            simpleDateFormatters.set(it)
        }
        val key = "$pattern\u0000${locale.toLanguageTag()}"
        return cache.getOrPut(key) { SimpleDateFormat(pattern, locale) }
    }

    /**
     * 将数字格式化为 B站风格 (例如: 1.2万)
     */
    fun formatStat(count: Long): String {
        return when {
            count >= 100000000 -> formatOneDecimal(count, 100000000L, "亿")
            count >= 10000 -> formatOneDecimal(count, 10000L, "万")
            else -> count.toString()
        }
    }

    /** 与 `String.format("%.1f…")` 同结果，但不创建 Formatter。 */
    private fun formatOneDecimal(count: Long, divisor: Long, unit: String): String {
        val tenths = (count + divisor / 20) / (divisor / 10)
        return "${tenths / 10}.${tenths % 10}$unit"
    }

    /**
     * 将秒数格式化为 HH:MM:SS
     */
    fun formatDuration(seconds: Int): String {
        return formatDurationFromSeconds(seconds.toLong())
    }

    /**
     * 将毫秒数格式化为 HH:MM:SS
     */
    fun formatDuration(milliseconds: Long): String {
        return formatDurationFromSeconds(milliseconds / 1000L)
    }

    private fun formatDurationFromSeconds(rawSeconds: Long): String {
        val seconds = rawSeconds.coerceAtLeast(0L)
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, secs)
        } else {
            String.format("%02d:%02d", minutes, secs)
        }
    }

    /**
     * 补全协议并去掉旧缩放参数；显示尺寸和压缩质量由图片请求统一处理。
     */
    fun fixImageUrl(url: String?): String {
        return normalizeImageUrl(url)
    }

    fun resolveVideoCoverUrl(
        url: String?,
        useLowQuality: Boolean
    ): String {
        val normalized = normalizeImageUrl(url)
        if (normalized.isEmpty()) return normalized
        return if (useLowQuality) {
            buildSizedImageUrl(normalized, width = COVER_IMAGE_LOW_WIDTH, height = COVER_IMAGE_LOW_HEIGHT)
        } else {
            normalized
        }
    }

    fun buildSizedImageUrl(
        url: String?,
        width: Int,
        height: Int,
        format: String = "webp"
    ): String {
        val normalized = normalizeImageUrl(url)
        if (normalized.isEmpty() || width <= 0 || height <= 0) return normalized
        return "$normalized@${width}w_${height}h.$format"
    }

    fun normalizeImageUrl(url: String?): String {
        if (url.isNullOrEmpty()) return ""

        val withProtocol = if (url.startsWith("//")) {
            "https:$url"
        } else if (url.startsWith("http://")) {
            url.replace("http://", "https://")
        } else {
            url
        }

        return withProtocol.substringBefore("@")
    }

    /**
     * 格式化观看进度
     */
    fun formatProgress(progress: Int, duration: Int): String {
        if (duration <= 0) return "已看"
        if (progress == -1) return "已看" // finish
        if (progress == 0) return "未观看"
        val percent = (progress.toFloat() / duration.toFloat() * 100).toInt()
        return if (percent >= 99) "已看完" else "已看$percent%"
    }
    
    /**
     *  格式化发布时间 (相对时间 + 日期)
     * PiliPlus 视频与评论共用规则，例如: "3小时前" / "昨天 18:30" / "08-20"
     */
    fun formatPublishTime(
        timestampSeconds: Long,
        nowMs: Long = System.currentTimeMillis(),
        zoneId: java.time.ZoneId = systemZoneId,
        locale: Locale = Locale.getDefault(),
    ): String {
        if (timestampSeconds <= 0) return ""

        val published = java.time.Instant.ofEpochSecond(timestampSeconds).atZone(zoneId)
        val now = java.time.Instant.ofEpochMilli(nowMs).atZone(zoneId)
        val elapsedMinutes = java.time.temporal.ChronoUnit.MINUTES
            .between(published, now)
            .coerceAtLeast(0L)
        if (elapsedMinutes < 1L) return "刚刚"
        if (elapsedMinutes < 60L) return "${elapsedMinutes}分钟前"

        val elapsedHours = java.time.temporal.ChronoUnit.HOURS
            .between(published, now)
            .coerceAtLeast(0L)
        if (elapsedHours < 24L) return "${elapsedHours}小时前"

        val elapsedDays = java.time.temporal.ChronoUnit.DAYS
            .between(published.toLocalDate(), now.toLocalDate())
            .coerceAtLeast(0L)
        if (elapsedDays == 1L) {
            val time = dateTimeFormatter("HH:mm", locale).format(published)
            return "昨天 $time"
        }
        if (elapsedDays < 4L) return "${elapsedDays}天前"

        val pattern = if (published.year == now.year) "MM-dd" else "yyyy-MM-dd"
        return dateTimeFormatter(pattern, locale).format(published)
    }

    /**
     * 评论时间：详细模式固定显示本地年月日时分秒，否则沿用 PiliPlus 相对时间规则。
     */
    fun formatCommentTime(
        timestampSeconds: Long,
        detailedTimeEnabled: Boolean,
        nowMs: Long = System.currentTimeMillis(),
        zoneId: java.time.ZoneId = systemZoneId,
        locale: Locale = Locale.getDefault(),
    ): String {
        if (timestampSeconds <= 0L) return ""
        if (!detailedTimeEnabled) {
            return formatPublishTime(timestampSeconds, nowMs, zoneId, locale)
        }
        return detailedCommentTimeFormatter.format(
            java.time.Instant.ofEpochSecond(timestampSeconds).atZone(zoneId)
        )
    }

    /**
     * 历史条目观看时间，与 PiliPlus `DateFormatUtils.chatFormat(isHistory: true)` 对齐：
     * 今天 HH:mm / 昨天 HH:mm / 同年 MM-dd HH:mm / 跨年 yyyy-MM-dd HH:mm。
     */
    fun formatHistoryViewTime(
        timestampSeconds: Long,
        zoneId: java.time.ZoneId = systemZoneId,
        locale: Locale = Locale.getDefault(),
    ): String {
        if (timestampSeconds <= 0) return ""

        val viewed = java.time.Instant.ofEpochSecond(timestampSeconds).atZone(zoneId)
        val now = java.time.Instant.ofEpochMilli(System.currentTimeMillis()).atZone(zoneId)
        val clock = dateTimeFormatter("HH:mm", locale).format(viewed)
        return when (viewed.toLocalDate()) {
            now.toLocalDate() -> "今天 $clock"
            now.toLocalDate().minusDays(1) -> "昨天 $clock"
            else -> {
                val pattern = if (viewed.year == now.year) "MM-dd HH:mm" else "yyyy-MM-dd HH:mm"
                dateTimeFormatter(pattern, locale).format(viewed)
            }
        }
    }

    fun formatPrecisePublishTime(
        timestampSeconds: Long,
        pattern: String = "yyyy-MM-dd HH:mm",
        locale: Locale = Locale.getDefault(),
        timeZone: TimeZone = TimeZone.getDefault()
    ): String {
        if (timestampSeconds <= 0) return ""

        return simpleDateFormat(pattern, locale).apply {
            this.timeZone = timeZone
        }.format(Date(timestampSeconds * 1000L))
    }
}
