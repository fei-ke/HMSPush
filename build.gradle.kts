import com.android.build.api.dsl.ApplicationDefaultConfig
import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.CommonExtension
import com.android.build.gradle.api.AndroidBasePlugin
import org.lsposed.lsplugin.ApksignExtension
import org.lsposed.lsplugin.ApksignPlugin

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.parcelize) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.lsplugin.jgit)
    alias(libs.plugins.lsplugin.apksign) apply false
}

val repo = jgit.repo()
val commitCount = (repo?.commitCount("refs/remotes/origin/master") ?: 1)
val latestTag = repo?.latestTag?.removePrefix("v") ?: "0.0"

val appNamespace by extra("one.yufz.hmspush")
val appApplicationId by extra("one.yufz.hmspush")

val appVersionCode by extra(commitCount)
val appVersionName by extra(latestTag)

val androidTargetSdkVersion by extra(35)
val androidMinSdkVersion by extra(27)
val androidBuildToolsVersion by extra("35.0.0")
val androidCompileSdkVersion by extra(36)
val androidSourceCompatibility by extra(JavaVersion.VERSION_21)
val androidTargetCompatibility by extra(JavaVersion.VERSION_21)

tasks.register("Delete", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}

subprojects {
    plugins.withType<AndroidBasePlugin> {
        extensions.configure(CommonExtension::class.java) {
            compileSdk = androidCompileSdkVersion
            buildToolsVersion = androidBuildToolsVersion

            defaultConfig {
                minSdk = androidMinSdkVersion
            }
            lint {
                abortOnError = true
                checkReleaseBuilds = false
            }

            compileOptions {
                sourceCompatibility = androidSourceCompatibility
                targetCompatibility = androidTargetCompatibility
            }
        }
        extensions.configure(BasePluginExtension::class.java) {
            archivesName.set("${rootProject.name}-v$appVersionName-$appVersionCode")
        }
    }

    plugins.withId("com.android.application") {
        extensions.configure(ApplicationExtension::class.java) {
            defaultConfig {
                targetSdk = androidTargetSdkVersion
                versionCode = appVersionCode
                versionName = appVersionName

                namespace = appNamespace
                applicationId = appApplicationId
            }
        }
    }

    plugins.withType(JavaPlugin::class.java) {
        extensions.configure(JavaPluginExtension::class.java) {
            sourceCompatibility = androidSourceCompatibility
            targetCompatibility = androidTargetCompatibility
        }
    }

    plugins.withType(ApksignPlugin::class.java) {
        extensions.configure(ApksignExtension::class.java) {
            storeFileProperty = "StoreFile"
            storePasswordProperty = "StorePassword"
            keyAliasProperty = "KeyAlias"
            keyPasswordProperty = "KeyPassword"
        }
    }
}