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

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.revenuecat.purchases.Offering
import com.revenuecat.purchases.ui.revenuecatui.Paywall
import com.revenuecat.purchases.ui.revenuecatui.PaywallOptions
import com.skydoves.compose.effects.RememberedEffect

@Composable
public fun CatCustomPaywalls(
  viewModel: CustomCatPaywallsViewModel = hiltViewModel(),
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  var offering: Offering? by remember { mutableStateOf(null) }

  HandleUiState(uiState = uiState) { offering = it }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color.White)
      .padding(bottom = 100.dp),
  ) {
    Paywall(
      options = PaywallOptions.Builder(
        dismissRequest = { viewModel.navigateUp() },
      ).setOffering(offering).build(),
    )
  }
}

@Composable
private fun HandleUiState(
  uiState: PaywallsUiState,
  onFetchOffering: (Offering) -> Unit,
) {
  val context = LocalContext.current

  RememberedEffect(key1 = uiState) {
    if (uiState is PaywallsUiState.Success) {
      onFetchOffering.invoke(uiState.offering)
    } else if (uiState is PaywallsUiState.Error) {
      Toast.makeText(context, uiState.message, Toast.LENGTH_SHORT).show()
    }
  }
}
