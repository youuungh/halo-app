import java.util.Properties

plugins {
    alias(libs.plugins.android.test)
    alias(libs.plugins.baselineprofile)
}

val localProps = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

android {
    namespace = "com.ninezero.halo.baselineprofile"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        minSdk = 30
        targetSdk = 36
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        testInstrumentationRunnerArguments["loginEmail"] = localProps.getProperty("BASELINE_LOGIN_EMAIL").orEmpty()
        testInstrumentationRunnerArguments["loginPassword"] = localProps.getProperty("BASELINE_LOGIN_PASSWORD").orEmpty()
    }

    targetProjectPath = ":app"
}

baselineProfile {
    useConnectedDevices = true
}

dependencies {
    implementation(libs.androidx.test.ext.junit)
    implementation(libs.androidx.uiautomator)
    implementation(libs.androidx.benchmark.macro.junit4)
}
