package com.android.purebilibili.core.ui.performance

import android.app.Activity
import android.content.ContentResolver
import android.os.Build
import android.provider.Settings
import android.view.Display
import java.util.Locale
import kotlin.math.abs

internal const val SYSTEM_AUTO_DISPLAY_MODE_ID = 0

internal data class AppDisplayMode(
    val modeId: Int,
    val refreshRate: Float,
    val width: Int,
    val height: Int,
)

/**
 * Returns the display modes exposed by Android. Mode `0` is deliberately not synthesized here:
 * it represents PiliPlus-style "auto", where the app releases its display-mode preference.
 */
internal fun Activity.supportedAppDisplayModes(): List<AppDisplayMode> {
    val targetDisplay = currentActivityDisplay() ?: return emptyList()
    return targetDisplay.supportedModes
        .map { mode ->
            AppDisplayMode(
                modeId = mode.modeId,
                refreshRate = mode.refreshRate,
                width = mode.physicalWidth,
                height = mode.physicalHeight,
            )
        }
        .sortedWith(
            compareByDescending<AppDisplayMode> { it.refreshRate }
                .thenByDescending { it.width * it.height }
                .thenBy { it.modeId },
        )
}

internal fun normalizePreferredDisplayModeId(
    preferredModeId: Int,
    supportedModes: List<AppDisplayMode>,
): Int = preferredModeId.takeIf { requestedId ->
    requestedId != SYSTEM_AUTO_DISPLAY_MODE_ID &&
        supportedModes.any { mode -> mode.modeId == requestedId }
} ?: SYSTEM_AUTO_DISPLAY_MODE_ID

internal fun resolveAutoHighRefreshDisplayModeId(
    supportedModes: List<AppDisplayMode>,
    currentWidth: Int,
    currentHeight: Int,
    isColorOsFamily: Boolean,
    powerSaveActive: Boolean = false,
    oplusRefreshRateSetting: Int = -1,
    choiceModeRate: Float = 0f,
    peakRefreshRate: Float = 0f,
): Int {
    if (!isColorOsFamily || powerSaveActive || oplusRefreshRateSetting == 2) {
        return SYSTEM_AUTO_DISPLAY_MODE_ID
    }
    if (peakRefreshRate in 1f..60.5f && choiceModeRate <= 60.5f && oplusRefreshRateSetting < 0) {
        return SYSTEM_AUTO_DISPLAY_MODE_ID
    }
    val sameResolutionModes = supportedModes.filter {
        it.width == currentWidth && it.height == currentHeight && it.refreshRate > 60.5f
    }
    if (sameResolutionModes.isEmpty()) return SYSTEM_AUTO_DISPLAY_MODE_ID
    val targetRate = when {
        choiceModeRate > 60.5f -> choiceModeRate
        peakRefreshRate > 60.5f -> peakRefreshRate
        oplusRefreshRateSetting == 1 -> 90f
        else -> 120f
    }
    val matched = sameResolutionModes
        .filter { it.refreshRate <= targetRate + 1.5f }
        .maxByOrNull { it.refreshRate }
        ?: sameResolutionModes.minByOrNull { abs(it.refreshRate - targetRate) }
    return matched?.modeId ?: SYSTEM_AUTO_DISPLAY_MODE_ID
}

internal fun displayModePreferenceLabel(mode: AppDisplayMode): String {
    val roundedRate = mode.refreshRate.toInt()
    val rateLabel = if (abs(mode.refreshRate - roundedRate) < 0.05f) {
        roundedRate.toString()
    } else {
        String.format(Locale.ROOT, "%.2f", mode.refreshRate).trimEnd('0').trimEnd('.')
    }
    return "$rateLabel Hz · ${mode.width} × ${mode.height}"
}

/** Applies one explicit user preference, or resolves ColorOS high-refresh mode when set to Auto. */
internal fun Activity.applyPreferredDisplayMode(preferredModeId: Int) {
    val supportedModes = supportedAppDisplayModes()
    val explicitModeId = normalizePreferredDisplayModeId(
        preferredModeId = preferredModeId,
        supportedModes = supportedModes,
    )
    val resolvedModeId = if (explicitModeId != SYSTEM_AUTO_DISPLAY_MODE_ID) {
        explicitModeId
    } else {
        resolveActivityAutoDisplayModeId(supportedModes)
    }
    window.attributes = window.attributes.apply {
        preferredDisplayModeId = resolvedModeId
        preferredRefreshRate = 0f
    }
}

private fun Activity.resolveActivityAutoDisplayModeId(supportedModes: List<AppDisplayMode>): Int {
    val activeMode = currentActivityDisplay()?.mode ?: return SYSTEM_AUTO_DISPLAY_MODE_ID
    val resolver: ContentResolver = contentResolver
    val brand = "${Build.MANUFACTURER} ${Build.BRAND}".lowercase(Locale.ROOT)
    val oplusRateSetting = runCatching {
        Settings.Secure.getInt(resolver, "oplus_customize_screen_refresh_rate", -1)
    }.getOrDefault(-1)
    val choiceModeRate = runCatching {
        Settings.Secure.getFloat(resolver, "choice_mode_rate", 0f)
    }.getOrDefault(0f)
    val peakRefreshRate = runCatching {
        Settings.System.getFloat(resolver, "peak_refresh_rate", 0f)
    }.getOrDefault(0f)
    val isColorOsFamily = "oppo" in brand || "oneplus" in brand || "realme" in brand ||
        "oplus" in brand || oplusRateSetting >= 0 || choiceModeRate > 0f
    val powerSaveActive = runCatching {
        Settings.Global.getInt(resolver, "low_power", 0) == 1 ||
            Settings.System.getInt(resolver, "super_powersave_mode_state", 0) == 1
    }.getOrDefault(false)
    return resolveAutoHighRefreshDisplayModeId(
        supportedModes = supportedModes,
        currentWidth = activeMode.physicalWidth,
        currentHeight = activeMode.physicalHeight,
        isColorOsFamily = isColorOsFamily,
        powerSaveActive = powerSaveActive,
        oplusRefreshRateSetting = oplusRateSetting,
        choiceModeRate = choiceModeRate,
        peakRefreshRate = peakRefreshRate,
    )
}

private fun Activity.currentActivityDisplay(): Display? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        display
    } else {
        @Suppress("DEPRECATION")
        windowManager.defaultDisplay
    }
