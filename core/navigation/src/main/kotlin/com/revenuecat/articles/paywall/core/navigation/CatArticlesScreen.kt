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
package com.revenuecat.articles.paywall.core.navigation

import com.revenuecat.articles.paywall.core.model.Article
import kotlinx.serialization.Serializable
import kotlin.reflect.typeOf

sealed interface CatArticlesScreen {

  @Serializable
  data object CatHome : CatArticlesScreen

  @Serializable
  data object Paywalls : CatArticlesScreen

  @Serializable
  data class CatArticle(val article: Article) : CatArticlesScreen {
    companion object {
      val typeMap = mapOf(typeOf<Article>() to CatArticlesType)
    }
  }
}
