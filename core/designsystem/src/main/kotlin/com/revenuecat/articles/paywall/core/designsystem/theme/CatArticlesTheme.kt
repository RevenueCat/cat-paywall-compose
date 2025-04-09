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
@file:OptIn(ExperimentalComposeUiApi::class)

package com.revenuecat.articles.paywall.core.designsystem.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId

/**
 * Local providers for various properties we connect to our components, for styling.
 */
private val LocalColors = compositionLocalOf<CatArticleColors> {
  error(
    "No colors provided! Make sure to wrap all usages of " +
      "ArtArticles components in CatArticleTheme.",
  )
}

@Composable
fun CatArticlesTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  colors: CatArticleColors = if (darkTheme) {
    CatArticleColors.defaultDarkColors()
  } else {
    CatArticleColors.defaultLightColors()
  },
  background: CatArticlesBackground = CatArticlesBackground.defaultBackground(darkTheme),
  content: @Composable () -> Unit,
) {
  CompositionLocalProvider(
    LocalColors provides colors,
    LocalBackgroundTheme provides background,
  ) {
    Box(
      modifier = Modifier
        .background(background.color)
        .semantics { testTagsAsResourceId = true },
    ) {
      content()
    }
  }
}

/**
 * Contains ease-of-use accessors for different properties used to style and customize the app
 * look and feel.
 */
object CatArticlesTheme {
  /**
   * Retrieves the current [CatArticleColors] at the call site's position in the hierarchy.
   */
  val colors: CatArticleColors
    @Composable
    @ReadOnlyComposable
    get() = LocalColors.current

  /**
   * Retrieves the current [CatArticleColors] at the call site's position in the hierarchy.
   */
  val background: CatArticlesBackground
    @Composable
    @ReadOnlyComposable
    get() = LocalBackgroundTheme.current
}
