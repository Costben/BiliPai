package com.android.purebilibili.feature.home

import com.android.purebilibili.data.model.response.VideoItem
import com.android.purebilibili.feature.home.components.cards.resolveVideoCardCoverCacheKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HomeCoverRequestPolicyTest {

    @Test
    fun `preload source uses the displayed cover identity and sized URL`() {
        val video = VideoItem(bvid = "BV1cover", pic = "https://example.com/cover.jpg")
        val spec = HomeCoverRequestSpec(640, 400)
        val source = resolveHomeCoverImageSource(video, false, spec)

        assertEquals(spec.resolveUrl(video.pic), source.url)
        assertEquals(resolveVideoCardCoverCacheKey(video, false, spec), source.cacheKey)
        assertTrue(source.cacheKey.endsWith("640x400"))
    }

    @Test
    fun `changing decode size or quality cannot reuse a differently sized cover`() {
        val video = VideoItem(bvid = "BV1cover", pic = "https://example.com/cover.jpg")
        val normal = resolveHomeCoverImageSource(video, false, HomeCoverRequestSpec(640, 400))
        val larger = resolveHomeCoverImageSource(video, false, HomeCoverRequestSpec(960, 600))
        val saver = resolveHomeCoverImageSource(video, true, HomeCoverRequestSpec(240, 150))

        assertTrue(normal.cacheKey != larger.cacheKey)
        assertTrue(normal.cacheKey != saver.cacheKey)
        assertTrue(normal.url != larger.url)
        assertTrue(normal.url != saver.url)
    }

    @Test
    fun `normal cover selects the smallest tier that still covers the displayed width`() {
        val spec = resolveHomeCoverRequestSpec(
            cardWidthDp = 180f,
            density = 3f,
            useLowQualityCover = false,
        )

        // 180dp × 3 = 540px，640 档已覆盖，不再按 1.25 倍超采样跳到 960。
        assertEquals(640, spec.widthPx)
        assertEquals(400, spec.heightPx)
        assertEquals("640x400", spec.cacheKeySuffix)
    }

    @Test
    fun `two column cover on a 620dpi phone does not oversample to the 960 tier`() {
        val spec = resolveHomeCoverRequestSpec(
            cardWidthDp = 148f,
            density = 3.875f,
            useLowQualityCover = false,
        )

        // 被测设备实测卡片显示宽约 573px，解码面积比 960 档少一半以上。
        assertEquals(640, spec.widthPx)
        assertTrue(spec.widthPx >= 148f * 3.875f)
    }

    @Test
    fun `large cover clamps to largest tier instead of requesting original`() {
        val spec = resolveHomeCoverRequestSpec(
            cardWidthDp = 600f,
            density = 3f,
            useLowQualityCover = false,
        )

        assertEquals(1280, spec.widthPx)
        assertTrue(spec.resolveUrl("https://example.com/cover.jpg").endsWith("@1280w_800h.webp"))
    }

    @Test
    fun `data saver continues using 240 tier`() {
        val spec = resolveHomeCoverRequestSpec(
            cardWidthDp = 600f,
            density = 4f,
            useLowQualityCover = true,
        )

        assertEquals(HomeCoverRequestSpec(240, 150), spec)
    }
}
