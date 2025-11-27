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
package com.revenuecat.articles.paywall.feature.subscriptions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.revenuecat.articles.paywall.core.designsystem.component.CatArticlesAppBar
import com.revenuecat.articles.paywall.core.designsystem.component.CatArticlesCircularProgress
import com.revenuecat.articles.paywall.core.designsystem.theme.CatArticlesTheme
import com.revenuecat.articles.paywall.core.navigation.CatArticlesScreen
import com.revenuecat.articles.paywall.core.navigation.currentComposeNavigator
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.Offering
import com.revenuecat.purchases.Package
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SubscriptionManagementScreen(
  viewModel: SubscriptionManagementViewModel = hiltViewModel(),
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val composeNavigator = currentComposeNavigator

  Column(modifier = Modifier.fillMaxSize()) {
    CatArticlesAppBar(
      modifier = Modifier.background(CatArticlesTheme.colors.primary),
      title = "Manage Subscription",
      navigationIcon = {
        IconButton(onClick = { composeNavigator.navigateUp() }) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = Color.White,
          )
        }
      },
    )

    when (uiState) {
      is SubscriptionManagementUiState.Loading -> {
        Box(modifier = Modifier.fillMaxSize()) {
          CatArticlesCircularProgress()
        }
      }

      is SubscriptionManagementUiState.Success -> {
        val state = uiState as SubscriptionManagementUiState.Success
        SubscriptionContent(
          offering = state.offering,
          customerInfo = state.customerInfo,
          onNavigateToPaywall = { composeNavigator.navigate(CatArticlesScreen.Paywalls) },
        )
      }

      is SubscriptionManagementUiState.Error -> {
        ErrorContent(message = (uiState as SubscriptionManagementUiState.Error).message)
      }
    }
  }
}

@Composable
private fun SubscriptionContent(
  offering: Offering,
  customerInfo: CustomerInfo,
  onNavigateToPaywall: () -> Unit,
) {
  val scrollState = rememberScrollState()
  val isPremium = customerInfo.entitlements["premium"]?.isActive == true
  val premiumEntitlement = customerInfo.entitlements["premium"]

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(scrollState)
      .padding(16.dp),
  ) {
    // Current Subscription Status
    Text(
      text = "Current Plan",
      fontSize = 20.sp,
      fontWeight = FontWeight.Bold,
      color = CatArticlesTheme.colors.black,
      modifier = Modifier.padding(vertical = 8.dp),
    )

    CurrentSubscriptionCard(
      isPremium = isPremium,
      entitlement = premiumEntitlement,
    )

    Spacer(modifier = Modifier.height(24.dp))

    // Active Subscriptions
    if (isPremium && customerInfo.activeSubscriptions.isNotEmpty()) {
      Text(
        text = "Active Subscriptions",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = CatArticlesTheme.colors.black,
        modifier = Modifier.padding(vertical = 8.dp),
      )

      customerInfo.activeSubscriptions.forEach { subscriptionId ->
        ActiveSubscriptionCard(
          subscriptionId = subscriptionId,
          customerInfo = customerInfo,
        )
        Spacer(modifier = Modifier.height(8.dp))
      }

      Spacer(modifier = Modifier.height(16.dp))
    }

    // Available Packages
    Text(
      text = if (isPremium) "Switch Plans" else "Available Plans",
      fontSize = 20.sp,
      fontWeight = FontWeight.Bold,
      color = CatArticlesTheme.colors.black,
      modifier = Modifier.padding(vertical = 8.dp),
    )

    offering.availablePackages.forEach { pkg ->
      PackageCard(
        pkg = pkg,
        isCurrentPlan = isPremium && pkg.product.id in customerInfo.activeSubscriptions,
        onNavigateToPaywall = onNavigateToPaywall,
      )
      Spacer(modifier = Modifier.height(8.dp))
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Subscription History
    Text(
      text = "Subscription History",
      fontSize = 20.sp,
      fontWeight = FontWeight.Bold,
      color = CatArticlesTheme.colors.black,
      modifier = Modifier.padding(vertical = 8.dp),
    )

    SubscriptionHistoryCard(customerInfo = customerInfo)
  }
}

