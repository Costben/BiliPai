package com.android.purebilibili.core.ui.migration

import java.io.File
import java.security.MessageDigest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * 双主题原生组件迁移的存量棘轮。
 *
 * 判定语义（2026-11-03 重新冻结）：
 *
 * 1. 「是否直连旧皮肤」按文件内的 import 解析结果判定，不再按调用名判定。
 *    每个文件先建立「本地名 → 全限定名」映射，调用 `Name(` 时依次查：
 *    别名/显式 import 的本地名 → 末段同名的显式 import → 通配 import（`import a.b.*`）→
 *    都没有时退回按名字判定。因此 `import ...AppSlider as Slider` 之后的 `Slider(...)`、
 *    以及 `import io.github.suqi8.coui.kmp.basic.Card` 之后的 `Card(...)` 都不再计为债务。
 *    `SliderDefaults.colors(`、`IconButtonDefaults.` 这类成员引用走同一套解析。
 * 2. 只有 `androidx.compose.material3.` 与 `top.yukonga.miuix.` 属于旧皮肤债务。
 *    `com.android.purebilibili.`（App 门面）与 `io.github.suqi8.coui.`（迁移目标 COUI 17）
 *    一律不计入，COUI 的 import 也不再出现在任何债务计数里。
 * 3. 上限等于重新冻结时生产源码的真实值，不留余量。完成一个迁移批次后同步调低；
 *    不允许为了让测试通过而提高上限。
 *
 * 已删除 `liquidGlassExceptionSetCannotExpandOrChangeIdentity`：它钉死的是使用
 * `io.github.suqi8.coui.kmp.blur./shader./squircle.` 的文件集合。COUI 17 是迁移目标而不是债务，
 * 玻璃材质向更多界面扩散属于预期迁移行为，该测试的前提已不成立。
 * 共享 Miuix navigation（`top.yukonga.miuix.kmp.nav.`）仍是真正的 Miuix 例外，保留精确集合钉死。
 */
class NativeThemeMigrationBoundaryTest {

    @Test
    fun featureVendorImportsOnlyDecrease() {
        val featureFiles = kotlinFiles("app/src/main/java/com/android/purebilibili/feature")

        assertAtMost(
            actual = featureFiles.countFilesWithImportPrefix(MATERIAL3_IMPORT),
            maximum = MAX_FEATURE_MATERIAL3_FILES,
            label = "feature Material3 import files",
        )
        assertAtMost(
            actual = featureFiles.countImportLines(MATERIAL3_IMPORT),
            maximum = MAX_FEATURE_MATERIAL3_IMPORTS,
            label = "feature Material3 imports",
        )
        assertAtMost(
            actual = featureFiles.countImportLines("import androidx.compose.material3.*"),
            maximum = MAX_FEATURE_MATERIAL3_WILDCARD_IMPORTS,
            label = "feature Material3 wildcard imports",
        )
        assertAtMost(
            actual = featureFiles.countFilesWithAnyImportPrefix(MIUIX_COMPONENT_IMPORTS),
            maximum = MAX_FEATURE_MIUIX_COMPONENT_FILES,
            label = "feature Miuix visible-component import files",
        )
        assertAtMost(
            actual = featureFiles.countImportLines(MIUIX_COMPONENT_IMPORTS),
            maximum = MAX_FEATURE_MIUIX_COMPONENT_IMPORTS,
            label = "feature Miuix visible-component imports",
        )
    }

    @Test
    fun featureThemeBranchingOnlyDecreases() {
        val featureFiles = kotlinFiles("app/src/main/java/com/android/purebilibili/feature")
        val branchLines = featureFiles.flatMap { file ->
            file.readLines().filter { line ->
                line.contains("LocalAppUiStyle") || APP_UI_STYLE_BRANCH.containsMatchIn(line)
            }
        }
        val branchFiles = featureFiles.count { file ->
            file.useLines { lines ->
                lines.any { line ->
                    line.contains("LocalAppUiStyle") || APP_UI_STYLE_BRANCH.containsMatchIn(line)
                }
            }
        }

        assertAtMost(branchFiles, MAX_FEATURE_THEME_BRANCH_FILES, "feature theme-branch files")
        assertAtMost(branchLines.size, MAX_FEATURE_THEME_BRANCH_LINES, "feature theme-branch lines")
    }

