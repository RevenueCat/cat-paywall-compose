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
package com.revenuecat.articles.paywall.compose.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import androidx.navigation3.ui.NavDisplay
import com.revenuecat.articles.paywall.core.navigation.CatArticlesNavigatorImpl
import com.revenuecat.articles.paywall.core.navigation.CatArticlesScreen
import com.revenuecat.articles.paywall.core.navigation.LocalComposeNavigator
import com.revenuecat.articles.paywall.feature.account.AccountScreen
import com.revenuecat.articles.paywall.feature.article.CatArticlesDetail
import com.revenuecat.articles.paywall.feature.home.CatArticlesHome
import com.revenuecat.articles.paywall.feature.subscriptions.SubscriptionManagementScreen
import com.revenuecat.articles.paywall.paywalls.CatCustomPaywalls

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun CatArticlesNavHost() {
  val backStack = rememberNavBackStack(CatArticlesScreen.CatHome)
  val navigator = remember(backStack) { CatArticlesNavigatorImpl(backStack) }

  CompositionLocalProvider(
    LocalComposeNavigator provides navigator,
  ) {
    SharedTransitionLayout {
      NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator()),
        entryProvider = entryProvider<NavKey> {
          entry<CatArticlesScreen.CatHome> {
            CatArticlesHome(
              sharedTransitionScope = this@SharedTransitionLayout,
              animatedContentScope = LocalNavAnimatedContentScope.current,
            )
          }

          entry<CatArticlesScreen.CatArticle> { screen ->
            CatArticlesDetail(
              sharedTransitionScope = this@SharedTransitionLayout,
              animatedContentScope = LocalNavAnimatedContentScope.current,
              article = screen.article,
            )
          }

          entry<CatArticlesScreen.Paywalls> {
            CatCustomPaywalls()
          }

          entry<CatArticlesScreen.Account> {
            AccountScreen()
          }

          entry<CatArticlesScreen.SubscriptionManagement> {
            SubscriptionManagementScreen()
          }
        },
      )
    }
  }
}
