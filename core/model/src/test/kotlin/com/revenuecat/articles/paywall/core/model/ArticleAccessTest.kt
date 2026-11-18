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
package com.revenuecat.articles.paywall.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * The daily limit is written out as 3 here on purpose, so the expectations do not move if the
 * production constant does.
 */
class ArticleAccessTest {

  private val freeArticle = MockUtils.mockArticle
  private val premiumArticle = MockUtils.mockArticle.copy(premium = true)

  // ========== One test per branch ==========

  @Test
  fun `entitled user opens a free article even over the daily limit`() {
    val access = resolveArticleAccess(freeArticle, isEntitled = true, todayReadCount = 5)

    assertEquals(ArticleAccess.Open, access)
  }

  @Test
  fun `non-entitled user tapping a premium article gets the premium paywall`() {
    val access = resolveArticleAccess(premiumArticle, isEntitled = false, todayReadCount = 1)

    assertEquals(ArticleAccess.PremiumPaywall, access)
  }

  @Test
  fun `non-entitled user at the daily limit gets the daily limit paywall for a free article`() {
    val access = resolveArticleAccess(freeArticle, isEntitled = false, todayReadCount = 3)

    assertEquals(ArticleAccess.DailyLimitPaywall, access)
  }

  @Test
  fun `non-entitled user under the daily limit opens a free article`() {
    val access = resolveArticleAccess(freeArticle, isEntitled = false, todayReadCount = 2)

    assertEquals(ArticleAccess.Open, access)
  }

  // ========== Premium and quota interplay ==========

  @Test
  fun `entitled user opening a premium article at or over the quota opens it`() {
    assertEquals(
      ArticleAccess.Open,
      resolveArticleAccess(premiumArticle, isEntitled = true, todayReadCount = 3),
    )
    assertEquals(
      ArticleAccess.Open,
      resolveArticleAccess(premiumArticle, isEntitled = true, todayReadCount = 10),
    )
  }

  @Test
  fun `non-entitled premium article with 0 reads gets the premium paywall, not the quota one`() {
    val access = resolveArticleAccess(premiumArticle, isEntitled = false, todayReadCount = 0)

    assertEquals(ArticleAccess.PremiumPaywall, access)
  }

  @Test
  fun `non-entitled premium article over the quota gets the premium paywall, not the quota one`() {
    val access = resolveArticleAccess(premiumArticle, isEntitled = false, todayReadCount = 3)

    assertEquals(ArticleAccess.PremiumPaywall, access)
  }
}
