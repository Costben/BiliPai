package com.android.purebilibili.core.ui.renderer.coui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.suqi8.coui.kmp.basic.BasicComponentColors
import io.github.suqi8.coui.kmp.basic.BasicComponentDefaults
import io.github.suqi8.coui.kmp.basic.CardListPosition
import io.github.suqi8.coui.kmp.basic.SwitchColors
import io.github.suqi8.coui.kmp.basic.SwitchDefaults
import io.github.suqi8.coui.kmp.preference.ArrowPreference as CouiArrowPreference
import io.github.suqi8.coui.kmp.preference.SwitchPreference as CouiSwitchPreference
import io.github.suqi8.coui.kmp.preference.CheckboxPreference as CouiCheckboxPreference
import io.github.suqi8.coui.kmp.preference.RadioButtonPreference as CouiRadioButtonPreference
import io.github.suqi8.coui.kmp.preference.SliderPreference as CouiSliderPreference

@Composable
fun AppCouiArrowPreference(
    title: String,
    modifier: Modifier = Modifier,
    summary: String? = null,
    startAction: (@Composable () -> Unit)? = null,
    endActions: (@Composable RowScope.() -> Unit)? = null,
    bottomAction: (@Composable () -> Unit)? = null,
    insideMargin: PaddingValues = PaddingValues(),
    cardListPosition: CardListPosition = CardListPosition.None,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
) {
    CouiArrowPreference(
        title = title,
        modifier = modifier,
        summary = summary,
        startAction = startAction,
        endActions = endActions ?: {},
        bottomAction = bottomAction,
        insideMargin = insideMargin,
        cardListPosition = cardListPosition,
        enabled = enabled,
        onClick = onClick,
    )
}

@Composable
fun AppCouiSwitchPreference(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    summary: String? = null,
    startAction: (@Composable () -> Unit)? = null,
    endActions: (@Composable RowScope.() -> Unit)? = null,
    bottomAction: (@Composable () -> Unit)? = null,
    switchColors: SwitchColors = SwitchDefaults.switchColors(),
    insideMargin: PaddingValues = PaddingValues(),
    cardListPosition: CardListPosition = CardListPosition.None,
    enabled: Boolean = true,
) {
    CouiSwitchPreference(
        checked = checked,
        onCheckedChange = onCheckedChange,
        title = title,
        modifier = modifier,
        summary = summary,
        startAction = startAction,
        endActions = endActions ?: {},
        bottomAction = bottomAction,
        switchColors = switchColors,
        insideMargin = insideMargin,
        cardListPosition = cardListPosition,
        enabled = enabled,
    )
}
