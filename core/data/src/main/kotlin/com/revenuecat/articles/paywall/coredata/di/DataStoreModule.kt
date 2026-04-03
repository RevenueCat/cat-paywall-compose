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
package com.revenuecat.articles.paywall.coredata.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

private val Context.readingTrackerDataStore: DataStore<Preferences> by
  preferencesDataStore(name = "reading_tracker")
private val Context.bookmarksDataStore: DataStore<Preferences> by
  preferencesDataStore(name = "bookmarks")

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ReadingTrackerDataStore

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BookmarksDataStore

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

  @Provides
  @Singleton
  @ReadingTrackerDataStore
  fun providesReadingTrackerDataStore(
    @ApplicationContext context: Context,
  ): DataStore<Preferences> = context.readingTrackerDataStore

  @Provides
  @Singleton
  @BookmarksDataStore
  fun providesBookmarksDataStore(
    @ApplicationContext context: Context,
  ): DataStore<Preferences> = context.bookmarksDataStore
}