    @Test
    fun featureSliderColorsUseNeutralAppTokens() {
        val offenders = kotlinFiles("app/src/main/java/com/android/purebilibili/feature")
            .nativeCallOffenders(MATERIAL_SLIDER_DEFAULTS_CALL)

        assertTrue(
            offenders.isEmpty(),
            "AppSlider colors must use AppSliderDefaults, not Material3 SliderDefaults:\n" +
                offenders.joinToString("\n"),
        )
    }

    @Test
    fun appDirectSliderCallsUseFacade() {
        val offenders = kotlinFiles("app/src/main/java")
            .nativeCallOffenders(DIRECT_NATIVE_SLIDER_CALL)

        assertTrue(
            offenders.isEmpty(),
            "App production sources must use AppSlider, not a direct native Slider:\n" +
                offenders.joinToString("\n"),
        )
    }

    @Test
    fun appDirectButtonCallsOnlyDecrease() {
        val offenders = kotlinFiles("app/src/main/java")
            .nativeCallOffenders(DIRECT_NATIVE_BUTTON_CALL)

        assertAtMostOffenders(
            offenders = offenders,
            maximum = MAX_APP_DIRECT_BUTTON_CALLS,
            label = "app direct native Button/TextButton/OutlinedButton calls",
        )
    }

    @Test
    fun appIconButtonVisualGeometryDoesNotReuseTouchTargetDimensions() {
        val offenders = kotlinFiles("app/src/main/java")
            .flatMap { file ->
                file.callHeaders("AppIconButton").mapNotNull { (lineNumber, header) ->
                    if (FIXED_ICON_BUTTON_DIMENSION.containsMatchIn(header)) {
                        "${repoRelativePath(file)}:$lineNumber: ${header.trim()}"
                    } else {
                        null
                    }
                }
            }

        assertAtMostOffenders(
            offenders = offenders,
            maximum = MAX_APP_ICON_BUTTON_FIXED_DIMENSION_CALLS,
            label = "AppIconButton calls with a fixed 48dp touch-target dimension",
        )
    }

    @Test
    fun appDirectIconButtonCallsAndDefaultsOnlyDecrease() {
        val offenders = kotlinFiles("app/src/main/java")
            .flatMap { file ->
                val importTable = file.importTable()
                file.readLines().mapIndexedNotNull { index, line ->
                    if (
                        importTable.hasDirectNativeCall(DIRECT_NATIVE_ICON_BUTTON_CALL, line) ||
                        importTable.hasDirectNativeCall(MATERIAL_ICON_BUTTON_DEFAULTS_REFERENCE, line) ||
                        DIRECT_ICON_BUTTON_VENDOR_IMPORT.containsMatchIn(line)
                    ) {
                        "${repoRelativePath(file)}:${index + 1}: $line"
                    } else {
                        null
                    }
                }
            }

        assertAtMostOffenders(
            offenders = offenders,
            maximum = MAX_APP_DIRECT_ICON_BUTTON_CALLS,
            label = "app direct native IconButton calls / defaults / vendor imports",
        )
    }

    @Test
    fun featureDirectProgressCallsOnlyDecrease() {
        val directCalls = kotlinFiles("app/src/main/java/com/android/purebilibili/feature")
            .nativeCallOffenders(DIRECT_PROGRESS_INDICATOR_CALL)

        assertAtMost(
            actual = directCalls.size,
            maximum = MAX_FEATURE_DIRECT_PROGRESS_CALLS,
            label = "feature direct native progress calls",
        )
    }

    @Test
    fun designSystemComponentsUseTheProgressFacade() {
        val offenders = kotlinFiles(
            "design-system/src/main/java/com/android/purebilibili/core/ui/components"
        )
            .filterNot { it.name == "AppProgressIndicator.kt" }
            .nativeCallOffenders(DIRECT_PROGRESS_INDICATOR_CALL)

        assertTrue(
            offenders.isEmpty(),
            "Design-system components must use App* progress facades:\n" +
                offenders.joinToString("\n"),
        )
    }

