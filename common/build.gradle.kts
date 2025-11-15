plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.parcelize)
}

val applicationIdValue = rootProject.extra["applicationId"] as String
val gitVersionName = (rootProject.extra["gitVersionName"] as String).ifBlank { null }
val gitVersionCode = rootProject.extra["gitVersionCode"] as Int

android {
    namespace = "one.yufz.hmspush.common"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        buildConfigField("String", "APPLICATION_ID", "\"$applicationIdValue\"")
        buildConfigField("String", "VERSION_NAME", "\"${gitVersionName ?: "null"}\"")
        buildConfigField("int", "VERSION_CODE", gitVersionCode.toString())
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    api(libs.kotlinx.coroutines.android)
}
