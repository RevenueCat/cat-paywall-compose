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
package com.revenuecat.articles.paywall.core.network

import com.revenuecat.articles.paywall.core.model.Article
import com.revenuecat.articles.paywall.core.network.di.NetworkModule
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Decodes payloads shaped like the articles gist with the Json instance Retrofit uses. */
class ArticleDecodingTest {

  private val networkJson: Json = NetworkModule.providesNetworkJson()

  @Test
  fun `article without the premium key decodes as not premium`() {
    val articles = networkJson.decodeFromString<List<Article>>(
      """
      [
        {
          "title": "Effective Kotlin",
          "content": "Content",
          "description": "Description",
          "author": "Author",
          "date": "2025-01-01",
          "cover": "https://example.com/cover.jpg"
        }
      ]
      """.trimIndent(),
    )

    assertEquals(1, articles.size)
    assertFalse(articles.single().premium)
  }

  @Test
  fun `article with premium true decodes as premium`() {
    val articles = networkJson.decodeFromString<List<Article>>(
      """
      [
        {
          "title": "Modern Android Testing",
          "content": "Content",
          "description": "Description",
          "author": "Author",
          "date": "2025-01-01",
          "cover": "https://example.com/cover.jpg",
          "premium": true
        }
      ]
      """.trimIndent(),
    )

    assertEquals(1, articles.size)
    assertTrue(articles.single().premium)
  }
}
