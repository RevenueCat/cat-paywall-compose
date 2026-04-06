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

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.revenuecat.articles.paywall.coredata.di.BookmarksDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class BookmarksRepositoryImpl @Inject constructor(
  @BookmarksDataStore private val dataStore: DataStore<Preferences>,
) : BookmarksRepository {

  private val bookmarksKey = stringSetPreferencesKey("bookmarked_articles")

  override val bookmarkedArticleTitles: Flow<Set<String>> = dataStore.data.map { prefs ->
    prefs[bookmarksKey] ?: emptySet()
  }

  override suspend fun toggleBookmark(articleTitle: String) {
    dataStore.edit { prefs ->
      val current = prefs[bookmarksKey] ?: emptySet()
      prefs[bookmarksKey] =
        if (articleTitle in current) current - articleTitle else current + articleTitle
    }
  }
}
