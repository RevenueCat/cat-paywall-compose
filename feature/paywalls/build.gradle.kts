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
  id("revenuecat.android.library")
  id("revenuecat.android.library.compose")
  id("revenuecat.android.feature")
  id("revenuecat.android.hilt")
  id("revenuecat.spotless")
}

android {
  namespace = "com.revenuecat.articles.paywall.compose.feature.paywalls"
}

dependencies {
  // RevenueCat Purchases
  implementation(libs.revenuecat.ui)

  // Compose
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.tooling)
  implementation(libs.androidx.compose.foundation)
  implementation(libs.androidx.compose.material)
  implementation(libs.androidx.compose.runtime)
  implementation(libs.compose.slidetounlock)
}
