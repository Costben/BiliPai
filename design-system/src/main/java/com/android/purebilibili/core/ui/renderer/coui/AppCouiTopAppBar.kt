package com.android.purebilibili.core.ui.renderer.coui

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.github.suqi8.coui.kmp.basic.PinnedTopAppBar
import io.github.suqi8.coui.kmp.basic.SmallTitle
import io.github.suqi8.coui.kmp.basic.SmallTopAppBar
import io.github.suqi8.coui.kmp.basic.TopAppBar
import io.github.suqi8.coui.kmp.basic.ScrollBehavior
import io.github.suqi8.coui.kmp.theme.COUITheme

@Composable
fun AppCouiSmallTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: (@Composable RowScope.() -> Unit)? = null,
    scrollBehavior: ScrollBehavior? = null,
) {
    SmallTopAppBar(
        title = title,
        modifier = modifier,
        color = color,
        navigationIcon = navigationIcon ?: {},
        actions = actions ?: {},
        scrollBehavior = scrollBehavior,
    )
}

@Composable
fun AppCouiPinnedTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: (@Composable RowScope.() -> Unit)? = null,
    scrollBehavior: ScrollBehavior? = null,
) {
    PinnedTopAppBar(
        title = title,
        modifier = modifier,
        containerColor = color,
        navigationIcon = navigationIcon ?: {},
        actions = actions ?: {},
        scrollBehavior = scrollBehavior,
    )
}

@Composable
fun AppCouiLargeTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: (@Composable RowScope.() -> Unit)? = null,
    scrollBehavior: ScrollBehavior? = null,
) {
    TopAppBar(
        title = title,
        modifier = modifier,
        color = color,
        navigationIcon = navigationIcon ?: {},
        actions = actions ?: {},
        scrollBehavior = scrollBehavior,
    )
}
