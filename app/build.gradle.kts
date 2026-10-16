import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
    alias(libs.plugins.aboutlibraries)
}

val localProps = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

val hubHost: String = localProps.getProperty("HUB_HOST") ?: "10.0.2.2:8080"

val tossClientKey: String = localProps.getProperty("TOSS_CLIENT_KEY").orEmpty()
val kakaoNativeAppKey: String = localProps.getProperty("KAKAO_NATIVE_APP_KEY").orEmpty()
val naverClientId: String = localProps.getProperty("NAVER_CLIENT_ID").orEmpty()
val naverClientSecret: String = localProps.getProperty("NAVER_CLIENT_SECRET").orEmpty()

// 릴리즈 서명 키
val releaseStoreFile: String? = localProps.getProperty("RELEASE_STORE_FILE")

android {
    namespace = "com.ninezero.halo"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.ninezero.halo"
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "TOSS_CLIENT_KEY", "\"$tossClientKey\"")

        // 웹 클라이언트 ID — 구글 로그인 serverClientId
        buildConfigField(
            "String",
            "GOOGLE_SERVER_CLIENT_ID",
            "\"867890220312-dsqsq2h853j45vr169iknascnte7qj0m.apps.googleusercontent.com\""
        )

        buildConfigField("String", "KAKAO_NATIVE_APP_KEY", "\"$kakaoNativeAppKey\"")

        manifestPlaceholders["kakaoNativeAppKey"] = kakaoNativeAppKey

        buildConfigField("String", "NAVER_CLIENT_ID", "\"$naverClientId\"")
        buildConfigField("String", "NAVER_CLIENT_SECRET", "\"$naverClientSecret\"")
    }

    signingConfigs {
        if (releaseStoreFile != null) {
            create("release") {
                storeFile = rootProject.file(releaseStoreFile)
                storePassword = localProps.getProperty("RELEASE_STORE_PASSWORD")
                keyAlias = localProps.getProperty("RELEASE_KEY_ALIAS")
                keyPassword = localProps.getProperty("RELEASE_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        debug {
            buildConfigField("String", "HUB_BASE_URL", "\"http://$hubHost/\"")
            buildConfigField("String", "HUB_WS_BASE_URL", "\"ws://$hubHost/\"")
            manifestPlaceholders["usesCleartextTraffic"] = "true"
        }
        release {
            buildConfigField("String", "HUB_BASE_URL", "\"https://api.haloapp.cc/\"")
            buildConfigField("String", "HUB_WS_BASE_URL", "\"wss://api.haloapp.cc/\"")
            manifestPlaceholders["usesCleartextTraffic"] = "false"
            signingConfig = signingConfigs.findByName("release")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

composeCompiler {
    stabilityConfigurationFiles.add(project.layout.projectDirectory.file("compose_stability_config.conf"))
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        freeCompilerArgs.addAll(
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=androidx.compose.animation.ExperimentalSharedTransitionApi",
            "-opt-in=androidx.compose.foundation.layout.ExperimentalLayoutApi"
        )
    }
}

dependencies {
    // AndroidX Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.lifecycle.viewmodel.compose)

    // Orbit MVI
    implementation(libs.orbit.core)
    implementation(libs.orbit.viewmodel)
    implementation(libs.orbit.compose)

    // Retrofit & OkHttp
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    // Kotlin Serialization
    implementation(libs.kotlinx.serialization.json)

    // Room
    implementation(libs.room.runtime)
    ksp(libs.room.compiler)

    // WorkManager
    implementation(libs.work.runtime.ktx)
    implementation(libs.hilt.work)
    ksp(libs.androidx.hilt.compiler)

    // DataStore
    implementation(libs.datastore.preferences)

    // Navigation
    implementation(libs.navigation.compose)

    // Coil 3
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    implementation(libs.coil.video)

    // Media3 (ExoPlayer)
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.ui)
    implementation(libs.media3.common)

    // Palette
    implementation(libs.palette)

    // Haze
    implementation(libs.haze)
    implementation(libs.haze.blur)

    // Kyant AndroidLiquidGlass
    implementation(libs.kyant.backdrop)

    // Collapsing Toolbar
    implementation(libs.collapsing.toolbar)

    // Reorderable (drag & drop)
    implementation(libs.reorderable)

    // AboutLibraries (오픈소스 라이선스 데이터 파싱)
    implementation(libs.aboutlibraries.core)

    // Timber
    implementation(libs.timber)

    // Browser (Chrome Custom Tabs)
    implementation(libs.browser)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)

    // Google Sign-In (Credential Manager)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services)
    implementation(libs.googleid)

    // Kakao Login
    implementation(libs.kakao.user)

    // Naver Login
    implementation(libs.naver.oauth)

    // Toss Payments (결제 SDK)
    implementation(libs.toss.payments)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)

    // LeakCanary (debug only)
    debugImplementation(libs.leakcanary)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    // Debug
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}