    @Test
    fun appNativeCardCallsUseOnlyAppFacades() {
        val directCalls = kotlinFiles("app/src/main/java")
            .flatMap { file ->
                val importTable = file.importTable()
                var enclosingFunction = "<top-level>"
                buildList {
                    file.readLines().forEach { line ->
                        FUNCTION_DECLARATION.find(line)?.groupValues?.get(1)?.let {
                            enclosingFunction = it
                        }
                        DIRECT_NATIVE_CARD_CALL.findAll(line).forEach { match ->
                            if (importTable.isDirectNativeCall(match.groupValues[1])) {
                                add(
                                    "${repoRelativePath(file)}#$enclosingFunction#" +
                                        match.value.removeSuffix("(")
                                )
                            }
                        }
                    }
                }
            }
            .sorted()

        assertEquals(MD3_CARD_PREVIEW_EXCEPTIONS, directCalls)
    }

    @Test
    fun primitiveFacadeVendorImportsOnlyDecrease() {
        val source = repoFile(
            "design-system/src/main/java/com/android/purebilibili/core/ui/components/" +
                "AppPrimitiveComponents.kt"
        )
        val imports = source.readLines().count { it.startsWith(MATERIAL3_IMPORT) }

        assertAtMost(
            actual = imports,
            maximum = MAX_PRIMITIVE_FACADE_MATERIAL3_IMPORTS,
            label = "AppPrimitiveComponents Material3 imports",
        )
    }

    @Test
    fun rendererPackagesCannotCrossVendorBoundaries() {
        val materialRenderers = kotlinFiles(
            "design-system/src/main/java/com/android/purebilibili/core/ui/renderer/material3"
        )
        val miuixRenderers = kotlinFiles(
            "design-system/src/main/java/com/android/purebilibili/core/ui/renderer/miuix"
        )

        val materialOffenders = materialRenderers.importOffenders(MIUIX_VENDOR_IMPORT)
        val miuixOffenders = miuixRenderers.importOffenders(MATERIAL3_IMPORT)

        assertTrue(
            materialOffenders.isEmpty(),
            "Material3 renderer must not import Miuix:\n${materialOffenders.joinToString("\n")}",
        )
        assertTrue(
            miuixOffenders.isEmpty(),
            "Miuix renderer must not import Material3:\n${miuixOffenders.joinToString("\n")}",
        )
    }

    @Test
    fun sharedMiuixNavigationExceptionSetCannotExpandOrChangeIdentity() {
        val sourceFiles = kotlinFiles("app/src/main/java") + kotlinFiles("design-system/src/main/java")
        val navigationFiles = sourceFiles
            .filter { it.hasAnyImportPrefix(listOf(MIUIX_NAVIGATION_IMPORT)) }
            .map(::repoRelativePath)
            .toSet()

        assertEquals(MIUIX_NAVIGATION_EXCEPTION_FILE_COUNT, navigationFiles.size)
        assertEquals(MIUIX_NAVIGATION_EXCEPTION_PATHS_SHA256, sha256(navigationFiles))
    }

    /**
     * L2 直读预算：这些 token 不经过 App* 语义层，是后续批次可以直接下降的存量。
     * 四类各自冻结，不合并成总数，避免一类下降掩盖另一类增长。
     */
    @Test
    fun featureTokenDirectReadsOnlyDecrease() {
        val appSources = kotlinFiles("app/src/main/java")

        assertAtMost(
            actual = appSources.countOccurrences("MaterialTheme.typography"),
            maximum = MAX_APP_MATERIAL_THEME_TYPOGRAPHY_READS,
            label = "app MaterialTheme.typography direct reads",
        )
        assertAtMost(
            actual = appSources.countOccurrences("MaterialTheme.colorScheme"),
            maximum = MAX_APP_MATERIAL_THEME_COLOR_SCHEME_READS,
            label = "app MaterialTheme.colorScheme direct reads",
        )
        assertAtMost(
            actual = appSources.countOccurrences("Color(0x"),
            maximum = MAX_APP_HARDCODED_COLOR_LITERALS,
            label = "app hardcoded Color(0x…) literals",
        )
        assertAtMost(
            actual = appSources.countOccurrences("RoundedCornerShape("),
            maximum = MAX_APP_ROUNDED_CORNER_SHAPE_LITERALS,
            label = "app RoundedCornerShape(…) literals",
        )
    }

    private fun List<File>.countFilesWithImportPrefix(prefix: String): Int =
        count { it.hasAnyImportPrefix(listOf(prefix)) }

    private fun List<File>.countFilesWithAnyImportPrefix(prefixes: List<String>): Int =
        count { it.hasAnyImportPrefix(prefixes) }

