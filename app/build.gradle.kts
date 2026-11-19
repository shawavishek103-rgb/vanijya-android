plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
}

android {
  namespace = "com.example"
  compileSdk { version = release(36) { minorApiLevel = 1 } }

  defaultConfig {
    applicationId = "com.aistudio.vanijya.kxpqzm"
    minSdk = 26
    targetSdk = 36
    versionCode = 1
    versionName = "1.0.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  // Do not remove or modify these signingConfigs. They are necessary for building, installing,
  // and updating Android apps in AI Studio.
  signingConfigs {
    create("release") {
      val keystorePath = System.getenv("KEYSTORE_PATH") ?: "${rootDir}/my-upload-key.jks"
      storeFile = file(keystorePath)
      storePassword = System.getenv("STORE_PASSWORD")
      keyAlias = "upload"
      keyPassword = System.getenv("KEY_PASSWORD")
    }
    create("debugConfig") {
      storeFile = file("${rootDir}/debug.keystore")
      storePassword = "android"
      keyAlias = "androiddebugkey"
      keyPassword = "android"
    }
  }

  buildTypes {
    release {
      isCrunchPngs = false
      isMinifyEnabled = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      // Do not remove or modify this signingConfig assignment. It is necessary for Android apps in
      // AI Studio.
      signingConfig = signingConfigs.getByName("release")
    }
    debug {
      // Do not remove or modify this signingConfig assignment. It is necessary for Android apps in
      // AI Studio.
      signingConfig = signingConfigs.getByName("debugConfig")
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
  testOptions { unitTests { isIncludeAndroidResources = true } }
  dependenciesInfo {
    includeInApk = false
    includeInBundle = true
  }
}

ksp {
  arg("room.schemaLocation", "$projectDir/schemas")
  arg("room.incremental", "true")
  arg("room.generateKotlin", "true")
  arg("dagger.hilt.android.internal.disableAndroidSuperclassValidation", "true")
}

// CI / Lint Verification: Fail build if android.permission.INTERNET appears in any merged manifest
abstract class VerifyNoInternetPermissionTask : org.gradle.api.DefaultTask() {
  @get:org.gradle.api.tasks.Internal
  abstract val mergedManifestDir: org.gradle.api.file.DirectoryProperty

  @get:org.gradle.api.tasks.InputFile
  abstract val sourceManifest: org.gradle.api.file.RegularFileProperty

  @org.gradle.api.tasks.TaskAction
  fun verify() {
    val srcFile = sourceManifest.get().asFile
    if (srcFile.exists() && srcFile.readText().contains("android.permission.INTERNET")) {
      throw GradleException("OFFLINE VIOLATION: android.permission.INTERNET declared in src/main/AndroidManifest.xml")
    }
    val intermediatesDir = mergedManifestDir.get().asFile
    if (intermediatesDir.exists()) {
      intermediatesDir.walkTopDown()
        .filter { it.isFile && it.name == "AndroidManifest.xml" }
        .forEach { manifestFile ->
          if (manifestFile.readText().contains("android.permission.INTERNET")) {
            throw GradleException(
              "OFFLINE VIOLATION: android.permission.INTERNET found in merged manifest: ${manifestFile.absolutePath}"
            )
          }
        }
    }
  }
}

val verifyNoInternetPermission = tasks.register<VerifyNoInternetPermissionTask>("verifyNoInternetPermission") {
  group = "verification"
  description = "Fails if android.permission.INTERNET is present in any merged AndroidManifest.xml"
  mergedManifestDir.set(layout.buildDirectory.dir("intermediates/merged_manifest"))
  sourceManifest.set(layout.projectDirectory.file("src/main/AndroidManifest.xml"))
}

tasks.matching { it.name.startsWith("process") && it.name.endsWith("MainManifest") }.configureEach {
  finalizedBy(verifyNoInternetPermission)
}
tasks.matching { it.name == "check" }.configureEach {
  dependsOn(verifyNoInternetPermission)
}

// Strictly offline dependencies only (no network, Firebase, or cloud libraries)
dependencies {
  implementation(platform(libs.androidx.compose.bom))
  // implementation(platform(libs.firebase.bom))
  // implementation(libs.accompanist.permissions)
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.appcompat)
  implementation(libs.androidx.biometric)
  implementation(libs.androidx.core.splashscreen)
  implementation(libs.androidx.camera.camera2)
  implementation(libs.androidx.camera.core)
  implementation(libs.androidx.camera.lifecycle)
  implementation(libs.androidx.camera.view)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.material3.window.size)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.datastore.preferences)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.hilt.navigation.compose)
  implementation(libs.hilt.android)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  implementation(libs.sqlcipher.android)
  implementation(libs.androidx.sqlite.ktx)
  // implementation(libs.coil.compose)
  // implementation(libs.converter.moshi)
  // implementation(libs.firebase.ai)
  // implementation(libs.firebase.firestore)
  // implementation(libs.firebase.auth)
  // implementation(libs.androidx.credentials)
  // implementation(libs.androidx.credentials.play.services)
  // implementation(libs.googleid)
  // implementation(libs.firebase.appcheck.recaptcha)
  // implementation(libs.firebase.appcheck.debug)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  // implementation(libs.logging.interceptor)
  // implementation(libs.moshi.kotlin)
  // implementation(libs.okhttp)
  // implementation(libs.play.services.location)
  // implementation(libs.retrofit)
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
  "ksp"(libs.androidx.room.compiler)
  "ksp"(libs.hilt.compiler)
  // "ksp"(libs.moshi.kotlin.codegen)
}
