plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.aboutlibraries) apply false
    alias(libs.plugins.spotless) apply false
}

subprojects {
    apply(plugin = rootProject.libs.plugins.spotless.get().pluginId)

    extensions.configure<com.diffplug.gradle.spotless.SpotlessExtension> {
        kotlin {
            target("**/*.kt")
            targetExclude("**/build/**")
            ktlint(libs.versions.ktlint.get())
        }
        kotlinGradle {
            target("**/*.kts")
            targetExclude("**/build/**")
            ktlint(libs.versions.ktlint.get())
        }
    }
}