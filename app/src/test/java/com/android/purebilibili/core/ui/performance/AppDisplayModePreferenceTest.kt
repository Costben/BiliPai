package com.android.purebilibili.core.ui.performance

import kotlin.test.Test
import kotlin.test.assertEquals

class AppDisplayModePreferenceTest {

    private val modes = listOf(
        AppDisplayMode(modeId = 1, refreshRate = 60f, width = 1080, height = 2400),
        AppDisplayMode(modeId = 2, refreshRate = 120f, width = 1080, height = 2400),
    )

    @Test
    fun autoPreference_releasesDisplayModeVote() {
        assertEquals(
            SYSTEM_AUTO_DISPLAY_MODE_ID,
            normalizePreferredDisplayModeId(SYSTEM_AUTO_DISPLAY_MODE_ID, modes),
        )
    }

    @Test
    fun supportedManualPreference_keepsRequestedMode() {
        assertEquals(2, normalizePreferredDisplayModeId(2, modes))
    }

    @Test
    fun staleManualPreference_fallsBackToSystemAuto() {
        assertEquals(
            SYSTEM_AUTO_DISPLAY_MODE_ID,
            normalizePreferredDisplayModeId(99, modes),
        )
    }

    @Test
    fun colorOsAuto_requestsHighRefreshAtCurrentResolutionUnlessPowerSaveOrStandard60Hz() {
        val plkModes = listOf(
            AppDisplayMode(modeId = 1, refreshRate = 165f, width = 1264, height = 2780),
            AppDisplayMode(modeId = 2, refreshRate = 120f, width = 1264, height = 2780),
            AppDisplayMode(modeId = 3, refreshRate = 60f, width = 1264, height = 2780),
            AppDisplayMode(modeId = 4, refreshRate = 120f, width = 1080, height = 2376),
        )
        assertEquals(
            2,
            resolveAutoHighRefreshDisplayModeId(
                supportedModes = plkModes,
                currentWidth = 1264,
                currentHeight = 2780,
                isColorOsFamily = true,
                oplusRefreshRateSetting = 3,
                choiceModeRate = 120f,
                peakRefreshRate = 120f,
            ),
        )
        assertEquals(
            SYSTEM_AUTO_DISPLAY_MODE_ID,
            resolveAutoHighRefreshDisplayModeId(
                supportedModes = plkModes,
                currentWidth = 1264,
                currentHeight = 2780,
                isColorOsFamily = true,
                powerSaveActive = true,
                choiceModeRate = 120f,
            ),
        )
        assertEquals(
            SYSTEM_AUTO_DISPLAY_MODE_ID,
            resolveAutoHighRefreshDisplayModeId(
                supportedModes = plkModes,
                currentWidth = 1264,
                currentHeight = 2780,
                isColorOsFamily = true,
                oplusRefreshRateSetting = 2,
                choiceModeRate = 60f,
            ),
        )
    }

}
