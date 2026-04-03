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

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.revenuecat.articles.paywall.core.model.Article
import com.revenuecat.articles.paywall.coredata.repository.ArticlesRepository
import com.revenuecat.articles.paywall.coredata.repository.BookmarksRepository
import com.revenuecat.articles.paywall.coredata.repository.PaywallsRepository
import com.revenuecat.articles.paywall.coredata.repository.ReadingTrackerRepository
import com.revenuecat.purchases.CustomerInfo
import com.skydoves.sandwich.fold
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CatArticlesViewModel @Inject constructor(
  repository: ArticlesRepository,
  private val bookmarksRepository: BookmarksRepository,
  paywallsRepository: PaywallsRepository,
  readingTrackerRepository: ReadingTrackerRepository,
) : ViewModel() {

  val uiState: StateFlow<HomeUiState> = repository.fetchArticles()
    .mapLatest { response ->
      response.fold(
        onSuccess = { HomeUiState.Success(it) },
        onFailure = { HomeUiState.Error(it) },
      )
    }.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5_000),
      initialValue = HomeUiState.Loading,
    )

  val bookmarkedTitles: StateFlow<Set<String>> = bookmarksRepository.bookmarkedArticleTitles
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5_000),
      initialValue = emptySet(),
    )

  val customerInfo: StateFlow<CustomerInfo?> = paywallsRepository.fetchCustomerInfo()
    .map { it.fold(onSuccess = { c -> c }, onFailure = { null }) }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5_000),
      initialValue = null,
    )

  val todayReadCount: StateFlow<Int> = readingTrackerRepository.todayReadCount
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5_000),
      initialValue = 0,
    )

  fun toggleBookmark(articleTitle: String) {
    viewModelScope.launch { bookmarksRepository.toggleBookmark(articleTitle) }
  }
}

@Stable
sealed interface HomeUiState {

  data object Loading : HomeUiState

  data class Success(val articles: List<Article>) : HomeUiState

  data class Error(val message: String?) : HomeUiState
}
