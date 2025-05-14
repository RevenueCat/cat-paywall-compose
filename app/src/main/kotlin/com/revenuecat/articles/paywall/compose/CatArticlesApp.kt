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
package com.revenuecat.articles.paywall.compose

import android.app.Application
import com.revenuecat.purchases.LogLevel
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesAreCompletedBy
import com.revenuecat.purchases.PurchasesConfiguration
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CatArticlesApp : Application() {

  override fun onCreate() {
    super.onCreate()

    /*
    Enable debug logs before calling `configure`.
     */
    Purchases.logLevel = LogLevel.DEBUG

    /**
     * Initialize the RevenueCat Purchases SDK.
     *
     * `appUserID` is nil, so an anonymous ID will be generated automatically by the Purchases SDK.
     * Read more about Identifying Users here: https://docs.revenuecat.com/docs/user-ids
     *
     * `purchasesAreCompletedBy` is set to REVENUECAT, so Purchases will automatically handle finishing transactions.
     * Read more about finishing transactions here: https://www.revenuecat.com/docs/migrating-to-revenuecat/sdk-or-not/finishing-transactions
     */
    val builder = PurchasesConfiguration.Builder(this, BuildConfig.REVENUECAT_API_KEY)
    Purchases.configure(
      builder
        .purchasesAreCompletedBy(PurchasesAreCompletedBy.REVENUECAT)
        .appUserID(null)
        .diagnosticsEnabled(true)
        .build(),
    )
  }
}
