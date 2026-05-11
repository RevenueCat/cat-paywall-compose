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
package com.revenuecat.articles.paywall.feature.home

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.revenuecat.articles.paywall.compose.core.designsystem.R
import com.revenuecat.articles.paywall.core.designsystem.component.CatArticlesAppBar
import com.revenuecat.articles.paywall.core.designsystem.component.CatArticlesCircularProgress
import com.revenuecat.articles.paywall.core.designsystem.component.catArticlesSharedElement
import com.revenuecat.articles.paywall.core.designsystem.theme.CatArticlesTheme
import com.revenuecat.articles.paywall.core.model.Article
import com.revenuecat.articles.paywall.core.model.MockUtils.mockArticle
import com.revenuecat.articles.paywall.core.navigation.CatArticlesScreen
import com.revenuecat.articles.paywall.core.navigation.boundsTransform
import com.revenuecat.articles.paywall.core.navigation.currentComposeNavigator
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.components.rememberImageComponent
import com.skydoves.landscapist.image.LandscapistImage
import com.skydoves.landscapist.placeholder.shimmer.Shimmer
import com.skydoves.landscapist.placeholder.shimmer.ShimmerPlugin

@Composable
fun CatArticlesHome(
  sharedTransitionScope: SharedTransitionScope,
  animatedContentScope: AnimatedContentScope,
  catArticlesViewModel: CatArticlesViewModel = hiltViewModel(),
) {
  val uiState by catArticlesViewModel.uiState.collectAsStateWithLifecycle()
  val bookmarkedTitles by catArticlesViewModel.bookmarkedTitles.collectAsStateWithLifecycle()
  val customerInfo by catArticlesViewModel.customerInfo.collectAsStateWithLifecycle()
  val todayReadCount by catArticlesViewModel.todayReadCount.collectAsStateWithLifecycle()
  val composeNavigator = currentComposeNavigator
  val entitlementIdentifier = stringResource(R.string.entitlement_premium)
  val isEntitled = customerInfo?.entitlements?.get(entitlementIdentifier)?.isActive == true

  Column(modifier = Modifier.fillMaxSize()) {
    CatArticlesAppBar(
      modifier = Modifier.background(CatArticlesTheme.colors.primary),
      actions = {
        IconButton(onClick = { composeNavigator.navigate(CatArticlesScreen.Bookmarks) }) {
          Icon(
            imageVector = Icons.Default.Bookmark,
            contentDescription = "Bookmarks",
            tint = Color.White,
          )
        }
        IconButton(onClick = { composeNavigator.navigate(CatArticlesScreen.Account) }) {
          Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "Account",
            tint = Color.White,
          )
        }
      },
    )

    HomeContent(
      uiState = uiState,
      bookmarkedTitles = bookmarkedTitles,
      isEntitled = isEntitled,
      todayReadCount = todayReadCount,
      sharedTransitionScope = sharedTransitionScope,
      animatedContentScope = animatedContentScope,
      onNavigateToDetails = { article ->
        if (!isEntitled && todayReadCount >= 3) {
          composeNavigator.navigate(CatArticlesScreen.Paywalls)
        } else {
          composeNavigator.navigate(CatArticlesScreen.CatArticle(article))
        }
      },
      onToggleBookmark = { title ->
        if (isEntitled) {
          catArticlesViewModel.toggleBookmark(title)
        } else {
          composeNavigator.navigate(CatArticlesScreen.Paywalls)
        }
      },
    )
  }
}

@Composable
private fun HomeContent(
  uiState: HomeUiState,
  bookmarkedTitles: Set<String>,
  isEntitled: Boolean,
  todayReadCount: Int,
  sharedTransitionScope: SharedTransitionScope,
  animatedContentScope: AnimatedContentScope,
  onNavigateToDetails: (Article) -> Unit,
  onToggleBookmark: (String) -> Unit,
) {
  Column(modifier = Modifier.fillMaxSize()) {
    if (!isEntitled) {
      QuotaBanner(todayReadCount = todayReadCount)
    }

    Box(modifier = Modifier.weight(1f)) {
      if (uiState == HomeUiState.Loading) {
        CatArticlesCircularProgress()
      } else if (uiState is HomeUiState.Success) {
        LazyVerticalGrid(
          modifier = Modifier.testTag("CatArticlesList"),
          columns = GridCells.Fixed(2),
          contentPadding = PaddingValues(6.dp),
        ) {
          items(items = uiState.articles, key = { it.title }) { article ->
            ArticleCard(
              article = article,
              isBookmarked = article.title in bookmarkedTitles,
              sharedTransitionScope = sharedTransitionScope,
              animatedContentScope = animatedContentScope,
              onNavigateToDetails = onNavigateToDetails,
              onToggleBookmark = { onToggleBookmark(article.title) },
            )
          }
        }
      }
    }
  }
}

