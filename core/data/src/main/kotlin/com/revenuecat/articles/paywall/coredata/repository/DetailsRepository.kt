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

import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Offering
import com.skydoves.sandwich.ApiResponse
import kotlinx.coroutines.flow.Flow

interface DetailsRepository {

  fun fetchOffering(): Flow<ApiResponse<Offering>>

  fun fetchCustomerInfo(): Flow<CustomerInfo?>
}
