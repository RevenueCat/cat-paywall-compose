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
package com.revenuecat.articles.paywall.coredata.repository

import com.revenuecat.articles.paywall.core.network.CatArticlesDispatchers
import com.revenuecat.articles.paywall.core.network.Dispatcher
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Offering
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesException
import com.revenuecat.purchases.awaitCustomerInfo
import com.revenuecat.purchases.awaitOfferings
import com.skydoves.sandwich.ApiResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

internal class DetailsRepositoryImpl @Inject constructor(
  @Dispatcher(CatArticlesDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) : DetailsRepository {

  override fun fetchOffering(): Flow<ApiResponse<Offering>> = flow {
    try {
      val offerings = Purchases.sharedInstance.awaitOfferings()
      offerings.current?.let { currentOffering ->
        val response = ApiResponse.of { currentOffering }
        emit(response)
      }
    } catch (e: PurchasesException) {
      ApiResponse.exception(e)
    }
  }.flowOn(ioDispatcher)

  override fun fetchCustomerInfo(): Flow<CustomerInfo?> = flow {
    try {
      val customerInfo = Purchases.sharedInstance.awaitCustomerInfo()
      emit(customerInfo)
    } catch (e: PurchasesException) {
      emit(null)
    }
  }
}
