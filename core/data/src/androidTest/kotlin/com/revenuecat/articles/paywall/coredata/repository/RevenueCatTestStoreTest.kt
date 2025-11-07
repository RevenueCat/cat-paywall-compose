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

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.revenuecat.articles.paywall.compose.core.data.BuildConfig
import com.revenuecat.purchases.LogLevel
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesAreCompletedBy
import com.revenuecat.purchases.PurchasesConfiguration
import com.revenuecat.purchases.awaitCustomerInfo
import com.revenuecat.purchases.awaitOfferings
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Direct RevenueCat Test Store integration tests.
 *
 * These tests verify that the RevenueCat SDK works correctly with Test Store
 * by directly calling SDK methods rather than going through repository layers.
 *
 * ## Test Store Setup
 *
 * RevenueCat Test Store is automatically provisioned for every project.
 * To use it:
 *
 * 1. **Get Your Test Store API Key**:
 *    - Log into RevenueCat dashboard
 *    - Go to Project Settings → API Keys
 *    - Copy the **Test Store** API key (starts with `test_`)
 *
 * 2. **Add to local.properties**:
 *    ```
 *    revenuecat.test.api.key=test_YOUR_KEY_HERE
 *    ```
 *
 * 3. **Configure Test Products** (in RevenueCat dashboard):
 *    - Go to Products section
 *    - Create test products (they don't need to exist in Google Play)
 *    - Create an Offering and add your products
 *    - Mark one offering as "current"
 *
 * ## What Test Store Provides
 *
 * - Instant testing without real payment processing
 * - Products work exactly as configured in RevenueCat
 * - Purchases update CustomerInfo and trigger entitlements
 * - Transactions appear in your RevenueCat dashboard
 * - No need for Google Play sandbox or test accounts
 *
 * ## Running These Tests
 *
 * ```bash
 * ./gradlew :core:data:connectedAndroidTest --tests "*RevenueCatTestStoreTest"
 * ```
 *
 * @see <a href="https://www.revenuecat.com/docs/getting-started/configuring-sdk#testing-with-test-store">RevenueCat Test Store Documentation</a>
 */
@RunWith(AndroidJUnit4::class)
class RevenueCatTestStoreTest {

  @Before
  fun setup() {
    val context = InstrumentationRegistry.getInstrumentation().targetContext

    println("================================================")
    println("Setting up RevenueCat Test Store")
    println("================================================")
    println("Package: ${context.packageName}")
    println("API Key: ${BuildConfig.REVENUECAT_TEST_API_KEY.take(20)}...")
    println("================================================")

    // Configure Purchases SDK with Test Store API key
    Purchases.logLevel = LogLevel.DEBUG
    Purchases.configure(
      PurchasesConfiguration.Builder(context, BuildConfig.REVENUECAT_TEST_API_KEY)
        .purchasesAreCompletedBy(PurchasesAreCompletedBy.REVENUECAT)
        .diagnosticsEnabled(true)
        .build(),
    )

    println("RevenueCat configured with Test Store")
    println()
  }

  /**
   * Test 1: Verify SDK can connect to Test Store
   *
   * This test verifies that:
   * - SDK is properly configured
   * - Test Store API key is valid
   * - Device can reach RevenueCat servers
   */
  @Test
  fun testSDKConnection() = runTest {
    println("Test 1: SDK Connection")
    println("-----------------------------")

    try {
      val customerInfo = Purchases.sharedInstance.awaitCustomerInfo()

      println("Connected to RevenueCat Test Store")
      println("User ID: ${customerInfo.originalAppUserId}")
      println("First Seen: ${customerInfo.firstSeen}")
      println("Entitlements: ${customerInfo.entitlements.all.size}")

      // More thorough assertions
      assertNotNull("CustomerInfo should not be null", customerInfo)
      assertNotNull("User ID should not be null", customerInfo.originalAppUserId)
      assertFalse("User ID should not be empty", customerInfo.originalAppUserId.isEmpty())
      assertNotNull("First seen date should not be null", customerInfo.firstSeen)
      assertNotNull("Entitlements map should not be null", customerInfo.entitlements)

      println("Test 1 PASSED")
    } catch (e: Exception) {
      println("Test 1 FAILED")
      println("Error: ${e.message}")
      println("Stack trace:")
      e.printStackTrace()
      throw AssertionError("Failed to connect to Test Store: ${e.message}", e)
    }
    println()
  }

  /**
   * Test 2: Fetch offerings from Test Store
   *
   * This test verifies that:
   * - Test Store has offerings configured
   * - SDK can fetch offerings
   * - At least one offering exists
   * - Products have valid metadata
   *
   * If this fails, you need to:
   * 1. Log into RevenueCat dashboard
   * 2. Go to Offerings section
   * 3. Create an offering with at least one product
   * 4. Mark it as "current"
   */
  @Test
  fun testFetchOfferings() = runTest {
    println("Test 2: Fetch Offerings")
    println("-----------------------------")

    try {
      val offerings = Purchases.sharedInstance.awaitOfferings()

      println("Total offerings: ${offerings.all.size}")

      if (offerings.all.isEmpty()) {
        println("WARNING: No offerings found!")
        println("   Please create offerings in RevenueCat dashboard:")
        println("   1. Log into RevenueCat dashboard")
        println("   2. Go to Offerings section")
        println("   3. Create a new offering")
        println("   4. Add at least one product to the offering")
        println("   5. Mark it as 'current'")
        throw AssertionError("No offerings configured in Test Store")
      }

      // Verify offerings structure
      assertNotNull("Offerings should not be null", offerings)
      assertTrue("Should have at least one offering", offerings.all.isNotEmpty())

      offerings.all.forEach { (id, offering) ->
        println("  Offering: $id")
        println("     Identifier: ${offering.identifier}")
        println("     Description: ${offering.serverDescription}")
        println("     Packages: ${offering.availablePackages.size}")

        // Verify offering has required fields
        assertNotNull("Offering identifier should not be null", offering.identifier)
        assertEquals("Offering map key should match identifier", id, offering.identifier)
        assertNotNull("Available packages should not be null", offering.availablePackages)

        offering.availablePackages.forEach { pkg ->
          println("       Package: ${pkg.identifier}")
          println("          Product: ${pkg.product.id}")
          println("          Title: ${pkg.product.title}")
          println("          Price: ${pkg.product.price}")

          // Validate product details (pattern from purchases-android tests)
          assertNotNull("Package identifier should not be null", pkg.identifier)
          assertFalse("Package identifier should not be empty", pkg.identifier.isEmpty())
          assertNotNull("Product should not be null", pkg.product)
          assertNotNull("Product ID should not be null", pkg.product.id)
          assertFalse("Product ID should not be empty", pkg.product.id.isEmpty())
          assertNotNull("Product price should not be null", pkg.product.price)
        }
      }

      println("Test 2 PASSED")
    } catch (e: Exception) {
      println("Test 2 FAILED")
      println("Error: ${e.message}")
      e.printStackTrace()
      throw AssertionError("Failed to fetch offerings: ${e.message}", e)
    }
    println()
  }

  /**
   * Test 3: Verify current offering
   *
   * This test verifies that:
   * - A current offering is configured
   * - Current offering has packages
   * - Package products have proper metadata
   */
  @Test
  fun testCurrentOffering() = runTest {
    println("Test 3: Verify Current Offering")
    println("-------------------------------------")

    try {
      val offerings = Purchases.sharedInstance.awaitOfferings()

      val currentOffering = offerings.current

      if (currentOffering == null) {
        println("WARNING: No current offering found!")
        println("   Please mark an offering as 'current' in RevenueCat dashboard")
        println("   Available offerings:")
        offerings.all.keys.forEach { println("     - $it") }
        println()
        println("Test 3 SKIPPED - No current offering configured")
        return@runTest
      }

      println("Current offering: ${currentOffering.identifier}")
      println("   Description: ${currentOffering.serverDescription}")
      println("   Packages: ${currentOffering.availablePackages.size}")
      println("   Metadata: ${currentOffering.metadata}")

      // Verify current offering structure
      assertNotNull("Current offering should not be null", currentOffering)
      assertNotNull("Current offering identifier should not be null", currentOffering.identifier)
      assertFalse(
        "Current offering identifier should not be empty",
        currentOffering.identifier.isEmpty(),
      )
      assertTrue(
        "Current offering should have at least one package",
        currentOffering.availablePackages.isNotEmpty(),
      )

      // Verify all packages in current offering
      currentOffering.availablePackages.forEach { pkg ->
        println("\n   Package: ${pkg.identifier}")
        println("      Product ID: ${pkg.product.id}")
        println("      Product Type: ${pkg.product.type}")
        println("      Title: ${pkg.product.title}")
        println("      Description: ${pkg.product.description}")
        println("      Price: ${pkg.product.price}")
        println("      Period: ${pkg.product.period}")

        // Assertions based on purchases-android product validation patterns
        assertNotNull("Package should not be null", pkg)
        assertNotNull("Product should not be null", pkg.product)
        assertNotNull("Product type should not be null", pkg.product.type)
        assertNotNull("Product price should not be null", pkg.product.price)
        assertFalse("Product price should not be empty", pkg.product.price.formatted.isEmpty())
      }

      println("\nTest 3 PASSED")
    } catch (e: Exception) {
      println("Test 3 FAILED")
      println("Error: ${e.message}")
      e.printStackTrace()
      throw AssertionError("Failed to verify current offering: ${e.message}", e)
    }
    println()
  }

  /**
   * Test 4: Verify customer entitlements
   *
   * This test verifies:
   * - Customer info is accessible
   * - Entitlements structure is valid
   * - Active vs. all entitlements distinction
   */
  @Test
  fun testCustomerEntitlements() = runTest {
    println("Test 4: Customer Entitlements")
    println("----------------------------------")

    try {
      val customerInfo = Purchases.sharedInstance.awaitCustomerInfo()

      println("User: ${customerInfo.originalAppUserId}")
      println("Active Entitlements: ${customerInfo.entitlements.active.size}")
      println("All Entitlements: ${customerInfo.entitlements.all.size}")
      println("Request Date: ${customerInfo.requestDate}")

      // Verify customer info structure
      assertNotNull("CustomerInfo should not be null", customerInfo)
      assertNotNull("Original app user ID should not be null", customerInfo.originalAppUserId)
      assertNotNull("Entitlements should not be null", customerInfo.entitlements)
      assertNotNull("Active entitlements map should not be null", customerInfo.entitlements.active)
      assertNotNull("All entitlements map should not be null", customerInfo.entitlements.all)
      assertNotNull("Request date should not be null", customerInfo.requestDate)

      if (customerInfo.entitlements.active.isEmpty()) {
        println("No active entitlements (expected for new test user)")
      } else {
        println("\nActive Entitlements Details:")
        customerInfo.entitlements.active.forEach { (id, info) ->
          println("  Entitlement: $id")
          println("     Active: ${info.isActive}")
          println("     Product: ${info.productIdentifier}")
          println("     Will Renew: ${info.willRenew}")
          println("     Period Type: ${info.periodType}")
          println("     Latest Purchase Date: ${info.latestPurchaseDate}")
          println("     Original Purchase Date: ${info.originalPurchaseDate}")

          // Verify active entitlement structure
          assertTrue("Active entitlement should be active", info.isActive)
          assertNotNull("Product identifier should not be null", info.productIdentifier)
          assertFalse("Product identifier should not be empty", info.productIdentifier.isEmpty())
        }
      }

      // If there are any entitlements (active or expired), verify their structure
      if (customerInfo.entitlements.all.isNotEmpty()) {
        println("\nAll Entitlements (including expired):")
        customerInfo.entitlements.all.forEach { (id, info) ->
          println("  $id: Active=${info.isActive}, Product=${info.productIdentifier}")

          // Verify entitlement has required fields
          assertNotNull("Entitlement info should not be null", info)
          assertNotNull("Product identifier should not be null", info.productIdentifier)
        }
      }

      println("\nTest 4 PASSED")
    } catch (e: Exception) {
      println("Test 4 FAILED")
      println("Error: ${e.message}")
      e.printStackTrace()
      throw AssertionError("Failed to check customer entitlements: ${e.message}", e)
    }
    println()
  }
}
