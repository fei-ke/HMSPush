import org.gradle.api.Project
import java.io.ByteArrayOutputStream

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.parcelize) apply false
}

fun Project.runGitCommand(vararg args: String): String? =
    try {
        val stdout = ByteArrayOutputStream()
        exec {
            commandLine("git", *args)
            standardOutput = stdout
        }
        stdout.toString().trim().ifBlank { null }
    } catch (_: Exception) {
        null
    }

fun Project.resolveVersionCode(): Int =
    runGitCommand("rev-list", "--first-parent", "--count", "HEAD")?.toIntOrNull() ?: -1

fun Project.resolveVersionName(): String? =
    runGitCommand("describe", "--tags", "--dirty")?.let {
        if (it.startsWith("v")) it.substring(1) else it
    }

val gitVersionCodeValue = resolveVersionCode()
val gitVersionNameValue = resolveVersionName()

extra.apply {
    set("gitVersionCode", gitVersionCodeValue)
    set("gitVersionName", gitVersionNameValue ?: "")
    set("applicationId", "one.yufz.hmspush")
}

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}
