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
  id("revenuecat.spotless")
}

android {
  namespace = "com.revenuecat.articles.paywall.compose.core.designsystem"
}

dependencies {
  api(libs.androidx.startup)

  // image loading
  api(libs.landscapist.image)
  api(libs.landscapist.animation)
  api(libs.landscapist.placeholder)
  api(libs.landscapist.palette)

  // compose
  implementation(platform(libs.androidx.compose.bom))
  api(libs.androidx.compose.runtime)
  api(libs.androidx.compose.animation)
  api(libs.androidx.compose.ui)
  api(libs.androidx.compose.ui.tooling)
  api(libs.androidx.compose.ui.tooling.preview)
  api(libs.androidx.compose.material.iconsExtended)
  api(libs.androidx.compose.material3)
  api(libs.androidx.compose.foundation)
  api(libs.androidx.compose.foundation.layout)
  api(libs.androidx.compose.constraintlayout)
  api(libs.compose.effects)
}