@Composable
private fun QuotaBanner(todayReadCount: Int) {
  val limitReached = todayReadCount >= 3
  val bannerColor = if (limitReached) {
    Color(
      0xFFFFF3CD,
    )
  } else {
    CatArticlesTheme.colors.primary.copy(alpha = 0.1f)
  }
  val textColor = if (limitReached) Color(0xFF856404) else CatArticlesTheme.colors.black
  val message = if (limitReached) {
    "Daily limit reached · Subscribe for unlimited access"
  } else {
    "$todayReadCount / 3 free articles read today"
  }

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .background(bannerColor)
      .padding(horizontal = 16.dp, vertical = 8.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = message,
      fontSize = 13.sp,
      color = textColor,
      fontWeight = if (limitReached) FontWeight.SemiBold else FontWeight.Normal,
    )
  }
}

@Composable
private fun ArticleCard(
  article: Article,
  isBookmarked: Boolean,
  sharedTransitionScope: SharedTransitionScope,
  animatedContentScope: AnimatedContentScope,
  onNavigateToDetails: (Article) -> Unit,
  onToggleBookmark: () -> Unit,
) {
  with(sharedTransitionScope) {
    Box(
      modifier = Modifier
        .testTag("Article")
        .padding(8.dp)
        .fillMaxWidth()
        .height(300.dp)
        .clip(RoundedCornerShape(6.dp))
        .clickable { onNavigateToDetails.invoke(article) }
        .catArticlesSharedElement(
          sharedTransitionScope = this,
          isLocalInspectionMode = LocalInspectionMode.current,
          state = rememberSharedContentState(key = "article-${article.title}"),
          animatedVisibilityScope = animatedContentScope,
          boundsTransform = boundsTransform,
        ),
    ) {
      LandscapistImage(
        imageModel = { article.cover },
        modifier = Modifier.fillMaxSize(),
        imageOptions = ImageOptions(contentScale = ContentScale.Crop),
        component = rememberImageComponent {
          +ShimmerPlugin(
            Shimmer.Resonate(
              baseColor = Color.Transparent,
              highlightColor = Color.LightGray,
            ),
          )
        },
      )

      Text(
        modifier = Modifier
          .fillMaxWidth()
          .align(Alignment.BottomCenter)
          .background(Color.Black.copy(alpha = 0.65f))
          .padding(12.dp),
        text = article.title,
        color = Color.White,
        textAlign = TextAlign.Center,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
      )

      IconButton(
        modifier = Modifier.align(Alignment.TopEnd),
        onClick = onToggleBookmark,
      ) {
        Icon(
          imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
          contentDescription = if (isBookmarked) "Remove bookmark" else "Add bookmark",
          tint = Color.White,
        )
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun HomeContentPreview() {
  CatArticlesTheme {
    HomeContentPreviewContent()
  }
}

@Composable
private fun HomeContentPreviewContent() {
  // Preview without shared element transitions
  Box(modifier = Modifier.fillMaxSize()) {
    LazyVerticalGrid(
      columns = GridCells.Fixed(2),
      contentPadding = PaddingValues(6.dp),
    ) {
      items(items = List(10) { mockArticle }, key = { "${it.title}-$it" }) { article ->
        Box(
          modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .height(300.dp)
            .clip(RoundedCornerShape(6.dp)),
        ) {
          LandscapistImage(
            imageModel = { article.cover },
            modifier = Modifier.fillMaxSize(),
            imageOptions = ImageOptions(contentScale = ContentScale.Crop),
          )

          Text(
            modifier = Modifier
              .fillMaxWidth()
              .align(Alignment.BottomCenter)
              .background(Color.Black.copy(alpha = 0.65f))
              .padding(12.dp),
            text = article.title,
            color = Color.White,
            textAlign = TextAlign.Center,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
          )
        }
      }
    }
  }
}
