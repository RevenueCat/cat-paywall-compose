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

import android.app.Activity
import app.cash.turbine.test
import com.revenuecat.articles.paywall.coredata.repository.PaywallsRepository
import com.revenuecat.purchases.Offering
import com.revenuecat.purchases.Package
import com.revenuecat.purchases.PurchaseResult
import com.skydoves.sandwich.ApiResponse
import io.mockk.coEvery
import io.mockk.mockk
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
 * Unit tests for CustomCatPaywallsViewModel using RevenueCat Test Store.
 *
 * These tests verify the ViewModel's behavior when interacting with the repository
 * to fetch offerings and handle purchases. The repository is mocked to simulate
 * various scenarios including successful purchases and error handling.
 *
 * For integration testing with actual RevenueCat Test Store:
 * 1. Configure SDK with Test Store API key (available via BuildConfig.REVENUECAT_TEST_API_KEY)
 * 2. Test Store products will automatically work without additional setup
 * 3. Use actual PaywallsRepository instead of mocks
 *
 * Example:
 * ```kotlin
 * Purchases.configure(
 *     PurchasesConfiguration.Builder(context, BuildConfig.REVENUECAT_TEST_API_KEY).build()
 * )
 * ```
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CustomCatPaywallsViewModelTest {

  private lateinit var viewModel: CustomCatPaywallsViewModel
  private val testDispatcher = StandardTestDispatcher()

  // Mock dependencies
  private val mockRepository: PaywallsRepository = mockk(relaxed = true)
  private val mockOffering: Offering = mockk(relaxed = true)
  private val mockPackage: Package = mockk(relaxed = true)
  private val mockActivity: Activity = mockk(relaxed = true)
  private val mockPurchaseResult: PurchaseResult = mockk(relaxed = true)

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

    // When
    viewModel = CustomCatPaywallsViewModel(mockRepository)

    // Then
    assertEquals(PaywallsUiState.Loading, viewModel.uiState.value)
  }

  @Test
  fun `uiState should emit Success when offering is fetched successfully`() = runTest(
    testDispatcher,
  ) {
    // Given
    val successResponse = ApiResponse.Success(mockOffering)
    coEvery { mockRepository.fetchOffering() } returns flowOf(successResponse)

    // When
    viewModel = CustomCatPaywallsViewModel(mockRepository)

    // Then
    viewModel.uiState.test {
      // Skip initial Loading state
      assertEquals(PaywallsUiState.Loading, awaitItem())

      // Advance coroutines after subscribing
      testScheduler.advanceUntilIdle()

      // Check Success state
      val state = awaitItem()
      assertTrue(state is PaywallsUiState.Success)
      assertEquals(mockOffering, (state as PaywallsUiState.Success).offering)
    }
  }

  @Test
  fun `uiState should emit Error when offering fetch fails`() = runTest(testDispatcher) {
    // Given
    val errorMessage = "Failed to fetch offerings"
    val errorResponse = ApiResponse.exception(RuntimeException(errorMessage))
    coEvery { mockRepository.fetchOffering() } returns flowOf(errorResponse)

    // When
    viewModel = CustomCatPaywallsViewModel(mockRepository)

    // Then
    viewModel.uiState.test {
      // Skip initial Loading state
      assertEquals(PaywallsUiState.Loading, awaitItem())

      // Advance coroutines after subscribing
      testScheduler.advanceUntilIdle()

      // Check Error state
      val state = awaitItem()
      assertTrue(state is PaywallsUiState.Error)
      assertTrue((state as PaywallsUiState.Error).message?.contains(errorMessage) == true)
    }
  }

  @Test
  fun `uiState should handle network errors gracefully`() = runTest(testDispatcher) {
    // Given
    val errorMessage = "Network connection failed"
    val errorResponse = ApiResponse.exception(RuntimeException(errorMessage))
    coEvery { mockRepository.fetchOffering() } returns flowOf(errorResponse)

    // When
    viewModel = CustomCatPaywallsViewModel(mockRepository)

    // Then
    viewModel.uiState.test {
      // Skip initial Loading state
      assertEquals(PaywallsUiState.Loading, awaitItem())

      // Advance coroutines after subscribing
      testScheduler.advanceUntilIdle()

      // Check Error state
      val state = awaitItem()
      assertTrue(state is PaywallsUiState.Error)
      assertTrue((state as PaywallsUiState.Error).message?.contains(errorMessage) == true)
    }
  }

  // ========== purchaseUiState Tests ==========

  @Test
  fun `purchaseUiState should start with None state`() = runTest(testDispatcher) {
    // Given
    coEvery { mockRepository.fetchOffering() } returns flowOf()

    // When
    viewModel = CustomCatPaywallsViewModel(mockRepository)

    // Then
    assertEquals(PurchaseUiState.None, viewModel.purchaseUiState.value)
  }

  @Test
  fun `purchaseUiState should remain None when None event is handled`() = runTest(testDispatcher) {
    // Given
    coEvery { mockRepository.fetchOffering() } returns flowOf()
    viewModel = CustomCatPaywallsViewModel(mockRepository)

    // When
    viewModel.handleEvent(PaywallEvent.None)
    advanceUntilIdle()

    // Then
    viewModel.purchaseUiState.test {
      val state = awaitItem()
      assertEquals(PurchaseUiState.None, state)
    }
  }

  @Test
  fun `purchaseUiState should emit Success when purchase completes successfully`() = runTest(
    testDispatcher,
  ) {
    // Given
    coEvery { mockRepository.fetchOffering() } returns flowOf()
    val successResponse = ApiResponse.Success(mockPurchaseResult)
    coEvery {
      mockRepository.awaitPurchases(mockActivity, mockPackage)
    } returns flowOf(successResponse)

    viewModel = CustomCatPaywallsViewModel(mockRepository)

    // Then
    viewModel.purchaseUiState.test {
      // Skip initial None state
      assertEquals(PurchaseUiState.None, awaitItem())

      // When - trigger purchase after subscribing
      viewModel.handleEvent(PaywallEvent.Purchases(mockActivity, mockPackage))
      testScheduler.advanceUntilIdle()

      // Check Success state
      val state = awaitItem()
      assertTrue(state is PurchaseUiState.Success)
      assertEquals(mockPurchaseResult, (state as PurchaseUiState.Success).purchaseResult)
    }
  }

  @Test
  fun `purchaseUiState should emit Error when purchase fails`() = runTest(testDispatcher) {
    // Given
    coEvery { mockRepository.fetchOffering() } returns flowOf()
    val errorMessage = "Purchase failed"
    val errorResponse = ApiResponse.exception(RuntimeException(errorMessage))
    coEvery {
      mockRepository.awaitPurchases(mockActivity, mockPackage)
    } returns flowOf(errorResponse)

    viewModel = CustomCatPaywallsViewModel(mockRepository)

    // Then
    viewModel.purchaseUiState.test {
      // Skip initial None state
      assertEquals(PurchaseUiState.None, awaitItem())

      // When - trigger purchase after subscribing
      viewModel.handleEvent(PaywallEvent.Purchases(mockActivity, mockPackage))
      testScheduler.advanceUntilIdle()

      // Check Error state
      val state = awaitItem()
      assertTrue(state is PurchaseUiState.Error)
      assertTrue((state as PurchaseUiState.Error).message?.contains(errorMessage) == true)
    }
  }

  @Test
  fun `purchaseUiState should handle user cancelled purchase`() = runTest(testDispatcher) {
    // Given
    coEvery { mockRepository.fetchOffering() } returns flowOf()
    val errorMessage = "User cancelled the purchase"
    val errorResponse = ApiResponse.exception(RuntimeException(errorMessage))
    coEvery {
      mockRepository.awaitPurchases(mockActivity, mockPackage)
    } returns flowOf(errorResponse)

    viewModel = CustomCatPaywallsViewModel(mockRepository)

    // Then
    viewModel.purchaseUiState.test {
      // Skip initial None state
      assertEquals(PurchaseUiState.None, awaitItem())

      // When - trigger purchase after subscribing
      viewModel.handleEvent(PaywallEvent.Purchases(mockActivity, mockPackage))
      testScheduler.advanceUntilIdle()

      // Check Error state
      val state = awaitItem()
      assertTrue(state is PurchaseUiState.Error)
      assertTrue((state as PurchaseUiState.Error).message?.contains(errorMessage) == true)
    }
  }

  @Test
  fun `purchaseUiState should handle product already owned error`() = runTest(testDispatcher) {
    // Given
    coEvery { mockRepository.fetchOffering() } returns flowOf()
    val errorMessage = "Product already owned"
    val errorResponse = ApiResponse.exception(RuntimeException(errorMessage))
    coEvery {
      mockRepository.awaitPurchases(mockActivity, mockPackage)
    } returns flowOf(errorResponse)

    viewModel = CustomCatPaywallsViewModel(mockRepository)

    // Then
    viewModel.purchaseUiState.test {
      // Skip initial None state
      assertEquals(PurchaseUiState.None, awaitItem())

      // When - trigger purchase after subscribing
      viewModel.handleEvent(PaywallEvent.Purchases(mockActivity, mockPackage))
      testScheduler.advanceUntilIdle()

      // Check Error state
      val state = awaitItem()
      assertTrue(state is PurchaseUiState.Error)
      assertTrue((state as PurchaseUiState.Error).message?.contains(errorMessage) == true)
    }
  }

  // ========== handleEvent Tests ==========

  @Test
  fun `handleEvent should emit event to SharedFlow`() = runTest(testDispatcher) {
    // Given
    coEvery { mockRepository.fetchOffering() } returns flowOf()
    viewModel = CustomCatPaywallsViewModel(mockRepository)

    // When
    val purchaseEvent = PaywallEvent.Purchases(mockActivity, mockPackage)
    viewModel.handleEvent(purchaseEvent)

    // Then
    viewModel.event.test {
      val emittedEvent = awaitItem()
      assertEquals(purchaseEvent, emittedEvent)
    }
  }

  @Test
  fun `handleEvent should trigger repository purchase when Purchases event is handled`() = runTest(
    testDispatcher,
  ) {
    // Given
    coEvery { mockRepository.fetchOffering() } returns flowOf()
    coEvery {
      mockRepository.awaitPurchases(any(), any())
    } returns flowOf(ApiResponse.Success(mockPurchaseResult))

    viewModel = CustomCatPaywallsViewModel(mockRepository)

    // When
    viewModel.handleEvent(PaywallEvent.Purchases(mockActivity, mockPackage))
    advanceUntilIdle()

    // Then
    coEvery {
      mockRepository.awaitPurchases(mockActivity, mockPackage)
    }
  }
}
