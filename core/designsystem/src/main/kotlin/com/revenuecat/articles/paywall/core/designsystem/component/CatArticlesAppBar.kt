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
package com.revenuecat.articles.paywall.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.revenuecat.articles.paywall.compose.core.designsystem.R
import com.revenuecat.articles.paywall.core.designsystem.theme.CatArticlesTheme

@Composable
fun CatArticlesAppBar(
  modifier: Modifier = Modifier,
  title: String = stringResource(id = R.string.app_name),
  navigationIcon: @Composable () -> Unit = {},
  actions: @Composable () -> Unit = {},
) {
  TopAppBar(
    modifier = modifier,
    title = {
      Text(
        text = title,
        color = CatArticlesTheme.colors.absoluteWhite,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
      )
    },
    navigationIcon = navigationIcon,
    actions = { actions() },
    colors = TopAppBarDefaults.topAppBarColors().copy(
      containerColor = Color.Transparent,
    ),
  )
}

@Preview
@Composable
private fun CatArticlesAppBarPreview() {
  CatArticlesTheme {
    CatArticlesAppBar(modifier = Modifier.background(CatArticlesTheme.colors.primary))
  }
}
