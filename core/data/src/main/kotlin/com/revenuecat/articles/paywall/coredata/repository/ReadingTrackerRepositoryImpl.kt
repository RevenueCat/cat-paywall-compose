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
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.revenuecat.articles.paywall.coredata.di.ReadingTrackerDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

internal class ReadingTrackerRepositoryImpl @Inject constructor(
  @ReadingTrackerDataStore private val dataStore: DataStore<Preferences>,
) : ReadingTrackerRepository {

  private val lastResetDateKey = stringPreferencesKey("last_reset_date")
  private val readArticlesKey = stringSetPreferencesKey("read_articles_today")

  override val todayReadCount: Flow<Int> = dataStore.data.map { prefs ->
    val today = LocalDate.now().toString()
    val lastReset = prefs[lastResetDateKey]
    if (lastReset != today) 0 else prefs[readArticlesKey]?.size ?: 0
  }

  override suspend fun recordArticleRead(articleTitle: String) {
    dataStore.edit { prefs ->
      val today = LocalDate.now().toString()
      val lastReset = prefs[lastResetDateKey]
      if (lastReset != today) {
        prefs[lastResetDateKey] = today
        prefs[readArticlesKey] = setOf(articleTitle)
      } else {
        val current = prefs[readArticlesKey] ?: emptySet()
        prefs[readArticlesKey] = current + articleTitle
      }
    }
  }
}
