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
package com.revenuecat.articles.paywall.feature.bookmarks

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.revenuecat.articles.paywall.core.model.Article
import com.revenuecat.articles.paywall.coredata.repository.ArticlesRepository
import com.revenuecat.articles.paywall.coredata.repository.BookmarksRepository
import com.skydoves.sandwich.fold
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@Stable
sealed interface BookmarksUiState {
  data object Loading : BookmarksUiState
  data class Success(val articles: List<Article>) : BookmarksUiState
  data object Empty : BookmarksUiState
}

@HiltViewModel
class BookmarksViewModel @Inject constructor(
  articlesRepository: ArticlesRepository,
  bookmarksRepository: BookmarksRepository,
) : ViewModel() {

  val uiState: StateFlow<BookmarksUiState> = combine(
    articlesRepository.fetchArticles(),
    bookmarksRepository.bookmarkedArticleTitles,
  ) { articlesResponse, bookmarkedTitles ->
    articlesResponse.fold(
      onSuccess = { articles ->
        val bookmarked = articles.filter { it.title in bookmarkedTitles }
        if (bookmarked.isEmpty()) BookmarksUiState.Empty else BookmarksUiState.Success(bookmarked)
      },
      onFailure = { BookmarksUiState.Empty },
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5_000),
    initialValue = BookmarksUiState.Loading,
  )
}
