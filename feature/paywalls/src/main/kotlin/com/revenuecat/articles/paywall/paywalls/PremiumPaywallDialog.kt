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
package com.revenuecat.articles.paywall.paywalls

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.res.stringResource
import com.revenuecat.articles.paywall.compose.core.designsystem.R
import com.revenuecat.articles.paywall.core.model.Article
import com.revenuecat.articles.paywall.core.navigation.CatArticlesScreen
import com.revenuecat.articles.paywall.core.navigation.currentComposeNavigator
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.models.StoreTransaction
import com.revenuecat.purchases.ui.revenuecatui.PaywallDialog
import com.revenuecat.purchases.ui.revenuecatui.PaywallDialogOptions
import com.revenuecat.purchases.ui.revenuecatui.PaywallListener

/**
 * Presents the current offering's paywall for a premium-only [article].
 *
 * Uses [PaywallDialog] because, unlike the embedded `Paywall`, it shows the exit offer configured
 * in the dashboard before the final dismissal. When a purchase or restore activates the premium
 * entitlement the dialog closes and [article] opens; otherwise the user stays where they were.
 * [onClose] is called in both cases so the caller stops presenting this dialog.
 */
@Composable
fun PremiumPaywallDialog(article: Article, onClose: () -> Unit) {
  val composeNavigator = currentComposeNavigator
  val entitlementIdentifier = stringResource(R.string.entitlement_premium)
  val currentArticle by rememberUpdatedState(article)
  val currentOnClose by rememberUpdatedState(onClose)

  // Built once so the unlocked latch and the listener stay the same for the whole presentation.
  val options = remember(entitlementIdentifier, composeNavigator) {
    var unlocked = false
    val openArticleIfUnlocked: (CustomerInfo) -> Unit = { customerInfo ->
      if (!unlocked && customerInfo.entitlements[entitlementIdentifier]?.isActive == true) {
        unlocked = true
        currentOnClose()
        composeNavigator.navigate(CatArticlesScreen.CatArticle(currentArticle))
      }
    }

    PaywallDialogOptions.Builder()
      // No setRequiredEntitlementIdentifier: if the SDK already sees the entitlement it skips the
      // dialog without calling the dismiss request, leaving the caller presenting nothing forever.
      .setListener(
        object : PaywallListener {
          override fun onPurchaseCompleted(
            customerInfo: CustomerInfo,
            storeTransaction: StoreTransaction,
          ) = openArticleIfUnlocked(customerInfo)

          // Without a required entitlement the SDK keeps the paywall open after a restore, so
          // this closes it (skipping the SDK's own close event).
          override fun onRestoreCompleted(customerInfo: CustomerInfo) =
            openArticleIfUnlocked(customerInfo)
        },
      )
      // Runs on the final dismissal (after any exit offer) and also right after a purchase.
      .setDismissRequest { if (!unlocked) currentOnClose() }
      .build()
  }

  PaywallDialog(paywallDialogOptions = options)
}
