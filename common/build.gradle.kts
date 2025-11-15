plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.parcelize)
}

val appApplicationId: String by rootProject.extra
val appVersionName: String by rootProject.extra
val appVersionCode: Int by rootProject.extra

android {
    namespace = "one.yufz.hmspush.common"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        buildConfigField("String", "APPLICATION_ID", "\"$appApplicationId\"")
        buildConfigField("String", "VERSION_NAME", "\"${appVersionName}\"")
        buildConfigField("int", "VERSION_CODE", appVersionCode.toString())
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    buildFeatures {
        aidl = true
    }
    kotlin {
        jvmToolchain(21)
    }
}

dependencies {
    api(libs.kotlinx.coroutines.android)
}
