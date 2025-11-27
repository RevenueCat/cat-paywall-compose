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
import androidx.compose.ui.res.painterResource
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
import com.skydoves.landscapist.glide.GlideImage
import com.skydoves.landscapist.placeholder.shimmer.Shimmer
import com.skydoves.landscapist.placeholder.shimmer.ShimmerPlugin

@Composable
fun CatArticlesHome(
  sharedTransitionScope: SharedTransitionScope,
  animatedContentScope: AnimatedContentScope,
  catArticlesViewModel: CatArticlesViewModel = hiltViewModel(),
) {
  val uiState by catArticlesViewModel.uiState.collectAsStateWithLifecycle()
  val composeNavigator = currentComposeNavigator

  Column(modifier = Modifier.fillMaxSize()) {
    CatArticlesAppBar(
      modifier = Modifier.background(CatArticlesTheme.colors.primary),
      actions = {
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
      sharedTransitionScope = sharedTransitionScope,
      animatedContentScope = animatedContentScope,
      onNavigateToDetails = { composeNavigator.navigate(CatArticlesScreen.CatArticle(it)) },
    )
  }
}

@Composable
private fun HomeContent(
  uiState: HomeUiState,
  sharedTransitionScope: SharedTransitionScope,
  animatedContentScope: AnimatedContentScope,
  onNavigateToDetails: (Article) -> Unit,
) {
  Box(modifier = Modifier.fillMaxSize()) {
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
            sharedTransitionScope = sharedTransitionScope,
            animatedContentScope = animatedContentScope,
            onNavigateToDetails = onNavigateToDetails,
          )
        }
      }
    }
  }
}

@Composable
private fun ArticleCard(
  article: Article,
  sharedTransitionScope: SharedTransitionScope,
  animatedContentScope: AnimatedContentScope,
  onNavigateToDetails: (Article) -> Unit,
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
          isLocalInspectionMode = LocalInspectionMode.current,
          state = rememberSharedContentState(key = "article-${article.title}"),
          animatedVisibilityScope = animatedContentScope,
          boundsTransform = boundsTransform,
        ),
    ) {
      GlideImage(
        modifier = Modifier.fillMaxSize(),
        imageModel = { article.cover },
        imageOptions = ImageOptions(contentScale = ContentScale.Crop),
        component = rememberImageComponent {
          +ShimmerPlugin(
            Shimmer.Resonate(
              baseColor = Color.Transparent,
              highlightColor = Color.LightGray,
            ),
          )
        },
        previewPlaceholder = painterResource(
          id = R.drawable.placeholder,
        ),
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
          GlideImage(
            modifier = Modifier.fillMaxSize(),
            imageModel = { article.cover },
            imageOptions = ImageOptions(contentScale = ContentScale.Crop),
            previewPlaceholder = painterResource(id = R.drawable.placeholder),
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