    private fun List<File>.countImportLines(prefix: String): Int = countImportLines(listOf(prefix))

    private fun List<File>.countImportLines(prefixes: List<String>): Int = sumOf { file ->
        file.useLines { lines ->
            lines.count { line -> prefixes.any(line::startsWith) }
        }
    }

    private fun List<File>.countOccurrences(needle: String): Int = sumOf { file ->
        val text = file.readText()
        var count = 0
        var index = text.indexOf(needle)
        while (index >= 0) {
            count++
            index = text.indexOf(needle, index + needle.length)
        }
        count
    }

    private fun List<File>.importOffenders(prefix: String): List<String> = flatMap { file ->
        file.readLines().mapIndexedNotNull { index, line ->
            if (line.startsWith(prefix)) {
                "${repoRelativePath(file)}:${index + 1}: $line"
            } else {
                null
            }
        }
    }

    /**
     * 按 import 解析结果筛出直连旧皮肤的调用行。正则第 1 组必须是被调用的简单名，
     * 解析不到（同包 / 未导入 / 全限定调用）时退回按名字判定，宁可计入也不漏计。
     */
    private fun List<File>.nativeCallOffenders(regex: Regex): List<String> = flatMap { file ->
        val importTable = file.importTable()
        file.readLines().mapIndexedNotNull { index, line ->
            if (importTable.hasDirectNativeCall(regex, line)) {
                "${repoRelativePath(file)}:${index + 1}: $line"
            } else {
                null
            }
        }
    }

    private fun File.hasAnyImportPrefix(prefixes: List<String>): Boolean = useLines { lines ->
        lines.any { line -> prefixes.any(line::startsWith) }
    }

    private fun File.importTable(): ImportTable = importTableCache.getOrPut(this) {
        ImportTable.parse(this)
    }

    private fun File.callHeaders(callName: String): List<Pair<Int, String>> {
        val marker = "$callName("
        var startLine = -1
        var parenthesisDepth = 0
        var header = StringBuilder()

        return buildList {
            readLines().forEachIndexed { index, line ->
                if (startLine < 0) {
                    val markerIndex = line.indexOf(marker)
                    if (markerIndex < 0) return@forEachIndexed
                    startLine = index + 1
                    val firstLine = line.substring(markerIndex)
                    header.appendLine(firstLine)
                    parenthesisDepth += firstLine.count { it == '(' } - firstLine.count { it == ')' }
                } else {
                    header.appendLine(line)
                    parenthesisDepth += line.count { it == '(' } - line.count { it == ')' }
                }

                if (parenthesisDepth <= 0) {
                    add(startLine to header.toString())
                    startLine = -1
                    parenthesisDepth = 0
                    header = StringBuilder()
                }
            }
            check(startLine < 0) { "Unterminated $callName call header in ${path}" }
        }
    }

    private fun assertAtMost(actual: Int, maximum: Int, label: String) {
        assertTrue(
            actual <= maximum,
            "$label increased to $actual (frozen maximum: $maximum). " +
                "Route feature UI through App* and lower the migration budget after each batch.",
        )
    }

    private fun assertAtMostOffenders(offenders: List<String>, maximum: Int, label: String) {
        assertTrue(
            offenders.size <= maximum,
            "$label increased to ${offenders.size} (frozen maximum: $maximum). " +
                "Route these call sites through App* and lower the migration budget:\n" +
                offenders.joinToString("\n"),
        )
    }

    private fun kotlinFiles(path: String): List<File> = repoFile(path)
        .walkTopDown()
        .filter { it.isFile && it.extension == "kt" }
        .toList()

    private fun repoFile(path: String): File = File(repositoryRoot(), path)

    private fun repoRelativePath(file: File): String = file
        .relativeTo(repositoryRoot())
        .invariantSeparatorsPath

    private fun repositoryRoot(): File {
        val cwd = File(".").absoluteFile.canonicalFile
        return when {
            File(cwd, "app/src/main/java").isDirectory -> cwd
            File(cwd, "src/main/java").isDirectory && File(cwd.parentFile, "design-system").isDirectory ->
                cwd.parentFile.canonicalFile
            else -> error("Cannot locate repository root from ${cwd.path}")
        }
    }

