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
package com.revenuecat.articles.paywall.core.network.di

import com.revenuecat.articles.paywall.core.network.service.CatArticlesService
import com.skydoves.sandwich.retrofit.adapters.ApiResponseCallAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.create
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object NetworkModule {

  @Provides
  @Singleton
  fun providesNetworkJson(): Json = Json {
    ignoreUnknownKeys = true
  }

  @Provides
  @Singleton
  fun provideArticlesRetrofit(networkJson: Json): Retrofit = Retrofit.Builder()
    .baseUrl(
      "https://gist.githubusercontent.com/skydoves/f3836488f8d4f1bf3e3c239759d92c0d/raw/" +
        "1376ef30281e9c9c0322c88281ea09d572b892a4/",
    )
    .addConverterFactory(networkJson.asConverterFactory("application/json".toMediaType()))
    .addCallAdapterFactory(ApiResponseCallAdapterFactory.create())
    .build()

  @Provides
  @Singleton
  fun provideArticleService(retrofit: Retrofit): CatArticlesService = retrofit.create()
}
