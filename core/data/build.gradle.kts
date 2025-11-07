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
import java.util.Properties

plugins {
  id("revenuecat.android.library")
  id("revenuecat.android.hilt")
  id("revenuecat.spotless")
}

// Load local.properties
val localProperties = Properties().apply {
  val localPropertiesFile = rootProject.file("local.properties")
  if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { load(it) }
  }
}

android {
  namespace = "com.revenuecat.articles.paywall.compose.core.data"

  buildFeatures {
    buildConfig = true
  }

  defaultConfig {
    // Add RevenueCat Test Store API key to BuildConfig for unit tests
    buildConfigField(
      "String",
      "REVENUECAT_TEST_API_KEY",
      "\"${localProperties.getProperty("revenuecat.test.api.key", "")}\""
    )

    // Set configuration for androidTest
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  testOptions {
    unitTests {
      isIncludeAndroidResources = true
    }
  }

  // Exclude hilt-navigation-compose from androidTest to avoid minSdk conflicts
  configurations {
    getByName("androidTestImplementation") {
      exclude(group = "androidx.hilt", module = "hilt-navigation-compose")
    }
  }
}

dependencies {
  api(projects.core.model)
  api(projects.core.network)
  implementation(libs.androidx.junit.ktx)

  // Testing
  testImplementation(libs.junit)
  testImplementation(libs.mockk)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.turbine)
  testImplementation(libs.revenuecat) // Required for extension functions

  // Android Instrumentation Testing (for Test Store integration tests)
  androidTestImplementation(libs.junit)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.test.runner)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.kotlinx.coroutines.test)
  androidTestImplementation(libs.turbine)
  androidTestImplementation(libs.revenuecat) // Required for RevenueCat SDK
}
