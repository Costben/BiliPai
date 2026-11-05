package com.android.purebilibili.core.store

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.map

/**
 * 全局实时模糊总开关。
 *
 * 四个细粒度开关（顶栏 / 底栏 / 渐进顶栏 / 液态玻璃）只决定模糊出现在哪里；总开关决定
 * 整个应用是否允许实时模糊。关闭时 COUI 组件回落到不透明底色，也不再录制背景源。
 *
 * 默认开启，与四个细粒度开关独立：总开关关闭时它们全部失效，整个应用回落为纯色。 */
object BlurSettingsStore {
    private val globalBlurKey = booleanPreferencesKey("global_blur_enabled")

    fun globalBlurEnabled(context: Context) = context.settingsDataStore.data.map {
        it[globalBlurKey] ?: true
    }

    suspend fun setGlobalBlurEnabled(context: Context, enabled: Boolean) {
        context.settingsDataStore.edit { it[globalBlurKey] = enabled }
    }
}
