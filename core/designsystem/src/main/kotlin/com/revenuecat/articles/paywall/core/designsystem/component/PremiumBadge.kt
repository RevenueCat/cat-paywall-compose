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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.revenuecat.articles.paywall.core.designsystem.theme.CatArticlesTheme

/**
 * Marks a premium-only article. Dark translucent with white text so it stays legible on photos.
 *
 * @param isLocked Shows a lock while the user does not have the premium entitlement.
 */
@Composable
fun PremiumBadge(isLocked: Boolean, modifier: Modifier = Modifier) {
  Row(
    modifier = modifier
      .background(
        color = CatArticlesTheme.colors.absoluteBlack.copy(alpha = 0.65f),
        shape = RoundedCornerShape(percent = 50),
      )
      .padding(horizontal = 8.dp, vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    if (isLocked) {
      Icon(
        modifier = Modifier.size(12.dp),
        imageVector = Icons.Default.Lock,
        contentDescription = "Locked",
        tint = CatArticlesTheme.colors.absoluteWhite,
      )
      Spacer(modifier = Modifier.width(4.dp))
    }

    Text(
      text = "Premium",
      color = CatArticlesTheme.colors.absoluteWhite,
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )
  }
}

@Preview
@Composable
private fun PremiumBadgePreview() {
  CatArticlesTheme {
    Row {
      PremiumBadge(isLocked = true, modifier = Modifier.padding(8.dp))
      PremiumBadge(isLocked = false, modifier = Modifier.padding(8.dp))
    }
  }
}
