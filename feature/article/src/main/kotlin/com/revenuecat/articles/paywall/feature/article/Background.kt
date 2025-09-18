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
package com.revenuecat.articles.paywall.feature.article

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.kmpalette.palette.graphics.Palette
import com.revenuecat.articles.paywall.core.designsystem.theme.CatArticlesTheme

@Composable
internal fun Palette?.paletteBackgroundBrush(): State<Brush> {
  val defaultBackground = CatArticlesTheme.colors.background
  return remember(this) {
    derivedStateOf {
      val light = this?.lightVibrantSwatch?.rgb
      val domain = this?.dominantSwatch?.rgb
      if (domain != null) {
        val domainColor = Color(domain)
        if (light != null) {
          val lightColor = Color(light)
          val gradient = arrayOf(
            0.0f to domainColor,
            1f to lightColor,
          )
          Brush.verticalGradient(colorStops = gradient)
        } else {
          Brush.linearGradient(colors = listOf(domainColor, domainColor))
        }
      } else {
        Brush.linearGradient(colors = listOf(defaultBackground, defaultBackground))
      }
    }
  }
}