@Composable
private fun CurrentSubscriptionCard(
  isPremium: Boolean,
  entitlement: com.revenuecat.purchases.EntitlementInfo?,
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(
      containerColor = if (isPremium) {
        Color(0xFFFFD700).copy(alpha = 0.15f)
      } else {
        Color.LightGray.copy(alpha = 0.2f)
      },
    ),
    shape = RoundedCornerShape(16.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(20.dp),
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        if (isPremium) {
          Icon(
            imageVector = Icons.Default.Star,
            contentDescription = "Premium",
            tint = Color(0xFFFFD700),
            modifier = Modifier.size(28.dp),
          )
        }
        Text(
          text = if (isPremium) "Premium Plan" else "Free Plan",
          fontSize = 22.sp,
          fontWeight = FontWeight.Bold,
          color = CatArticlesTheme.colors.black,
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      if (isPremium) {
        Text(
          text = "Status: Active",
          fontSize = 14.sp,
          color = Color(0xFF4CAF50),
          fontWeight = FontWeight.Medium,
        )

        entitlement?.expirationDate?.let { date ->
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Next billing date: ${formatDate(date)}",
            fontSize = 14.sp,
            color = CatArticlesTheme.colors.black,
          )
        }

        entitlement?.productIdentifier?.let { productId ->
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Product: $productId",
            fontSize = 12.sp,
            color = Color.Gray,
          )
        }
      } else {
        Text(
          text = "You are currently on the free plan",
          fontSize = 14.sp,
          color = CatArticlesTheme.colors.black,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "Upgrade to unlock all premium features",
          fontSize = 12.sp,
          color = Color.Gray,
        )
      }
    }
  }
}

@Composable
private fun ActiveSubscriptionCard(
  subscriptionId: String,
  customerInfo: CustomerInfo,
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = Color.White),
    shape = RoundedCornerShape(12.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Icon(
          imageVector = Icons.Default.CheckCircle,
          contentDescription = "Active",
          tint = Color(0xFF4CAF50),
          modifier = Modifier.size(20.dp),
        )
        Text(
          text = subscriptionId,
          fontSize = 16.sp,
          fontWeight = FontWeight.Medium,
          color = CatArticlesTheme.colors.black,
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = "Status: Active",
        fontSize = 12.sp,
        color = Color(0xFF4CAF50),
      )
    }
  }
}

@Composable
private fun PackageCard(
  pkg: Package,
  isCurrentPlan: Boolean,
  onNavigateToPaywall: () -> Unit,
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(
      containerColor = if (isCurrentPlan) {
        Color(0xFF4CAF50).copy(alpha = 0.1f)
      } else {
        Color.White
      },
    ),
    shape = RoundedCornerShape(12.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = pkg.product.title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = CatArticlesTheme.colors.black,
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = pkg.product.description,
            fontSize = 14.sp,
            color = Color.Gray,
          )
        }

        Text(
          text = pkg.product.price.formatted,
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold,
          color = CatArticlesTheme.colors.primary,
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      if (isCurrentPlan) {
        OutlinedButton(
          onClick = { /* Current plan, no action */ },
          modifier = Modifier.fillMaxWidth(),
          enabled = false,
        ) {
          Text(text = "Current Plan")
        }
      } else {
        Button(
          onClick = onNavigateToPaywall,
          modifier = Modifier.fillMaxWidth(),
          colors = ButtonDefaults.buttonColors(
            containerColor = CatArticlesTheme.colors.primary,
          ),
        ) {
          Text(text = "Select Plan", color = Color.White)
        }
      }
    }
  }
}

@Composable
private fun SubscriptionHistoryCard(customerInfo: CustomerInfo) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = Color.White),
    shape = RoundedCornerShape(12.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
    ) {
      // Account creation date
      Text(
        text = "Member since",
        fontSize = 14.sp,
        color = Color.Gray,
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = formatDate(customerInfo.firstSeen),
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        color = CatArticlesTheme.colors.black,
      )

      Spacer(modifier = Modifier.height(12.dp))

      // Original App User ID
      Text(
        text = "User ID",
        fontSize = 14.sp,
        color = Color.Gray,
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = customerInfo.originalAppUserId,
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        color = CatArticlesTheme.colors.black,
      )
    }
  }
}

@Composable
private fun ErrorContent(message: String) {
  Box(
    modifier = Modifier.fillMaxSize(),
    contentAlignment = Alignment.Center,
  ) {
    Text(
      text = message,
      fontSize = 16.sp,
      color = Color.Red,
      textAlign = TextAlign.Center,
      modifier = Modifier.padding(16.dp),
    )
  }
}

private fun formatDate(date: Date?): String {
  if (date == null) return "N/A"
  val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
  return formatter.format(date)
}
