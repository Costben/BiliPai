package com.android.purebilibili.core.ui.popup

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.android.purebilibili.core.ui.components.LocalCouiPopupHostAvailable
import io.github.suqi8.coui.kmp.basic.Scaffold
import io.github.suqi8.coui.kmp.blur.LocalBackdrop
import io.github.suqi8.coui.kmp.blur.layerBackdrop
import io.github.suqi8.coui.kmp.blur.rememberLayerBackdrop
import io.github.suqi8.coui.kmp.utils.COUIPopupUtils

/**
 * COUI 17 的同窗弹窗宿主。
 *
 * COUI 的弹窗有两条路径：Dialog 是一条独立窗口，采不到页面，底板只能是不透明纯色；
 * 只有落在同一个窗口里的 overlay 才能拿到 [LocalBackdrop]，画出真正的模糊、描边和内阴影。
 * 这个宿主把整棵内容录进一张 layer backdrop，并在上面挂 COUI 的弹窗层。
 *
 * 没有这层宿主的界面（例如播放器 Activity）里，`AppDropdownMenu` 会退回 Dialog 路径，
 * 所以菜单不会因为缺宿主而消失。
 */
@Composable
fun CouiWindowPopupHost(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val screenBackdrop = rememberLayerBackdrop { drawContent() }
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        popupHost = {
            // 弹窗层是录制内容的兄弟节点，所以弹窗永远采不到自己。
            CompositionLocalProvider(LocalBackdrop provides screenBackdrop) {
                COUIPopupUtils.COUIPopupHost()
            }
        },
    ) {
        CompositionLocalProvider(LocalCouiPopupHostAvailable provides true) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .layerBackdrop(screenBackdrop),
            ) {
                content()
            }
        }
    }
}
