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
package com.revenuecat.articles.paywall.feature.article

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.revenuecat.articles.paywall.coredata.repository.BookmarksRepository
import com.revenuecat.articles.paywall.coredata.repository.PaywallsRepository
import com.revenuecat.articles.paywall.coredata.repository.ReadingTrackerRepository
import com.revenuecat.purchases.Offering
import com.revenuecat.purchases.Package
import com.skydoves.sandwich.fold
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CatArticlesDetailViewModel @Inject constructor(
  private val repository: PaywallsRepository,
  private val bookmarksRepository: BookmarksRepository,
  private val readingTrackerRepository: ReadingTrackerRepository,
) : ViewModel() {

  val customerInfo = repository.fetchCustomerInfo()
    .map { response ->
      response.fold(
        onSuccess = { it },
        onFailure = { null },
      )
    }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5_000),
      initialValue = null,
    )

  val bookmarkedTitles: StateFlow<Set<String>> = bookmarksRepository.bookmarkedArticleTitles
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5_000),
      initialValue = emptySet(),
    )

  val todayReadCount: StateFlow<Int> = readingTrackerRepository.todayReadCount
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5_000),
      initialValue = 0,
    )

  val offering: StateFlow<Offering?> = repository.fetchOffering()
    .map { response -> response.fold(onSuccess = { it }, onFailure = { null }) }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5_000),
      initialValue = null,
    )

  private val _promoShownThisSession = MutableStateFlow(false)

  val shouldShowPromo: StateFlow<Boolean> = combine(
    todayReadCount,
    customerInfo,
    _promoShownThisSession,
  ) { count, info, shown ->
    val isEntitled = info?.entitlements?.active?.isNotEmpty() == true
    count == 3 && !isEntitled && !shown
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5_000),
    initialValue = false,
  )

  fun dismissPromo() {
    _promoShownThisSession.value = true
  }

  fun purchasePackage(activity: Activity, pkg: Package) {
    repository.awaitPurchases(activity, pkg)
      .onEach { response ->
        response.fold(
          onSuccess = { dismissPromo() },
          onFailure = { },
        )
      }
      .launchIn(viewModelScope)
  }

  fun recordRead(articleTitle: String) {
    viewModelScope.launch { readingTrackerRepository.recordArticleRead(articleTitle) }
  }

  fun toggleBookmark(articleTitle: String, isPremium: Boolean, onNotPremium: () -> Unit) {
    if (!isPremium) {
      onNotPremium()
      return
    }
    viewModelScope.launch { bookmarksRepository.toggleBookmark(articleTitle) }
  }
}