    private fun sha256(values: Set<String>): String {
        val bytes = values.sorted().joinToString("\n").toByteArray(Charsets.UTF_8)
        return MessageDigest.getInstance("SHA-256")
            .digest(bytes)
            .joinToString("") { byte ->
                (byte.toInt() and 0xff).toString(16).padStart(2, '0')
            }
    }

    private val importTableCache = HashMap<File, ImportTable>()

    /**
     * 一个 Kotlin 文件的 import 视图。
     *
     * - [byLocalName]：本地名（有 `as` 时取别名，否则取末段）→ 全限定名。
     * - [byLastSegment]：末段 → 全限定名集合，用于「别名改了本地名但仍按原名调用」的兜底。
     * - [wildcardPackages]：`import a.b.*` 记录的包前缀（含结尾的点）。
     */
    private class ImportTable(
        private val byLocalName: Map<String, String>,
        private val byLastSegment: Map<String, Set<String>>,
        private val wildcardPackages: List<String>,
    ) {
        /** 解析失败返回 null，调用方据此退回按名字判定。 */
        fun resolve(name: String): String? {
            byLocalName[name]?.let { return it }
            byLastSegment[name]?.singleOrNull()?.let { return it }
            // 显式 import 覆盖通配 import。通配命中旧皮肤才算债务；只有 COUI/App 门面通配时，
            // 未知名的定义就在那些包里，必须判成非债务，否则 COUI 通配反而被计成欠账。
            val fromWildcard = wildcardPackages.map { wildcard -> wildcard + name }
            return fromWildcard.firstOrNull { it.isDirectNativeSkin() }
                ?: fromWildcard.firstOrNull()
        }

        fun isDirectNativeCall(name: String): Boolean =
            resolve(name)?.isDirectNativeSkin() ?: true

        fun hasDirectNativeCall(regex: Regex, line: String): Boolean =
            regex.findAll(line).any { match -> isDirectNativeCall(match.groupValues[1]) }

        companion object {
            private val IMPORT_LINE =
                Regex("""^import\s+([A-Za-z0-9_.*]+)(?:\s+as\s+([A-Za-z0-9_]+))?\s*$""")

            fun parse(file: File): ImportTable {
                val byLocalName = HashMap<String, String>()
                val byLastSegment = HashMap<String, MutableSet<String>>()
                val wildcardPackages = mutableListOf<String>()

                file.useLines { lines ->
                    lines.forEach { rawLine ->
                        val line = rawLine.trim()
                        if (!line.startsWith("import ")) return@forEach
                        val match = IMPORT_LINE.matchEntire(line) ?: return@forEach
                        val qualifiedName = match.groupValues[1]
                        val alias = match.groupValues[2]
                        if (qualifiedName.endsWith(".*")) {
                            wildcardPackages += qualifiedName.removeSuffix("*")
                            return@forEach
                        }
                        val simpleName = qualifiedName.substringAfterLast('.')
                        byLocalName[alias.ifEmpty { simpleName }] = qualifiedName
                        byLastSegment.getOrPut(simpleName) { mutableSetOf() } += qualifiedName
                    }
                }

                return ImportTable(byLocalName, byLastSegment, wildcardPackages)
            }
        }
    }

