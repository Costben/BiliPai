package com.android.purebilibili.navigation3.predictiveback

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableFloatStateOf

/**
 * 卡片开合的几何进度：0 = 停在来源卡片的矩形上，1 = 整屏。
 *
 * 让「谁在画」和「谁在计时」解耦——进度只由 entry 那一层 `graphicsLayer` 在绘制阶段读取，
 * 写它不会引起任何重组；计时由两段按帧循环推进。
 *
 * 必须自己计时，不能只跟导航驱动：驱动的行程从压栈那一帧开始算，而详情页首帧要一两百
 * 毫秒才画得出来。只跟驱动的话，用户看到的是「点一下，整屏页面直接出现」——也就是硬切。
 * 这里等 entry 第一帧真正绘制之后再起跑，展开一定从卡片大小开始。
 *
 * 两段循环都可能撞车（返回还没走完就点了另一张卡），用世代号隔离：旧循环拿着过期的
 * 世代号，写不进来。
 */
@Stable
internal class VideoCardMorphDriver {

    val progress = mutableFloatStateOf(1f)

    /** 当前这一段的世代号，任何新动作都会让它变。 */
    private var epoch = 0

    /** 由发起进场的 entry 的 `remember` 持有，用来识别「同一张卡的重组」。 */
    private var armedToken: Any? = null

    /** 进场循环是否还有权写。返回开始时关掉，避免两段循环互相覆盖。 */
    private var entryWritable = false

    /** 这一段返回是否还没结束（结束前不允许 [settle] 抢先把页面推回整屏）。 */
    private var returnInFlight = false

    /**
     * 新开一张卡：先把页面按在卡片矩形上，再交给按帧循环推到 1。
     *
     * 同一个 [token] 重复调用（同一张卡反复重组）不会重新压回 0；只有重新进入导航栈、
     * 拿到新 token 才会重来一次。
     */
    fun arm(token: Any) {
        if (armedToken === token) return
        armedToken = token
        entryWritable = true
        returnInFlight = false
        epoch += 1
        progress.floatValue = 0f
    }

    /** 进场循环写一档。token 已失效或返回已经开始就不写。 */
    fun writeEntry(token: Any, value: Float) {
        if (!entryWritable || armedToken !== token) return
        progress.floatValue = value
    }

    /**
     * 返回开始：开一段新的收缩，返回本段世代号。进场循环就此交权。
     *
     * 收缩从**当前进度**继续往下走，不从 1 重新起算：手势刚弹出就返回时，进场可能才走到
     * 一半，硬跳回 1 会看见页面先弹大一下再缩回去。
     */
    fun beginReturn(): Int {
        entryWritable = false
        epoch += 1
        returnInFlight = true
        return epoch
    }

    /** 返回循环写一档。世代号过期（又开了新卡）就不写。 */
    fun writeReturn(epoch: Int, value: Float) {
        if (epoch != this.epoch) return
        progress.floatValue = value
        if (value <= 0f) returnInFlight = false
    }

    /** 返回是否还在进行中：用来避免弹出后的兜底循环重复开一段。 */
    fun isReturning(): Boolean = returnInFlight

    /** 回到整屏，并让所有在途循环交权。返回已开始时不抢。 */
    fun settle() {
        if (returnInFlight) return
        entryWritable = false
        epoch += 1
        progress.floatValue = 1f
    }

    fun value(): Float = progress.floatValue
}
