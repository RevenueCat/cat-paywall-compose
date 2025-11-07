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

import app.cash.turbine.test
import com.revenuecat.articles.paywall.compose.feature.subscriptions.BuildConfig
import com.revenuecat.articles.paywall.feature.subscriptions.SubscriptionManagementUiState as UiState
import com.revenuecat.articles.paywall.core.navigation.AppComposeNavigator
import com.revenuecat.articles.paywall.core.navigation.CatArticlesScreen
import com.revenuecat.articles.paywall.coredata.repository.PaywallsRepository
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Offering
import com.skydoves.sandwich.ApiResponse
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for SubscriptionManagementViewModel using RevenueCat Test Store.
 *
 * These tests verify the ViewModel's behavior when displaying subscription management
 * information by combining offering and customer info data from the repository.
 *
 * For integration testing with actual RevenueCat Test Store:
 * 1. Configure SDK with Test Store API key (available via BuildConfig.REVENUECAT_TEST_API_KEY)
 * 2. Test Store will automatically provide customer info and offerings
 * 3. Use actual PaywallsRepository instead of mocks
 * 4. Test various subscription states (active, expired, etc.)
 *
 * Example:
 * ```kotlin
 * Purchases.configure(
 *     PurchasesConfiguration.Builder(context, BuildConfig.REVENUECAT_TEST_API_KEY).build()
 * )
 * ```
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SubscriptionManagementViewModelTest {

  private lateinit var viewModel: SubscriptionManagementViewModel
  private val testDispatcher = StandardTestDispatcher()

  // Mock dependencies
  private val mockRepository: PaywallsRepository = mockk(relaxed = true)
  private val mockNavigator: AppComposeNavigator<CatArticlesScreen> = mockk(relaxed = true)
  private val mockOffering: Offering = mockk(relaxed = true)
  private val mockCustomerInfo: CustomerInfo = mockk(relaxed = true)

  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  // ========== uiState Tests ==========

  @Test
  fun `uiState should start with Loading state`() = runTest(testDispatcher) {
    // Given
    coEvery { mockRepository.fetchOffering() } returns flowOf()
    coEvery { mockRepository.fetchCustomerInfo() } returns flowOf()

    // When
    viewModel =
      SubscriptionManagementViewModel(
        mockRepository,
        mockNavigator,
      )

    // Then
    assertEquals(
      UiState.Loading,
      viewModel.uiState.value,
    )
  }

  @Test
  fun `uiState should emit Success when both offering and customerInfo are fetched successfully`() = runTest(testDispatcher) {
    // Given
    val offeringResponse = ApiResponse.Success(mockOffering)
    val customerInfoResponse = ApiResponse.Success(mockCustomerInfo)
    coEvery { mockRepository.fetchOffering() } returns flowOf(offeringResponse)
    coEvery { mockRepository.fetchCustomerInfo() } returns flowOf(customerInfoResponse)

    // When
    viewModel = SubscriptionManagementViewModel(mockRepository, mockNavigator)

    // Then
    viewModel.uiState.test {
      // Skip initial Loading state
      assertEquals(UiState.Loading, awaitItem())

      // Advance coroutines after subscribing
      testScheduler.advanceUntilIdle()

      // Check Success state
      val state = awaitItem()
      assertTrue(state is UiState.Success)
      assertEquals(mockOffering, (state as UiState.Success).offering)
      assertEquals(mockCustomerInfo, state.customerInfo)
    }
  }

  @Test
  fun `uiState should emit Error when offering fetch fails`() = runTest(testDispatcher) {
    // Given
    val errorMessage = "Failed to fetch offerings"
    val offeringResponse = ApiResponse.exception(RuntimeException(errorMessage))
    val customerInfoResponse = ApiResponse.Success(mockCustomerInfo)
    coEvery { mockRepository.fetchOffering() } returns flowOf(offeringResponse)
    coEvery { mockRepository.fetchCustomerInfo() } returns flowOf(customerInfoResponse)

    // When
    viewModel = SubscriptionManagementViewModel(mockRepository, mockNavigator)

    // Then
    viewModel.uiState.test {
      // Skip initial Loading state
      assertEquals(UiState.Loading, awaitItem())

      // Advance coroutines after subscribing
      testScheduler.advanceUntilIdle()

      // Check Error state
      val state = awaitItem()
      assertTrue(state is UiState.Error)
      assertTrue((state as UiState.Error).message.contains(errorMessage))
    }
  }

  @Test
  fun `uiState should emit Error when customerInfo fetch fails`() = runTest(testDispatcher) {
    // Given
    val errorMessage = "Failed to fetch customer info"
    val offeringResponse = ApiResponse.Success(mockOffering)
    val customerInfoResponse = ApiResponse.exception(RuntimeException(errorMessage))
    coEvery { mockRepository.fetchOffering() } returns flowOf(offeringResponse)
    coEvery { mockRepository.fetchCustomerInfo() } returns flowOf(customerInfoResponse)

    // When
    viewModel = SubscriptionManagementViewModel(mockRepository, mockNavigator)

    // Then
    viewModel.uiState.test {
      // Skip initial Loading state
      assertEquals(UiState.Loading, awaitItem())

      // Advance coroutines after subscribing
      testScheduler.advanceUntilIdle()

      // Check Error state
      val state = awaitItem()
      assertTrue(state is UiState.Error)
      assertTrue((state as UiState.Error).message.contains(errorMessage))
    }
  }

  @Test
  fun `uiState should emit Error when customerInfo is null`() = runTest(testDispatcher) {
    // Given
    val offeringResponse = ApiResponse.Success(mockOffering)
    val customerInfoResponse = ApiResponse.Success<CustomerInfo?>(null)
    coEvery { mockRepository.fetchOffering() } returns flowOf(offeringResponse)
    coEvery { mockRepository.fetchCustomerInfo() } returns flowOf(customerInfoResponse)

    // When
    viewModel = SubscriptionManagementViewModel(mockRepository, mockNavigator)

    // Then
    viewModel.uiState.test {
      // Skip initial Loading state
      assertEquals(UiState.Loading, awaitItem())

      // Advance coroutines after subscribing
      testScheduler.advanceUntilIdle()

      // Check Error state
      val state = awaitItem()
      assertTrue(state is UiState.Error)
      assertEquals("Failed to load customer information", (state as UiState.Error).message)
    }
  }

  @Test
  fun `uiState should emit Error when both offering and customerInfo fetch fail`() = runTest(testDispatcher) {
    // Given
    val offeringError = "Offering fetch failed"
    val offeringResponse = ApiResponse.exception(RuntimeException(offeringError))
    val customerInfoError = "Customer info fetch failed"
    val customerInfoResponse = ApiResponse.exception(RuntimeException(customerInfoError))
    coEvery { mockRepository.fetchOffering() } returns flowOf(offeringResponse)
    coEvery { mockRepository.fetchCustomerInfo() } returns flowOf(customerInfoResponse)

    // When
    viewModel = SubscriptionManagementViewModel(mockRepository, mockNavigator)

    // Then
    viewModel.uiState.test {
      // Skip initial Loading state
      assertEquals(UiState.Loading, awaitItem())

      // Advance coroutines after subscribing
      testScheduler.advanceUntilIdle()

      // Check Error state - should emit offering error since it's checked first
      val state = awaitItem()
      assertTrue(state is UiState.Error)
      assertTrue((state as UiState.Error).message.contains(offeringError))
    }
  }

  @Test
  fun `uiState should handle network errors for offering`() = runTest(testDispatcher) {
    // Given
    val errorMessage = "Network connection failed"
    val offeringResponse = ApiResponse.exception(RuntimeException(errorMessage))
    val customerInfoResponse = ApiResponse.Success(mockCustomerInfo)
    coEvery { mockRepository.fetchOffering() } returns flowOf(offeringResponse)
    coEvery { mockRepository.fetchCustomerInfo() } returns flowOf(customerInfoResponse)

    // When
    viewModel = SubscriptionManagementViewModel(mockRepository, mockNavigator)

    // Then
    viewModel.uiState.test {
      // Skip initial Loading state
      assertEquals(UiState.Loading, awaitItem())

      // Advance coroutines after subscribing
      testScheduler.advanceUntilIdle()

      // Check Error state
      val state = awaitItem()
      assertTrue(state is UiState.Error)
      assertTrue((state as UiState.Error).message.contains(errorMessage))
    }
  }

  @Test
  fun `uiState should handle network errors for customerInfo`() = runTest(testDispatcher) {
    // Given
    val errorMessage = "Network timeout"
    val offeringResponse = ApiResponse.Success(mockOffering)
    val customerInfoResponse = ApiResponse.exception(RuntimeException(errorMessage))
    coEvery { mockRepository.fetchOffering() } returns flowOf(offeringResponse)
    coEvery { mockRepository.fetchCustomerInfo() } returns flowOf(customerInfoResponse)

    // When
    viewModel = SubscriptionManagementViewModel(mockRepository, mockNavigator)

    // Then
    viewModel.uiState.test {
      // Skip initial Loading state
      assertEquals(UiState.Loading, awaitItem())

      // Advance coroutines after subscribing
      testScheduler.advanceUntilIdle()

      // Check Error state
      val state = awaitItem()
      assertTrue(state is UiState.Error)
      assertTrue((state as UiState.Error).message.contains(errorMessage))
    }
  }
  // ========== navigateUp Tests ==========

  @Test
  fun `navigateUp should call navigator navigateUp`() = runTest(testDispatcher) {
    // Given
    coEvery { mockRepository.fetchOffering() } returns flowOf()
    coEvery { mockRepository.fetchCustomerInfo() } returns flowOf()
    viewModel = SubscriptionManagementViewModel(mockRepository, mockNavigator)

    // When
    viewModel.navigateUp()

    // Then
    verify(exactly = 1) { mockNavigator.navigateUp() }
  }

  // ========== navigateToPaywall Tests ==========

  @Test
  fun `navigateToPaywall should navigate to Paywalls screen`() = runTest(testDispatcher) {
    // Given
    coEvery { mockRepository.fetchOffering() } returns flowOf()
    coEvery { mockRepository.fetchCustomerInfo() } returns flowOf()
    viewModel = SubscriptionManagementViewModel(mockRepository, mockNavigator)

    // When
    viewModel.navigateToPaywall()

    // Then
    verify(exactly = 1) { mockNavigator.navigate(CatArticlesScreen.Paywalls) }
  }

  @Test
  fun `navigateToPaywall should be callable multiple times`() = runTest(testDispatcher) {
    // Given
    coEvery { mockRepository.fetchOffering() } returns flowOf()
    coEvery { mockRepository.fetchCustomerInfo() } returns flowOf()
    viewModel = SubscriptionManagementViewModel(mockRepository, mockNavigator)

    // When
    viewModel.navigateToPaywall()
    viewModel.navigateToPaywall()
    viewModel.navigateToPaywall()

    // Then
    verify(exactly = 3) { mockNavigator.navigate(CatArticlesScreen.Paywalls) }
  }
}
