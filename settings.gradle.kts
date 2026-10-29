pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

// ASM 补丁插件（ComposeDetachedOwnerGuard）以组合构建提供，独立 classpath，不进入 buildSrc。
includeBuild("build-logic")

// Optional local checkout for deterministic development when GitHub Packages credentials are not
// available. Production/default resolution still uses the exact declared package coordinates.
providers.gradleProperty("bili.miuix.source").orNull?.let { sourcePath ->
    includeBuild(sourcePath) {
        dependencySubstitution {
            substitute(module("top.yukonga.miuix.kmp:miuix-core-android"))
                .using(project(":miuix-core"))
            substitute(module("top.yukonga.miuix.kmp:miuix-ui-android"))
                .using(project(":miuix-ui"))
            substitute(module("top.yukonga.miuix.kmp:miuix-shader-android"))
                .using(project(":miuix-shader"))
            substitute(module("top.yukonga.miuix.kmp:miuix-blur-android"))
                .using(project(":miuix-blur"))
            substitute(module("top.yukonga.miuix.kmp:miuix-preference-android"))
                .using(project(":miuix-preference"))
            substitute(module("top.yukonga.miuix.kmp:miuix-squircle-android"))
                .using(project(":miuix-squircle"))
            substitute(module("top.yukonga.miuix.kmp:miuix-icons-android"))
                .using(project(":miuix-icons"))
            substitute(module("top.yukonga.miuix.kmp:miuix-nav-android"))
                .using(project(":miuix-nav"))
        }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // 本地构件仓：没有 GitHub Packages 凭据时 miuix 与 COUI 的兜底来源，且必须先于远端快照仓库解析。
        // 稳定副本优先，其次各自 checkout 的构建产物目录——构建目录会被 gradle clean 清掉，副本不会。
        // 目录不存在时 Gradle 视作空仓库，不影响有凭据的常规路径。
        listOf(
            "miuix-local-repo",
            "miuix/build/repository/local",
            "coui-local-repo",
            "coui/build/repository/local",
        ).forEach { path -> maven { url = uri(rootDir.parentFile.resolve(path)) } }
        // 仍需指向其它 COUI（ColorOS 风格 Compose 组件库）构件目录时用 -Pbili.coui.repository=<path>。
        providers.gradleProperty("bili.coui.repository").orNull?.let { path ->
            maven { url = uri(rootDir.resolve(path)) }
        }
        maven { url = uri("https://jitpack.io") }
        // GitHub Packages (compose-miuix-ui/miuix).
        // Authentication is required even for this public package. Keep credentials outside
        // the repository in ~/.gradle/gradle.properties or environment variables.
        val gprUser = providers.gradleProperty("gpr.user")
            .orElse(providers.environmentVariable("GITHUB_ACTOR"))
        val gprKey = providers.gradleProperty("gpr.key")
            .orElse(providers.environmentVariable("GITHUB_TOKEN"))
        maven {
            name = "GitHubPackagesMiuix"
            url = uri("https://maven.pkg.github.com/compose-miuix-ui/miuix")
            if (gprUser.isPresent && gprKey.isPresent) {
                credentials {
                    username = gprUser.get()
                    password = gprKey.get()
                }
            }
        }
        // GitHub Packages (Costben/coui17)：COUI 17 的 1.2.0-SNAPSHOT 滚动快照只发布在这里，不在 Maven Central。
        // 与 miuix 相同，公开包也必须带 read:packages 凭据；只放行 COUI 的 group，避免其它依赖逐个去这里撞 401。
        maven {
            name = "GitHubPackagesCoui"
            url = uri("https://maven.pkg.github.com/Costben/coui17")
            if (gprUser.isPresent && gprKey.isPresent) {
                credentials {
                    username = gprUser.get()
                    password = gprKey.get()
                }
            }
            content { includeGroup("io.github.suqi8.coui.kmp") }
        }
        // Cling Repo
        maven { 
            url = uri("http://4thline.org/m2") 
            isAllowInsecureProtocol = true
        }
    }
}

rootProject.name = "BiliPai"
include(":app")
include(":app-tv")
include(":core-data")
include(":core-player")
include(":baselineprofile")
include(":settings-core")
include(":network-core")
include(":plugin-sdk")
include(":design-system")
include(":design-tokens")
include(":brand-motion")
include(":dolby-ffmpeg-decoder")
include(":danmaku-engine")
include(":miuix-navigation")
include(":ci-lint")

 
