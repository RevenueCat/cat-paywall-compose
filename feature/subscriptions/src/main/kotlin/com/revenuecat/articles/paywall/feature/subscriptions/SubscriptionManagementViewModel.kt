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
package com.revenuecat.articles.paywall.feature.subscriptions

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.revenuecat.articles.paywall.coredata.repository.PaywallsRepository
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Offering
import com.skydoves.sandwich.fold
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SubscriptionManagementViewModel @Inject constructor(
  repository: PaywallsRepository,
) : ViewModel() {

  val uiState: StateFlow<SubscriptionManagementUiState> = combine(
    repository.fetchOffering(),
    repository.fetchCustomerInfo(),
  ) { offeringResponse, customerInfoResponse ->
    offeringResponse.fold(
      onSuccess = { offering ->
        customerInfoResponse.fold(
          onSuccess = { customerInfo ->
            if (customerInfo == null) {
              SubscriptionManagementUiState.Error("Failed to load customer information")
            } else {
              SubscriptionManagementUiState.Success(
                offering = offering,
                customerInfo = customerInfo,
              )
            }
          },
          onFailure = { message ->
            SubscriptionManagementUiState.Error(
              message,
            )
          },
        )
      },
      onFailure = {
        SubscriptionManagementUiState.Error(it)
      },
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = SubscriptionManagementUiState.Loading,
  )
}

@Stable
sealed interface SubscriptionManagementUiState {

  data object Loading : SubscriptionManagementUiState

  data class Success(
    val offering: Offering,
    val customerInfo: CustomerInfo,
  ) : SubscriptionManagementUiState

  data class Error(val message: String) : SubscriptionManagementUiState
}
