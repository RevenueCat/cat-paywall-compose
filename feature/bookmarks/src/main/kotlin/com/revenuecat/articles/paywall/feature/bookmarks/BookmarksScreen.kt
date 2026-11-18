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
package com.revenuecat.articles.paywall.feature.bookmarks

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.revenuecat.articles.paywall.compose.core.designsystem.R
import com.revenuecat.articles.paywall.core.designsystem.component.PremiumBadge
import com.revenuecat.articles.paywall.core.designsystem.component.catArticlesSharedElement
import com.revenuecat.articles.paywall.core.model.Article
import com.revenuecat.articles.paywall.core.model.ArticleAccess
import com.revenuecat.articles.paywall.core.model.resolveArticleAccess
import com.revenuecat.articles.paywall.core.navigation.CatArticlesScreen
import com.revenuecat.articles.paywall.core.navigation.boundsTransform
import com.revenuecat.articles.paywall.core.navigation.currentComposeNavigator
import com.revenuecat.articles.paywall.paywalls.PremiumPaywallDialog

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Composable
fun BookmarksScreen(
  sharedTransitionScope: SharedTransitionScope,
  animatedContentScope: AnimatedContentScope,
  viewModel: BookmarksViewModel = hiltViewModel(),
) {
  val navigator = currentComposeNavigator
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val customerInfo by viewModel.customerInfo.collectAsStateWithLifecycle()
  val entitlementIdentifier = stringResource(R.string.entitlement_premium)
  val isEntitled = customerInfo?.entitlements?.get(entitlementIdentifier)?.isActive == true
  var premiumPaywallArticle by rememberSaveable { mutableStateOf<Article?>(null) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Bookmarks") },
        navigationIcon = {
          IconButton(onClick = { navigator.navigateUp() }) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
            )
          }
        },
      )
    },
  ) { paddingValues ->
    when (val state = uiState) {
      BookmarksUiState.Loading -> Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(paddingValues),
        contentAlignment = Alignment.Center,
      ) {
        Text("Loading bookmarks...")
      }

      BookmarksUiState.Empty -> Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(paddingValues),
        contentAlignment = Alignment.Center,
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
            text = "No bookmarks yet",
            style = MaterialTheme.typography.titleMedium,
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "Bookmark your favourite articles to read them later.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 24.dp),
          )
        }
      }

      is BookmarksUiState.Success -> LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
          .fillMaxSize()
          .padding(paddingValues),
      ) {
        items(state.articles, key = { it.title }) { article ->
          BookmarkArticleCard(
            article = article,
            isEntitled = isEntitled,
            sharedTransitionScope = sharedTransitionScope,
            animatedContentScope = animatedContentScope,
            onClick = {
              // Only the premium part of the rule applies: bookmarks were never quota gated, so
              // every other result opens the article and the read count does not matter.
              val access = resolveArticleAccess(article, isEntitled, todayReadCount = 0)
              if (access == ArticleAccess.PremiumPaywall) {
                premiumPaywallArticle = article
              } else {
                navigator.navigate(CatArticlesScreen.CatArticle(article))
              }
            },
          )
        }
      }
    }
  }

  premiumPaywallArticle?.let { article ->
    PremiumPaywallDialog(
      article = article,
      onClose = {
        premiumPaywallArticle = null
        // Otherwise a purchase made in the dialog leaves this screen showing a stale entitlement.
        viewModel.refreshCustomerInfo()
      },
    )
  }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun BookmarkArticleCard(
  article: Article,
  isEntitled: Boolean,
  sharedTransitionScope: SharedTransitionScope,
  animatedContentScope: AnimatedContentScope,
  onClick: () -> Unit,
) {
  with(sharedTransitionScope) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .catArticlesSharedElement(
          sharedTransitionScope = this,
          isLocalInspectionMode = LocalInspectionMode.current,
          state = rememberSharedContentState(key = "article-${article.title}"),
          animatedVisibilityScope = animatedContentScope,
          boundsTransform = boundsTransform,
        )
        .clickable { onClick() }
        .padding(8.dp),
    ) {
      Text(
        text = article.title,
        style = MaterialTheme.typography.titleSmall,
        maxLines = 2,
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = article.author,
        style = MaterialTheme.typography.bodySmall,
      )
      if (article.premium) {
        Spacer(modifier = Modifier.height(4.dp))
        PremiumBadge(isLocked = !isEntitled)
      }
    }
  }
}
