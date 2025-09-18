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

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.revenuecat.articles.paywall.core.navigation.AppComposeNavigator
import com.revenuecat.articles.paywall.core.navigation.CatArticlesScreen
import com.revenuecat.articles.paywall.coredata.repository.PaywallsRepository
import com.revenuecat.purchases.Offering
import com.skydoves.sandwich.fold
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class CustomCatPaywallsViewModel @Inject constructor(
  repository: PaywallsRepository,
  private val navigator: AppComposeNavigator<CatArticlesScreen>,
) : ViewModel() {

  val customerInfo = repository.fetchCustomerInfo().stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = null,
  )

  val uiState: StateFlow<PaywallsUiState> = repository.fetchOffering()
    .mapLatest { response ->
      response.fold(
        onSuccess = { PaywallsUiState.Success(it) },
        onFailure = { PaywallsUiState.Error(it) },
      )
    }.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = PaywallsUiState.Loading,
    )

  fun navigateUp() {
    navigator.navigateUp()
  }
}

@Stable
sealed interface PaywallsUiState {

  data object Loading : PaywallsUiState

  data class Success(val offering: Offering) : PaywallsUiState

  data class Error(val message: String?) : PaywallsUiState
}
