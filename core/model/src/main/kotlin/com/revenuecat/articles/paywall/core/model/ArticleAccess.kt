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

/** How many free articles a user without the premium entitlement can open per day. */
const val DAILY_FREE_ARTICLE_LIMIT: Int = 3

/** What tapping an article leads to. */
enum class ArticleAccess {
  Open,
  PremiumPaywall,
  DailyLimitPaywall,
}

/**
 * Decides what tapping [article] leads to. The order is the rule: a premium article never opens
 * without the entitlement, and the daily free quota never gates it.
 */
fun resolveArticleAccess(
  article: Article,
  isEntitled: Boolean,
  todayReadCount: Int,
  dailyFreeLimit: Int = DAILY_FREE_ARTICLE_LIMIT,
): ArticleAccess = when {
  isEntitled -> ArticleAccess.Open
  article.premium -> ArticleAccess.PremiumPaywall
  todayReadCount >= dailyFreeLimit -> ArticleAccess.DailyLimitPaywall
  else -> ArticleAccess.Open
}
