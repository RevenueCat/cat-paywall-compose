/*
 * Copyright (c) 2025 RevenueCat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
plugins {
  id("revenuecat.android.application")
  id("revenuecat.android.application.compose")
  id("revenuecat.android.hilt")
  id("revenuecat.spotless")
  id("kotlin-parcelize")
  alias(libs.plugins.google.secrets)
  alias(libs.plugins.android.application)
  alias(libs.plugins.baselineprofile)
}

android {
  namespace = "com.revenuecat.articles.paywall.compose"
  compileSdk = 36

  defaultConfig {
    applicationId = "com.revenuecat.articles.paywall.compose"
    minSdk = 24
    versionCode = 1
    versionName = "1.0"
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }

  kotlinOptions {
    jvmTarget = "11"
  }

  buildFeatures {
    compose = true
    buildConfig = true
  }
}

secrets {
  propertiesFileName = "secrets.properties"
  defaultPropertiesFileName = "secrets.defaults.properties"
  ignoreList.add("keyToIgnore") // Ignore the key "keyToIgnore"
  ignoreList.add("sdk.*") // Ignore all keys matching the regexp "sdk.*"
}

dependencies {
  // Core
  implementation(projects.core.designsystem)
  implementation(projects.core.navigation)

  // Features
  implementation(projects.feature.home)
  implementation(projects.feature.article)

  // RevenueCat
  implementation(libs.revenuecat)
  implementation(libs.revenuecat.ui)

  // Compose
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.compose.runtime)
  implementation(libs.androidx.compose.foundation)
  implementation(libs.androidx.compose.foundation.layout)
  implementation(libs.androidx.lifecycle.runtimeCompose)
  implementation(libs.androidx.lifecycle.viewModelCompose)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.material.iconsExtended)

  // Compose Image Loading
  implementation(libs.landscapist.glide)
  implementation(libs.landscapist.animation)
  implementation(libs.landscapist.placeholder)

  // Coroutines
  implementation(libs.kotlinx.coroutines.android)

  // Network
  implementation(libs.sandwich)
  implementation(libs.okhttp.logging)

  // Serialization
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.androidx.profileinstaller)
  baselineProfile((project(":baselineprofile")))
}