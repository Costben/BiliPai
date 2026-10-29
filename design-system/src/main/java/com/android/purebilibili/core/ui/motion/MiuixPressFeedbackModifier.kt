package com.android.purebilibili.core.ui.motion

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import io.github.suqi8.coui.kmp.utils.PressFeedbackType
import io.github.suqi8.coui.kmp.utils.SinkFeedback
import io.github.suqi8.coui.kmp.utils.TiltFeedback
import io.github.suqi8.coui.kmp.utils.pressable

/**
 * 依据当前系统主题自适应应用 Miuix 官方触控按压反馈。
 *
 * 仅在 Miuix 且液态玻璃关闭时应用 0.94x 下沉微缩与 Folme 回弹。
 * Material 3 与开玻璃路径保持 No-op。
 *
 * @param enabled 是否开启按压反馈
 * @param feedbackType 反馈效果类型，默认 [PressFeedbackType.Sink]
 * @param interactionSource 交互事件流；若不传入则在内部自动创建并维持
 */
@Composable
fun Modifier.adaptiveMiuixPressFeedback(
    enabled: Boolean = true,
    feedbackType: PressFeedbackType = PressFeedbackType.Sink,
    interactionSource: MutableInteractionSource? = null,
): Modifier {
    if (!enabled || feedbackType == PressFeedbackType.None) return this
    if (!com.android.purebilibili.core.ui.isMiuixNonGlassEnabled()) return this

    val resolvedInteractionSource = interactionSource ?: remember { MutableInteractionSource() }
    val feedback = remember(feedbackType) {
        when (feedbackType) {
            // COUI 的 Tint 由组件自身的按压底色实现，没有独立的 Indication 可挂。
            PressFeedbackType.None, PressFeedbackType.Tint -> null
            PressFeedbackType.Sink -> SinkFeedback()
            PressFeedbackType.Tilt -> TiltFeedback()
        }
    }

    return this.pressable(
        interactionSource = resolvedInteractionSource,
        indication = feedback,
        delay = null,
    )
}
