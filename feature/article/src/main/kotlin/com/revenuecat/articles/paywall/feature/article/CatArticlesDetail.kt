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
@file:OptIn(InternalRevenueCatAPI::class)

package com.revenuecat.articles.paywall.feature.article

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kmpalette.palette.graphics.Palette
import com.revenuecat.articles.paywall.compose.core.designsystem.R
import com.revenuecat.articles.paywall.core.designsystem.component.CatArticlesAppBar
import com.revenuecat.articles.paywall.core.designsystem.component.catArticlesSharedElement
import com.revenuecat.articles.paywall.core.designsystem.component.fadingEdge
import com.revenuecat.articles.paywall.core.designsystem.theme.CatArticlesTheme
import com.revenuecat.articles.paywall.core.model.Article
import com.revenuecat.articles.paywall.core.model.MockUtils.mockArticle
import com.revenuecat.articles.paywall.core.navigation.CatArticlesScreen
import com.revenuecat.articles.paywall.core.navigation.boundsTransform
import com.revenuecat.articles.paywall.core.navigation.currentComposeNavigator
import com.revenuecat.purchases.InternalRevenueCatAPI
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.components.rememberImageComponent
import com.skydoves.landscapist.glide.GlideImage
import com.skydoves.landscapist.palette.PalettePlugin
import com.skydoves.landscapist.palette.rememberPaletteState
import com.skydoves.landscapist.placeholder.shimmer.Shimmer
import com.skydoves.landscapist.placeholder.shimmer.ShimmerPlugin

@Composable
fun CatArticlesDetail(
  sharedTransitionScope: SharedTransitionScope,
  animatedContentScope: AnimatedContentScope,
  article: Article,
  viewModel: CatArticlesDetailViewModel = hiltViewModel(),
) {
  val composeNavigator = currentComposeNavigator

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(bottom = 80.dp)
      .verticalScroll(state = rememberScrollState()),
  ) {
    CatArticlesDetailContent(
      article = article,
      viewModel = viewModel,
      sharedTransitionScope = sharedTransitionScope,
      animatedContentScope = animatedContentScope,
      navigateUp = { composeNavigator.navigateUp() },
      navigateToPaywalls = { composeNavigator.navigate(CatArticlesScreen.Paywalls) },
    )
  }
}

@Composable
private fun CatArticlesDetailContent(
  article: Article,
  viewModel: CatArticlesDetailViewModel = hiltViewModel(),
  sharedTransitionScope: SharedTransitionScope,
  animatedContentScope: AnimatedContentScope,
  navigateUp: () -> Unit,
  navigateToPaywalls: () -> Unit,
) {
  var palette by rememberPaletteState()
  val backgroundBrush by palette.paletteBackgroundBrush()

  val customerInfo by viewModel.customerInfo.collectAsStateWithLifecycle()
  val entitlementIdentifier = stringResource(R.string.entitlement_premium)
  val isEntitled = customerInfo?.entitlements[entitlementIdentifier]?.isActive == true

  DetailsAppBar(
    article = article,
    navigateUp = navigateUp,
    backgroundBrush = backgroundBrush,
  )

  DetailsHeader(
    article = article,
    sharedTransitionScope = sharedTransitionScope,
    animatedContentScope = animatedContentScope,
    onPaletteLoaded = { palette = it },
  )

  DetailsContent(
    article = article,
    onJoinClicked = navigateToPaywalls,
    isEntitled = isEntitled,
  )
}

@Composable
private fun DetailsAppBar(
  article: Article,
  backgroundBrush: Brush,
  navigateUp: () -> Unit,
) {
  CatArticlesAppBar(
    modifier = Modifier.background(backgroundBrush),
    title = article.title,
    navigationIcon = {
      Icon(
        modifier = Modifier.clickable(onClick = { navigateUp.invoke() }),
        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
        tint = Color.White,
        contentDescription = null,
      )
    },
  )
}

@Composable
private fun DetailsHeader(
  article: Article,
  sharedTransitionScope: SharedTransitionScope,
  animatedContentScope: AnimatedContentScope,
  onPaletteLoaded: (Palette) -> Unit,
) {
  with(sharedTransitionScope) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .catArticlesSharedElement(
          isLocalInspectionMode = LocalInspectionMode.current,
          state = rememberSharedContentState(key = "article-${article.title}"),
          animatedVisibilityScope = animatedContentScope,
          boundsTransform = boundsTransform,
        ),
    ) {
      GlideImage(
        modifier = Modifier
          .fillMaxWidth()
          .height(460.dp),
        imageModel = { article.cover },
        imageOptions = ImageOptions(contentScale = ContentScale.Crop),
        component = rememberImageComponent {
          +ShimmerPlugin(
            Shimmer.Resonate(
              baseColor = Color.Transparent,
              highlightColor = Color.LightGray,
            ),
          )

          if (!LocalInspectionMode.current) {
            +PalettePlugin(
              imageModel = article.cover,
              useCache = true,
              paletteLoadedListener = { onPaletteLoaded.invoke(it) },
            )
          }
        },
        previewPlaceholder = painterResource(
          id = R.drawable.placeholder,
        ),
      )
    }
  }
}

@Composable
private fun DetailsContent(
  article: Article,
  isEntitled: Boolean,
  onJoinClicked: () -> Unit,
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(12.dp)
      .fadingEdge(isEnabled = !isEntitled),
  ) {
    Text(
      text = article.title,
      fontWeight = FontWeight.Bold,
      fontSize = 28.sp,
      color = CatArticlesTheme.colors.black,
    )

    Text(
      modifier = Modifier.padding(top = 4.dp),
      text = "${article.author} • ${article.date}",
      fontSize = 14.sp,
      color = CatArticlesTheme.colors.black,
    )

    Text(
      modifier = Modifier.padding(top = 16.dp),
      text = article.content + article.content,
      fontSize = 18.sp,
      color = CatArticlesTheme.colors.black,
    )
  }

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .height(250.dp)
      .padding(vertical = 4.dp, horizontal = 28.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Text(
      modifier = Modifier.fillMaxWidth(),
      textAlign = TextAlign.Center,
      text = stringResource(R.string.paywall_title),
      fontWeight = FontWeight.Medium,
      fontSize = 20.sp,
      color = CatArticlesTheme.colors.black,
    )

    Text(
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 16.dp),
      textAlign = TextAlign.Center,
      text = stringResource(R.string.paywall_description),
      fontSize = 16.sp,
      color = CatArticlesTheme.colors.black,
    )

    Button(
      modifier = Modifier.padding(top = 40.dp),
      colors = ButtonDefaults.buttonColors(
        containerColor = CatArticlesTheme.colors.primary,
      ),
      onClick = onJoinClicked,
    ) {
      Text(
        text = stringResource(R.string.paywall_cta),
        color = CatArticlesTheme.colors.absoluteWhite,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
      )
    }
  }
}

@Preview
@Composable
private fun CatArticlesDetailContentPreview() {
  CatArticlesTheme {
    Column(modifier = Modifier.fillMaxSize()) {
      DetailsContent(
        article = mockArticle,
        isEntitled = false,
        onJoinClicked = {},
      )
    }
  }
}
