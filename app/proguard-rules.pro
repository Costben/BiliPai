# BiliPai release R8 configuration.
# Keep the configuration intentionally minimal so R8 can shrink, optimize and
# obfuscate all statically reachable app and library code.

# Runtime reflection metadata used by Retrofit/kotlinx.serialization and
# framework callbacks. These preserve metadata only; they do not keep classes.
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes InnerClasses,EnclosingMethod
-keepattributes SourceFile,LineNumberTable

# 历史校验异常记录涉及的两个文件类暂时退出水平类合并。
# 这是兼容隔离，不代表已确认它们是生成错误字节码的根因。
# 只约束两个类的优化边界；不保留全部成员，静态方法仍可裁剪、混淆和优化。
# 避免为单个卡片路径全局关闭类合并。
-keep,allowshrinking,allowobfuscation class com.android.purebilibili.feature.home.components.cards.VideoCardKt
-keep,allowshrinking,allowobfuscation class androidx.compose.foundation.text.HeightInLinesModifierKt

# 调度器的 API 34 分支与缺失 API 回退必须留在独立方法边界内。
# 仅保护访问器和命名空间入口；同文件内的任务查询、错误说明等辅助方法可继续优化。
-keep,allowshrinking,allowobfuscation class androidx.work.impl.background.systemjob.JobSchedulerExtKt {
    public static android.app.job.JobScheduler getWmJobScheduler(android.content.Context);
}
-keep,allowshrinking,allowobfuscation class androidx.work.impl.background.systemjob.JobScheduler34 {
    public android.app.job.JobScheduler forNamespace(android.app.job.JobScheduler);
}
-keepclassmembers,allowshrinking,allowobfuscation class androidx.work.impl.background.systemjob.SystemJobScheduler {
    public static void cancelAllInAllNamespaces(android.content.Context);
}

# ART 的 AOT/JIT 编译器有 16384 DEX 指令上限，超限方法会被永久降级为 Nterp
# 解释执行。Compose 的巨型 Composable 很容易越线，而 R8 会把只有单一调用点的
# 拆分函数重新内联回去，使拆分失效。这里固定这些拆分方法的边界：
# 不给 allowoptimization，R8 就不能把方法体内联回调用者。
-keepclassmembers,allowobfuscation class com.android.purebilibili.feature.home.components.cards.VideoCardKt {
    private static void VideoCardSecondaryDialogs(...);
    private static void SingleColumnElegantVideoCardBranch(...);
    private static void VideoCardDismissMenuOverlay(...);
    private static void VideoCardUnfavoriteDialog(...);
    private static void VideoCardCoverOverlayBadges(...);
    private static void VideoCardInfoMetadataColumn(...);
}
-keepclassmembers,allowobfuscation class com.android.purebilibili.feature.home.HomeScreenKt {
    private static void HomeConsentAndPreloadEffects(...);
}

# Optional classes referenced by third-party libraries on specific code paths.
# Consumer rules supplied by each dependency remain authoritative.
-dontwarn javax.annotation.**
-dontwarn org.jetbrains.annotations.**
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn dev.chrisbanes.haze.**
-dontwarn io.github.alexzhirkevich.cupertino.**
-dontwarn androidx.room.paging.**
-dontwarn androidx.media3.**
-dontwarn coil3.**
-dontwarn com.google.zxing.**
-dontwarn org.fourthline.cling.**
-dontwarn javax.enterprise.context.**
-dontwarn javax.inject.**
-dontwarn org.seamless.**

# slf4j-api 1.7.x (pulled in transitively by readability4j) looks up its logging
# binding reflectively from LoggerFactory's static initializer. This app bundles
# no binding, so SLF4J deliberately falls back to the NOP logger; tell R8 that the
# absent binding class is expected instead of keeping a dead implementation.
-dontwarn org.slf4j.impl.StaticLoggerBinder

# WebView JavaScript bridges are discovered by annotation at runtime.
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# COUI 17 按 material-color-utilities 5.0.0 编译，应用侧的 material-kolor 6.x 把它顶到 6.0.0，
# DynamicScheme 的旧签名随之消失。COUI 只在 Monet 取色模式下走 colorsFromSeed，而 BiliPai 的
# COUI ThemeController 只用 System/Light/Dark 并显式传入色板，这条路径不可达，只需压掉缺类告警。
-dontwarn com.materialkolor.scheme.DynamicScheme$Platform
-dontwarn com.materialkolor.scheme.DynamicScheme
