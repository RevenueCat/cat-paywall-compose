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
  id("revenuecat.android.hilt")
  id("revenuecat.spotless")
  id("org.jetbrains.kotlin.plugin.serialization")
}

android {
  namespace = "com.revenuecat.articles.paywall.compose.core.network"
}

dependencies {
  implementation(projects.core.model)

  api(libs.retrofit)
  api(libs.sandwich)
  api(libs.okhttp.logging)

  api(libs.retrofit.kotlinx.serialization)
  api(libs.kotlinx.serialization.json)
}
