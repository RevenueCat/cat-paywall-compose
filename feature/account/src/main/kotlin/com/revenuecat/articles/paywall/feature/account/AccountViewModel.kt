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
package com.revenuecat.articles.paywall.feature.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.revenuecat.articles.paywall.coredata.repository.PaywallsRepository
import com.revenuecat.purchases.CustomerInfo
import com.skydoves.sandwich.fold
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
  private val repository: PaywallsRepository,
) : ViewModel() {

  private val _isAnonymous = MutableStateFlow(repository.isAnonymous())
  val isAnonymous: StateFlow<Boolean> = _isAnonymous.asStateFlow()

  private val _customerInfo = MutableStateFlow<CustomerInfo?>(null)
  val customerInfo: StateFlow<CustomerInfo?> = _customerInfo.asStateFlow()

  private val _isLoading = MutableStateFlow(false)
  val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

  private val _errorMessage = MutableStateFlow<String?>(null)
  val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

  init {
    refreshCustomerInfo()
  }

  private fun refreshCustomerInfo() {
    repository.fetchCustomerInfo()
      .onEach { response ->
        response.fold(
          onSuccess = { _customerInfo.value = it },
          onFailure = { },
        )
      }
      .launchIn(viewModelScope)
  }

  fun logIn(userId: String) {
    if (userId.isBlank()) return
    _isLoading.value = true
    _errorMessage.value = null
    repository.logIn(userId)
      .onEach { response ->
        _isLoading.value = false
        response.fold(
          onSuccess = { customerInfo ->
            _customerInfo.value = customerInfo
            _isAnonymous.value = repository.isAnonymous()
          },
          onFailure = {
            _errorMessage.value = "Login failed. Please try again."
          },
        )
      }
      .launchIn(viewModelScope)
  }

  fun logOut() {
    _isLoading.value = true
    _errorMessage.value = null
    repository.logOut()
      .onEach { response ->
        _isLoading.value = false
        response.fold(
          onSuccess = { customerInfo ->
            _customerInfo.value = customerInfo
            _isAnonymous.value = repository.isAnonymous()
          },
          onFailure = {
            _errorMessage.value = "Logout failed. Please try again."
          },
        )
      }
      .launchIn(viewModelScope)
  }

  fun clearError() {
    _errorMessage.value = null
  }
}
