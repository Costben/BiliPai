package com.android.purebilibili.core.ui.components

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AppPrimitiveComponentsStructureTest {
    @Test
    fun menuItemsUseNativeMiuixSurfacesBeforeMaterialFallback() {
        val source = loadSource().substringAfter("fun AppDropdownMenuItem(")
            .substringBefore("fun AppModalNavigationDrawer(")
        assertTrue(source.contains("LocalAppUiStyle.current.usesMiuixFamilySkin"))
        assertTrue(source.contains("AppSurface("))
        assertTrue(source.contains("enabled = enabled"))
        assertTrue(source.contains("leadingIcon?.invoke()"))
        assertTrue(source.contains("trailingIcon?.invoke()"))
        assertTrue(source.indexOf("AppSurface(") < source.indexOf("DropdownMenuItem("))
    }


    @Test
    fun singleChoiceRowsDelegateSelectionAndColorsToTheSharedThemeLayer() {
        val source = loadSource("components/AppSingleChoiceRow.kt")

        assertTrue(source.contains("fun AppSingleChoiceRow("))
        assertTrue(source.contains("AppRadioButton("))
        assertTrue(source.contains("AppSurfaceTokens.surfaceContainerHigh()"))
        assertTrue(source.contains("AppSurfaceTokens.onSurfaceContainerHigh()"))
        assertFalse(source.contains("primaryContainer"))
        assertFalse(source.contains("if (selected)"))
    }

    @Test
    fun remainingLegacyPrimitiveApisStayAvailableDuringRendererMigration() {
        val source = loadSource()

        assertTrue(source.contains("fun AppButton("))
        assertTrue(source.contains("resolveAppButtonContentPadding("))
        assertTrue(source.contains("colors: ButtonColors"))
        assertTrue(source.contains("fun AppTextButton("))
        assertTrue(source.contains("AppUiStyle.MIUIX -> AppCouiButton("))
        assertTrue(source.contains("AppUiStyle.MATERIAL3 -> Button("))
        assertTrue(source.contains("AppUiStyle.MATERIAL3 -> TextButton("))
        assertTrue(source.contains("fun AppOutlinedTextField("))
        assertTrue(source.contains("shouldUseMiuixOutlinedTextField("))
        assertTrue(source.contains("MiuixTextField("))
        assertTrue(source.contains("OutlinedTextField("))
        assertTrue(source.contains("fun AppDropdownMenu("))
        assertTrue(source.contains("fun AppModalNavigationDrawer("))
        assertTrue(source.contains("fun AppNavigationDrawerItem("))
        assertTrue(source.contains("NavigationDrawerItem("))
        assertTrue(source.contains("fun AppOutlinedButton("))
        assertTrue(source.contains("OutlinedButton("))
        assertTrue(source.contains("fun AppAssistChip("))
        assertTrue(source.contains("AssistChip("))
        assertTrue(source.contains("fun AppFilterChip("))
        assertTrue(source.contains("FilterChip("))
        assertTrue(source.contains("fun AppFloatingActionButton("))
        assertTrue(source.contains("FloatingActionButton("))
        assertTrue(source.contains("fun AppSmallFloatingActionButton("))
        assertTrue(source.contains("SmallFloatingActionButton("))
        assertTrue(source.contains("LocalAppUiStyle.current.usesMiuixFamilySkin"))
        assertTrue(source.contains("AppCouiButton("))
        assertTrue(source.contains("AppCouiChip("))
        assertTrue(source.contains("AppCouiFloatingActionButton("))
        assertTrue(source.contains("AppCouiSnackbar("))
        assertTrue(source.contains("AppCouiNavigationDrawerItem("))
        assertTrue(source.contains("MiuixButtonDefaults.buttonColors()"))
        assertTrue(source.contains("MiuixButtonDefaults.InsideMargin"))
        assertTrue(source.contains("fun AppTab("))
        assertTrue(source.contains(") = Tab("))
        assertTrue(source.contains("fun AppPrimaryTabRow("))
        assertTrue(source.contains("TabRow("))
        assertTrue(source.contains("fun AppPrimaryScrollableTabRow("))
        assertTrue(source.contains("fun AppElasticScrollableTabRow("))
        assertTrue(source.contains("minTabWidth.roundToPx()"))
        assertFalse(source.contains("import androidx.compose.material3.ScrollableTabRow"))
        assertTrue(source.contains("PiliPlusIndicatorDecelerate"))
        assertTrue(source.contains("PiliPlusIndicatorAccelerate"))
        assertTrue(source.contains("indicatorPositionProvider: (() -> Float)? = null"))
        assertEquals(2, source.windowed("divider = {}".length).count { it == "divider = {}" })
        assertFalse(source.contains("HorizontalDivider"))
        assertTrue(source.contains("resolveElasticTabIndicatorBounds("))
        assertTrue(source.contains(".wrapContentSize(Alignment.BottomStart)"))
        assertFalse(source.contains("matchContentSize = false"))
        assertTrue(source.contains("fun AppSuggestionChip("))
        assertTrue(source.contains("SuggestionChip("))
    }

    @Test
    fun nonGlassActionPrimitivesStayOnOfficialMiuixComponents() {
        val renderer = loadSource("renderer/coui/AppMiuixActionPrimitives.kt")

        assertTrue(renderer.contains("MiuixButtonDefaults.MinHeight"))
        assertTrue(renderer.contains("MiuixButtonDefaults.MinWidth"))
        assertTrue(renderer.contains("MiuixButtonDefaults.CornerRadius"))
        assertTrue(renderer.contains("resolveMiuixNonGlassChipMetrics()"))
        assertTrue(renderer.contains("MiuixFloatingActionButton("))
        assertTrue(renderer.contains("MiuixSnackbarDefaults"))
        assertTrue(renderer.contains("BasicComponent("))
        assertTrue(renderer.contains("holdDownState = selected"))
        assertFalse(renderer.contains("import androidx.compose.material3"))
    }

    @Test
    fun surfaceFacadeRoutesEachThemeToItsNativeRenderer() {
        val facade = loadSource("components/AppSurface.kt")
        val material = loadSource("renderer/material3/AppMaterial3Surface.kt")
        val miuix = loadSource("renderer/coui/AppCouiSurface.kt")

        assertTrue(facade.contains("AppUiStyle.MATERIAL3 -> AppMaterial3Surface("))
        assertTrue(facade.contains("AppUiStyle.MIUIX -> AppCouiSurface("))
        assertTrue(facade.contains("Color.Unspecified"))
        assertTrue(facade.contains("Dp.Unspecified"))
        assertFalse(facade.contains("import androidx.compose.material3"))
        assertFalse(facade.contains("import top.yukonga.miuix"))

        assertTrue(material.contains("import androidx.compose.material3.Surface"))
        assertTrue(material.contains("import androidx.compose.material3.HorizontalDivider"))
        assertTrue(miuix.contains("import io.github.suqi8.coui.kmp.basic.Surface"))
        assertTrue(miuix.contains("import io.github.suqi8.coui.kmp.basic.HorizontalDivider"))
        assertTrue(miuix.contains("interactionSource = resolvedInteractionSource"))
        assertFalse(miuix.contains("adaptiveMiuixPressFeedback("))
        assertFalse(miuix.contains("import androidx.compose.material3"))
    }

    @Test
    fun cardFacadeUsesControlledShapesAndNativeRenderers() {
        val primitiveSource = loadSource()
        val facade = loadSource("components/AppCard.kt")
        val material = loadSource("renderer/material3/AppMaterial3Card.kt")
        val miuix = loadSource("renderer/coui/AppCouiCard.kt")

        assertFalse(primitiveSource.contains("fun AppCard("))
        assertTrue(facade.contains("sealed interface AppCardShape"))
        assertTrue(facade.contains("data class Semantic(val level: ContainerLevel)"))
        assertTrue(facade.contains("data class Uniform(val cornerRadius: Dp)"))
        assertTrue(facade.contains("data class AppCardColors("))
        assertTrue(facade.contains("enum class AppCardVariant"))
        assertTrue(facade.contains("content: @Composable ColumnScope.() -> Unit"))
        assertTrue(facade.contains("AppUiStyle.MATERIAL3 -> AppMaterial3Card("))
        assertTrue(facade.contains("AppUiStyle.MIUIX -> AppCouiCard("))
        assertFalse(facade.contains("import androidx.compose.material3"))
        assertFalse(facade.contains("import top.yukonga.miuix"))
        assertFalse(facade.contains("import androidx.compose.ui.graphics.Shape"))

        assertTrue(material.contains("import androidx.compose.material3.Card"))
        assertTrue(material.contains("import androidx.compose.material3.ElevatedCard"))
        assertTrue(material.contains("uiStyle = AppUiStyle.MATERIAL3"))
        assertTrue(material.contains("is AppCardShape.Uniform -> RoundedCornerShape(cornerRadius)"))
        assertTrue(miuix.contains("import io.github.suqi8.coui.kmp.basic.Card"))
        assertTrue(miuix.contains("CardDefaults.CornerRadius"))
        assertTrue(miuix.contains("uiStyle = AppUiStyle.MIUIX"))
        assertFalse(miuix.contains("import androidx.compose.material3"))
        assertFalse(miuix.contains("import androidx.compose.ui.graphics.Shape"))
        assertFalse(facade.contains("48.dp"))
        assertFalse(material.contains("48.dp"))
        assertFalse(miuix.contains("48.dp"))
    }

    @Test
    fun listItemFacadeRoutesEachThemeToItsNativeRenderer() {
        val primitiveSource = loadSource()
        val facade = loadSource("components/AppListItem.kt")
        val material = loadSource("renderer/material3/AppMaterial3ListItem.kt")
        val miuix = loadSource("renderer/coui/AppCouiListItem.kt")

        assertFalse(primitiveSource.contains("fun AppListItem("))
        assertTrue(facade.contains("AppUiStyle.MATERIAL3 -> AppMaterial3ListItem("))
        assertTrue(facade.contains("AppUiStyle.MIUIX -> AppCouiListItem("))
        assertFalse(facade.contains("import androidx.compose.material3"))
        assertFalse(facade.contains("import top.yukonga.miuix"))

        assertTrue(material.contains("import androidx.compose.material3.ListItem"))
        assertFalse(miuix.contains("BasicComponent("))
        assertTrue(miuix.contains(".heightIn(min = AppChromeSizeTokens.CompactRowMinHeightDp.dp)"))
        assertTrue(miuix.contains("leadingContent()"))
        assertTrue(miuix.contains("trailingContent()"))
        assertFalse(miuix.contains("import androidx.compose.material3"))
    }

    @Test
    fun badgeFacadeRoutesEachThemeToItsNativeRenderer() {
        val primitiveSource = loadSource()
        val facade = loadSource("components/AppBadge.kt")
        val material = loadSource("renderer/material3/AppMaterial3Badge.kt")
        val miuix = loadSource("renderer/coui/AppCouiBadge.kt")

        assertFalse(primitiveSource.contains("fun AppBadge("))
        assertTrue(facade.contains("AppUiStyle.MATERIAL3 -> AppMaterial3Badge("))
        assertTrue(facade.contains("AppUiStyle.MIUIX -> AppCouiBadge("))
        assertTrue(facade.contains("containerColor: Color = Color.Unspecified"))
        assertTrue(facade.contains("contentColor: Color = Color.Unspecified"))
        assertFalse(facade.contains("import androidx.compose.material3"))
        assertFalse(facade.contains("import top.yukonga.miuix"))

        assertTrue(material.contains("import androidx.compose.material3.Badge"))
        assertTrue(material.contains("BadgeDefaults.containerColor"))
        assertTrue(miuix.contains("import io.github.suqi8.coui.kmp.basic.Badge"))
        assertTrue(miuix.contains("BadgeDefaults.containerColor"))
        assertTrue(miuix.contains("BadgeDefaults.contentColor"))
        assertFalse(miuix.contains("import androidx.compose.material3"))
    }


    @Test
    fun progressFacadePreservesProvidersAndRoutesToNativeRenderers() {
        val primitiveSource = loadSource()
        val facade = loadSource("components/AppProgressIndicator.kt")
        val material = loadSource("renderer/material3/AppMaterial3ProgressIndicator.kt")
        val miuix = loadSource("renderer/coui/AppMiuixProgressIndicator.kt")

        assertFalse(primitiveSource.contains("fun AppCircularProgressIndicator("))
        assertFalse(primitiveSource.contains("fun AppLinearProgressIndicator("))
        assertEquals(2, facade.lineSequence().count { it == "fun AppCircularProgressIndicator(" })
        assertEquals(2, facade.lineSequence().count { it == "fun AppLinearProgressIndicator(" })
        assertTrue(facade.contains("progress: () -> Float"))
        assertTrue(facade.contains("color: Color = Color.Unspecified"))
        assertTrue(facade.contains("strokeWidth: Dp = Dp.Unspecified"))
        assertTrue(facade.contains("AppUiStyle.MATERIAL3 -> AppMaterial3CircularProgressIndicator("))
        assertTrue(facade.contains("AppUiStyle.MIUIX -> AppCouiCircularProgressIndicator("))
        assertTrue(facade.contains("AppUiStyle.MATERIAL3 -> AppMaterial3LinearProgressIndicator("))
        assertTrue(facade.contains("AppUiStyle.MIUIX -> AppCouiLinearProgressIndicator("))
        assertFalse(facade.contains("ProgressIndicatorDefaults"))
        assertFalse(facade.contains("import androidx.compose.material3"))
        assertFalse(facade.contains("import top.yukonga.miuix"))

        assertTrue(material.contains("import androidx.compose.material3.CircularProgressIndicator"))
        assertTrue(material.contains("import androidx.compose.material3.LinearProgressIndicator"))
        assertTrue(material.contains("progress = progress,"))
        assertTrue(miuix.contains("import io.github.suqi8.coui.kmp.basic.CircularProgressIndicator"))
        assertTrue(miuix.contains("import io.github.suqi8.coui.kmp.basic.LinearProgressIndicator"))
        assertTrue(miuix.contains("progress = progress(),"))
        assertFalse(miuix.contains("import androidx.compose.material3"))
        assertFalse(facade.contains("48.dp"))
        assertFalse(material.contains("48.dp"))
        assertFalse(miuix.contains("48.dp"))
    }

    @Test
    fun primaryButtonLoadingUsesTheThemeAwareProgressFacade() {
        val source = loadSource("components/AppPrimaryButton.kt")

        assertTrue(source.contains("AppCircularProgressIndicator("))
        assertFalse(source.contains("import androidx.compose.material3.CircularProgressIndicator"))
        assertFalse(Regex("(?<!App)CircularProgressIndicator\\(").containsMatchIn(source))
    }


    @Test
    fun selectionFacadesRouteEachThemeToNativeRenderers() {
        val primitiveSource = loadSource()
        val checkboxFacade = loadSource("components/AppCheckbox.kt")
        val radioFacade = loadSource("components/AppRadioButton.kt")
        val materialCheckbox = loadSource("renderer/material3/AppMaterial3Checkbox.kt")
        val materialRadio = loadSource("renderer/material3/AppMaterial3RadioButton.kt")
        val miuixCheckbox = loadSource("renderer/coui/AppCouiCheckbox.kt")
        val miuixRadio = loadSource("renderer/coui/AppCouiRadioButton.kt")
        val miuixHaptic = loadSource("renderer/coui/AppMiuixHapticFeedback.kt")

        assertFalse(primitiveSource.contains("fun AppCheckbox("))
        assertFalse(primitiveSource.contains("fun AppRadioButton("))
        assertTrue(checkboxFacade.contains("data class AppCheckboxColors("))
        assertTrue(checkboxFacade.contains("AppUiStyle.MATERIAL3 -> AppMaterial3Checkbox("))
        assertTrue(checkboxFacade.contains("AppUiStyle.MIUIX -> AppCouiCheckbox("))
        assertTrue(radioFacade.contains("AppUiStyle.MATERIAL3 -> AppMaterial3RadioButton("))
        assertTrue(radioFacade.contains("AppUiStyle.MIUIX -> AppCouiRadioButton("))
        assertFalse(checkboxFacade.contains("import androidx.compose.material3"))
        assertFalse(radioFacade.contains("import androidx.compose.material3"))

        assertTrue(materialCheckbox.contains("import androidx.compose.material3.Checkbox"))
        assertTrue(materialRadio.contains("import androidx.compose.material3.RadioButton"))
        assertTrue(miuixCheckbox.contains("import io.github.suqi8.coui.kmp.basic.Checkbox"))
        assertTrue(miuixCheckbox.contains("ToggleableState.On"))
        assertTrue(miuixRadio.contains("import io.github.suqi8.coui.kmp.basic.RadioButton"))
        assertTrue(miuixCheckbox.contains("ProvideAppMiuixHapticFeedback"))
        assertTrue(miuixRadio.contains("ProvideAppMiuixHapticFeedback"))
        assertTrue(miuixHaptic.contains("LocalAppThemeConfig.current.hapticFeedbackEnabled"))
        assertTrue(miuixHaptic.contains("NoOpHapticFeedback"))
        assertFalse(miuixCheckbox.contains("import androidx.compose.material3"))
        assertFalse(miuixRadio.contains("import androidx.compose.material3"))
        assertFalse(
            loadSource("components/AppSelectionPreferenceComponents.kt")
                .contains("modifier = Modifier.size(48.dp)"),
        )
    }

    @Test
    fun textAndIconFacadesRouteEachThemeToNativeRenderers() {
        val primitiveSource = loadSource()
        val textFacade = loadSource("components/AppText.kt")
        val iconFacade = loadSource("components/AppIcon.kt")
        val themeDefaults = loadSource("AppPrimitiveThemeDefaults.kt")
        val materialText = loadSource("renderer/material3/AppMaterial3Text.kt")
        val materialIcon = loadSource("renderer/material3/AppMaterial3Icon.kt")
        val miuixText = loadSource("renderer/coui/AppCouiText.kt")
        val miuixIcon = loadSource("renderer/coui/AppCouiIcon.kt")

        assertFalse(primitiveSource.contains("fun AppText("))
        assertFalse(primitiveSource.contains("fun AppIcon("))
        assertEquals(4, textFacade.lineSequence().count { it == "fun AppText(" })
        assertEquals(4, iconFacade.lineSequence().count { it == "fun AppIcon(" })
        assertTrue(textFacade.contains("AppUiStyle.MATERIAL3 -> AppMaterial3Text("))
        assertTrue(textFacade.contains("AppUiStyle.MIUIX -> AppCouiText("))
        assertTrue(iconFacade.contains("AppUiStyle.MATERIAL3 -> AppMaterial3Icon("))
        assertTrue(iconFacade.contains("AppUiStyle.MIUIX -> AppCouiIcon("))
        assertTrue(textFacade.contains("globalTextTapCopy"))
        assertFalse(textFacade.contains("import androidx.compose.material3"))
        assertFalse(textFacade.contains("import top.yukonga.miuix"))
        assertFalse(iconFacade.contains("import androidx.compose.material3"))
        assertFalse(iconFacade.contains("import top.yukonga.miuix"))

        assertTrue(themeDefaults.contains("MaterialLocalTextStyle.current"))
        assertTrue(themeDefaults.contains("COUITheme.textStyles.main"))
        assertFalse(themeDefaults.contains("LocalTextStyles"))
        assertFalse(themeDefaults.contains(".kmp.basic."))
        assertTrue(materialText.contains("import androidx.compose.material3.Text"))
        assertTrue(materialIcon.contains("import androidx.compose.material3.Icon"))
        assertTrue(miuixText.contains("import io.github.suqi8.coui.kmp.basic.Text"))
        assertTrue(miuixIcon.contains("import io.github.suqi8.coui.kmp.basic.Icon"))
        assertFalse(miuixText.contains("import androidx.compose.material3"))
        assertFalse(miuixIcon.contains("import androidx.compose.material3"))
    }

    private fun loadSource(): String {
        val path = "src/main/java/com/android/purebilibili/core/ui/components/AppPrimitiveComponents.kt"
        return listOf(
            File(path),
            File("design-system/$path"),
        ).firstOrNull(File::exists)?.readText()
            ?: error("Cannot locate AppPrimitiveComponents.kt from ${File(".").absolutePath}")
    }

    private fun loadSource(relativePath: String): String {
        val path = "src/main/java/com/android/purebilibili/core/ui/$relativePath"
        return listOf(
            File(path),
            File("design-system/$path"),
        ).firstOrNull(File::exists)?.readText()
            ?: error("Cannot locate $path from ${File(".").absolutePath}")
    }
}