    private companion object {
        const val MATERIAL3_IMPORT = "import androidx.compose.material3."
        const val MIUIX_VENDOR_IMPORT = "import top.yukonga.miuix."
        const val MIUIX_NAVIGATION_IMPORT = "import top.yukonga.miuix.kmp.nav."

        val MIUIX_COMPONENT_IMPORTS = listOf(
            "import top.yukonga.miuix.kmp.overlay.",
        )
        val APP_UI_STYLE_BRANCH = Regex("\\bAppUiStyle\\.")
        val MATERIAL_SLIDER_DEFAULTS_CALL = Regex("(?<![A-Za-z0-9_])(SliderDefaults)\\.colors\\(")
        val DIRECT_NATIVE_SLIDER_CALL = Regex("(?<![A-Za-z0-9_])(Slider)\\(")
        val DIRECT_NATIVE_BUTTON_CALL =
            Regex("(?<![A-Za-z0-9_])(Button|TextButton|OutlinedButton)\\(")
        val FIXED_ICON_BUTTON_DIMENSION = Regex(
            "\\.(?:size|width|height|sizeIn|widthIn|heightIn)\\s*\\([^)]*" +
                "(?:48\\.dp|(?:AppChromeSizeTokens\\.)?MinimumTouchTarget)",
            RegexOption.DOT_MATCHES_ALL,
        )
        val DIRECT_NATIVE_ICON_BUTTON_CALL = Regex(
            "(?<![A-Za-z0-9_])(IconButton|FilledIconButton|FilledTonalIconButton|" +
                "OutlinedIconButton)\\s*\\(",
        )
        val MATERIAL_ICON_BUTTON_DEFAULTS_REFERENCE =
            Regex("(?<![A-Za-z0-9_])(IconButtonDefaults)\\.")
        val DIRECT_ICON_BUTTON_VENDOR_IMPORT = Regex(
            "^import (?:androidx\\.compose\\.material3\\.(?:IconButton|FilledIconButton|" +
                "FilledTonalIconButton|OutlinedIconButton|IconButtonDefaults)|" +
                "top\\.yukonga\\.miuix\\.kmp\\.basic\\.IconButton)(?:\\s+as\\s+\\w+)?$",
        )
        val DIRECT_PROGRESS_INDICATOR_CALL =
            Regex("(?<![A-Za-z0-9_])(CircularProgressIndicator|LinearProgressIndicator)\\(")
        val DIRECT_NATIVE_CARD_CALL =
            Regex("(?<![A-Za-z0-9_])(Card|ElevatedCard|OutlinedCard)\\(")
        val FUNCTION_DECLARATION = Regex("\\bfun\\s+([A-Za-z0-9_]+)\\s*\\(")

        // 首页配色预览重做后，原 Md3ThemeColorPreview 的 ElevatedCard 例外已移除：
        // app 源码不应再直接调用原生 Card / ElevatedCard / OutlinedCard，一律走 App* 外观。
        val MD3_CARD_PREVIEW_EXCEPTIONS = emptyList<String>()

        // Frozen from production sources on 2026-11-03; lower after each migration batch.
        // 别名已解析，COUI import 不计入债务。
        const val MAX_FEATURE_MATERIAL3_FILES = 233
        const val MAX_FEATURE_MATERIAL3_IMPORTS = 353
        const val MAX_FEATURE_MATERIAL3_WILDCARD_IMPORTS = 83
        const val MAX_FEATURE_MIUIX_COMPONENT_FILES = 0
        const val MAX_FEATURE_MIUIX_COMPONENT_IMPORTS = 0
        const val MAX_FEATURE_THEME_BRANCH_FILES = 83
        const val MAX_FEATURE_THEME_BRANCH_LINES = 291
        const val MAX_FEATURE_DIRECT_PROGRESS_CALLS = 2
        const val MAX_PRIMITIVE_FACADE_MATERIAL3_IMPORTS = 45

        // 下列上限为 0 之外的存量：剩余命中都是 Material3 皮肤分支里的真实直连调用
        // （双皮肤共存期必须保留），不是 import 别名假阳性。后续批次迁移一处就调低一处。
        const val MAX_APP_DIRECT_BUTTON_CALLS = 6
        const val MAX_APP_DIRECT_ICON_BUTTON_CALLS = 11
        const val MAX_APP_ICON_BUTTON_FIXED_DIMENSION_CALLS = 39

        // L2 直读预算，逐类冻结。
        const val MAX_APP_MATERIAL_THEME_TYPOGRAPHY_READS = 13
        const val MAX_APP_MATERIAL_THEME_COLOR_SCHEME_READS = 180
        const val MAX_APP_HARDCODED_COLOR_LITERALS = 203
        const val MAX_APP_ROUNDED_CORNER_SHAPE_LITERALS = 183

        const val MIUIX_NAVIGATION_EXCEPTION_FILE_COUNT = 23
        const val MIUIX_NAVIGATION_EXCEPTION_PATHS_SHA256 =
            "2203844046259c29aa6da89d72684d3f394ca0aeeddbe5ca64aac9f3e3d704ef"
    }
}

/** 旧皮肤（债务）包前缀：只有这两个命名空间算直连原生组件。 */
private val DEBT_IMPORT_PREFIXES = listOf(
    "androidx.compose.material3.",
    "top.yukonga.miuix.",
)

private fun String.isDirectNativeSkin(): Boolean =
    DEBT_IMPORT_PREFIXES.any(::startsWith)

