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
package com.revenuecat.articles.paywall.feature.home

import app.cash.turbine.test
import com.revenuecat.articles.paywall.coredata.repository.PaywallsRepository
import com.revenuecat.purchases.CustomerInfo
import com.skydoves.sandwich.ApiResponse
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CatArticlesViewModelTest {

  private val testDispatcher = StandardTestDispatcher()

  private val mockPaywallsRepository: PaywallsRepository = mockk()
  private val customerInfoBeforePurchase: CustomerInfo = mockk()
  private val customerInfoAfterPurchase: CustomerInfo = mockk()

  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun `refreshCustomerInfo replaces the customer info read when the screen opened`() = runTest(
    testDispatcher,
  ) {
    // Given
    every { mockPaywallsRepository.fetchCustomerInfo() } returnsMany listOf(
      flowOf(ApiResponse.Success(customerInfoBeforePurchase)),
      flowOf(ApiResponse.Success(customerInfoAfterPurchase)),
    )
    val viewModel = CatArticlesViewModel(
      repository = mockk(relaxed = true),
      bookmarksRepository = mockk(relaxed = true),
      paywallsRepository = mockPaywallsRepository,
      readingTrackerRepository = mockk(relaxed = true),
    )

    viewModel.customerInfo.test {
      assertNull(awaitItem())
      assertSame(customerInfoBeforePurchase, awaitItem())

      // When
      viewModel.refreshCustomerInfo()

      // Then
      assertSame(customerInfoAfterPurchase, awaitItem())
    }
  }
}
