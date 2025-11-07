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

import android.app.Activity
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.revenuecat.articles.paywall.compose.core.data.BuildConfig
import com.revenuecat.purchases.LogLevel
import com.revenuecat.purchases.PurchaseParams
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesAreCompletedBy
import com.revenuecat.purchases.PurchasesConfiguration
import com.revenuecat.purchases.awaitCustomerInfo
import com.revenuecat.purchases.awaitOfferings
import com.revenuecat.purchases.awaitPurchase
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.time.Duration.Companion.seconds

/**
 * Test Store Purchase Flow Integration Tests with Espresso UI Testing.
 *
 * These tests verify the complete purchase flow including:
 * - Product fetching
 * - Purchase initiation
 * - Test Store dialog interaction
 * - Purchase result verification
 * - Entitlement granting
 *
 * ## How Test Store Works
 *
 * When you call `awaitPurchase()` with Test Store, it shows an AlertDialog with 3 options:
 * 1. "Test valid Purchase" - Simulates successful purchase
 * 2. "Test failed Purchase" - Simulates purchase failure
 * 3. "Cancel" - Simulates user cancellation
 *
 * ## Test Approach
 *
 * These tests use Espresso to:
 * 1. Launch a test Activity
 * 2. Trigger purchase via SDK
 * 3. Interact with Test Store dialog
 * 4. Verify results
 *
 * ## Requirements
 *
 * - Test Store API key configured in local.properties
 * - Device/emulator with internet connection
 * - Offering with identifier "test-offering" configured in RevenueCat dashboard
 *
 * ## Running Tests
 *
 * ```bash
 * ./gradlew :core:data:connectedAndroidTest --tests "*TestStorePurchaseFlowTest"
 * ```
 *
 * @see <a href="https://www.revenuecat.com/docs/test-and-launch/sandbox">Test Store Documentation</a>
 */
@RunWith(AndroidJUnit4::class)
class TestStorePurchaseFlowTest {

  private lateinit var activityScenario: ActivityScenario<TestPurchaseActivity>

  @Before
  fun setup() {
    val context = InstrumentationRegistry.getInstrumentation().targetContext

    println("================================================")
    println("Setting up Test Store Purchase Flow Tests")
    println("================================================")
    println("Package: ${context.packageName}")
    println("API Key: ${BuildConfig.REVENUECAT_TEST_API_KEY.take(20)}...")
    println("================================================")

    // Configure Purchases SDK with Test Store
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

  @After
  fun tearDown() {
    if (::activityScenario.isInitialized) {
      activityScenario.close()
    }
  }

  /**
   * Test 1: Successful Purchase Flow
   *
   * This test verifies the complete successful purchase flow:
   * 1. Fetch offerings from Test Store
   * 2. Select first available package
   * 3. Initiate purchase
   * 4. Click "Test valid Purchase" in dialog
   * 5. Verify entitlements granted
   */
  @Test
  fun testSuccessfulPurchaseFlow() = runBlocking {
    println("Test 1: Successful Purchase Flow")
    println("---------------------------------------")

    try {
      // Step 1: Fetch offerings
      println("Step 1: Fetching offerings...")
      val offerings = Purchases.sharedInstance.awaitOfferings()
      val testOffering = offerings.all["test-offering"]

      assertNotNull("test-offering should exist", testOffering)
      println("Test offering: ${testOffering!!.identifier}")

      assertTrue("Offering should have packages", testOffering.availablePackages.isNotEmpty())
      val packageToPurchase = testOffering.availablePackages.first()
      println("Package to purchase: ${packageToPurchase.identifier}")
      println("Product: ${packageToPurchase.product.id}")

      // Step 2: Get initial customer info
      println("\nStep 2: Getting initial customer info...")
      val initialCustomerInfo = Purchases.sharedInstance.awaitCustomerInfo()
      val initialActiveEntitlements = initialCustomerInfo.entitlements.active.size
      println("Initial active entitlements: $initialActiveEntitlements")

      // Step 3: Launch Activity and initiate purchase
      println("\nStep 3: Launching test activity and initiating purchase...")
      activityScenario = ActivityScenario.launch(TestPurchaseActivity::class.java)

      var purchaseResult: com.revenuecat.purchases.PurchaseResult? = null
      var purchaseError: Throwable? = null

      activityScenario.onActivity { activity ->
        println("Activity launched, starting purchase...")

        // Launch purchase in background
        activity.launchPurchase(packageToPurchase) { result, error ->
          purchaseResult = result
          purchaseError = error
          println("Purchase callback received")
        }
      }

      // Step 4: Wait for Test Store dialog and click "Test valid Purchase"
      println("\nStep 4: Waiting for Test Store dialog...")
      delay(2000) // Give dialog time to appear

      println("Clicking 'Test valid Purchase' button...")
      try {
        onView(withText("Test valid Purchase")).perform(click())
        println("Button clicked successfully")
      } catch (e: Exception) {
        println("Failed to click button: ${e.message}")
        // Try alternative button text
        try {
          onView(withText("Test Valid Purchase")).perform(click())
          println("Alternative button clicked")
        } catch (e2: Exception) {
          println("Alternative button also failed: ${e2.message}")
          throw e
        }
      }

      // Step 5: Wait for purchase to complete
      println("\nStep 5: Waiting for purchase completion...")
      withTimeout(30.seconds) {
        while (purchaseResult == null && purchaseError == null) {
          delay(500)
        }
      }

      // Step 6: Verify results
      println("\nStep 6: Verifying purchase results...")
      assertNotNull("Purchase should complete without error", purchaseResult)
      assertNotNull("Purchase result should not be null", purchaseResult)

      val result = purchaseResult!!
      println("Purchase successful!")
      println("Transaction: ${result.storeTransaction.productIds}")
      println("Customer ID: ${result.customerInfo.originalAppUserId}")

      // Verify entitlements were granted
      val updatedEntitlements = result.customerInfo.entitlements.active.size
      println("Updated active entitlements: $updatedEntitlements")

      assertTrue(
        "Should have active entitlements after purchase",
        result.customerInfo.entitlements.active.isNotEmpty(),
      )

      // Verify transaction details
      assertTrue(
        "Transaction should contain purchased product",
        result.storeTransaction.productIds.contains(packageToPurchase.product.id),
      )

      println("\nTest 1 PASSED: Successfully completed purchase flow!")
    } catch (e: Exception) {
      println("Test 1 FAILED")
      println("Error: ${e.message}")
      e.printStackTrace()
      throw AssertionError("Purchase flow test failed: ${e.message}", e)
    }
    println()
  }

  /**
   * Test 2: Purchase Cancellation Flow
   *
   * Verifies that clicking "Cancel" in Test Store dialog
   * properly cancels the purchase.
   */
  @Test
  fun testPurchaseCancellation() = runBlocking {
    println("Test 2: Purchase Cancellation Flow")
    println("----------------------------------------")

    try {
      // Fetch offerings
      println("Fetching offerings...")
      val offerings = Purchases.sharedInstance.awaitOfferings()
      val testOffering = offerings.all["test-offering"]
      assertNotNull("test-offering should exist", testOffering)

      val packageToPurchase = testOffering!!.availablePackages.first()
      println("Package: ${packageToPurchase.identifier}")

      // Launch Activity
      println("\nLaunching test activity...")
      activityScenario = ActivityScenario.launch(TestPurchaseActivity::class.java)

      var purchaseResult: com.revenuecat.purchases.PurchaseResult? = null
      var purchaseError: Throwable? = null

      activityScenario.onActivity { activity ->
        activity.launchPurchase(packageToPurchase) { result, error ->
          purchaseResult = result
          purchaseError = error
        }
      }

      // Wait for dialog
      println("\nWaiting for Test Store dialog...")
      delay(2000)

      // Click Cancel button
      println("Clicking 'Cancel' button...")
      try {
        onView(withText("Cancel")).perform(click())
        println("Cancel button clicked")
      } catch (e: Exception) {
        println("Failed to click Cancel: ${e.message}")
        throw e
      }

      // Wait for cancellation
      println("\nWaiting for cancellation...")
      withTimeout(15.seconds) {
        while (purchaseResult == null && purchaseError == null) {
          delay(500)
        }
      }

      // Verify cancellation
      println("\nVerifying cancellation...")
      assertNotNull("Should have error after cancellation", purchaseError)
      println("Cancellation error: ${purchaseError?.message}")

      assertTrue(
        "Error should indicate cancellation",
        purchaseError?.message?.contains("cancel", ignoreCase = true) == true ||
          purchaseError?.message?.contains("cancelled", ignoreCase = true) == true,
      )

      println("\nTest 2 PASSED: Purchase cancellation handled correctly!")
    } catch (e: Exception) {
      println("Test 2 FAILED")
      println("Error: ${e.message}")
      e.printStackTrace()
      throw AssertionError("Cancellation test failed: ${e.message}", e)
    }
    println()
  }

  /**
   * Test 3: Failed Purchase Flow
   *
   * Verifies that clicking "Test failed Purchase" properly
   * returns an error.
   */
  @Test
  fun testFailedPurchase() = runBlocking {
    println("Test 3: Failed Purchase Flow")
    println("----------------------------------")

    try {
      // Fetch offerings
      println("Fetching offerings...")
      val offerings = Purchases.sharedInstance.awaitOfferings()
      val testOffering = offerings.all["test-offering"]
      assertNotNull("test-offering should exist", testOffering)

      val packageToPurchase = testOffering!!.availablePackages.first()
      println("Package: ${packageToPurchase.identifier}")

      // Get initial customer info
      val initialCustomerInfo = Purchases.sharedInstance.awaitCustomerInfo()
      val initialEntitlements = initialCustomerInfo.entitlements.active.size
      println("Initial entitlements: $initialEntitlements")

      // Launch Activity
      println("\nLaunching test activity...")
      activityScenario = ActivityScenario.launch(TestPurchaseActivity::class.java)

      var purchaseResult: com.revenuecat.purchases.PurchaseResult? = null
      var purchaseError: Throwable? = null

      activityScenario.onActivity { activity ->
        activity.launchPurchase(packageToPurchase) { result, error ->
          purchaseResult = result
          purchaseError = error
        }
      }

      // Wait for dialog
      println("\nWaiting for Test Store dialog...")
      delay(2000)

      // Click "Test failed Purchase"
      println("Clicking 'Test failed Purchase' button...")
      try {
        onView(withText("Test failed Purchase")).perform(click())
        println("Button clicked")
      } catch (e: Exception) {
        println("Failed to click button: ${e.message}")
        // Try alternatives
        try {
          onView(withText("Test Failed Purchase")).perform(click())
        } catch (e2: Exception) {
          try {
            onView(withText("Test failure")).perform(click())
          } catch (e3: Exception) {
            throw e
          }
        }
      }

      // Wait for failure
      println("\nWaiting for purchase failure...")
      withTimeout(15.seconds) {
        while (purchaseResult == null && purchaseError == null) {
          delay(500)
        }
      }

      // Verify failure
      println("\nVerifying purchase failure...")
      assertNotNull("Should have error after failed purchase", purchaseError)
      println("Purchase error: ${purchaseError?.message}")

      // Verify entitlements unchanged
      val finalCustomerInfo = Purchases.sharedInstance.awaitCustomerInfo()
      val finalEntitlements = finalCustomerInfo.entitlements.active.size
      println("Final entitlements: $finalEntitlements")

      // Entitlements should not increase after failed purchase
      assertFalse(
        "Failed purchase should not grant entitlements",
        finalEntitlements > initialEntitlements,
      )

      println("\nTest 3 PASSED: Failed purchase handled correctly!")
    } catch (e: Exception) {
      println("Test 3 FAILED")
      println("Error: ${e.message}")
      e.printStackTrace()
      throw AssertionError("Failed purchase test failed: ${e.message}", e)
    }
    println()
  }

  /**
   * Test 4: Multiple Packages Purchase
   *
   * Verifies purchasing different packages successfully.
   */
  @Test
  fun testMultiplePackagePurchases() = runBlocking {
    println("Test 4: Multiple Package Purchases")
    println("----------------------------------------")

    try {
      // Fetch offerings
      println("Fetching offerings...")
      val offerings = Purchases.sharedInstance.awaitOfferings()
      val testOffering = offerings.all["test-offering"]
      assertNotNull("test-offering should exist", testOffering)

      val packages = testOffering!!.availablePackages
      println("Available packages: ${packages.size}")

      if (packages.size < 2) {
        println("Not enough packages to test multiple purchases")
        println("Test 4 SKIPPED")
        return@runBlocking
      }

      // Test first package
      println("\n--- Testing Package 1 ---")
      val package1 = packages[0]
      println("Package: ${package1.identifier}")

      activityScenario = ActivityScenario.launch(TestPurchaseActivity::class.java)

      var result1: com.revenuecat.purchases.PurchaseResult? = null

      activityScenario.onActivity { activity ->
        activity.launchPurchase(package1) { result, _ ->
          result1 = result
        }
      }

      delay(2000)
      onView(withText("Test valid Purchase")).perform(click())

      withTimeout(30.seconds) {
        while (result1 == null) delay(500)
      }

      assertNotNull("First purchase should succeed", result1)
      println("Package 1 purchased successfully")

      activityScenario.close()

      // Small delay between purchases
      delay(3000)

      // Test second package
      println("\n--- Testing Package 2 ---")
      val package2 = packages[1]
      println("Package: ${package2.identifier}")

      activityScenario = ActivityScenario.launch(TestPurchaseActivity::class.java)

      var result2: com.revenuecat.purchases.PurchaseResult? = null

      activityScenario.onActivity { activity ->
        activity.launchPurchase(package2) { result, _ ->
          result2 = result
        }
      }

      delay(2000)
      onView(withText("Test valid Purchase")).perform(click())

      withTimeout(30.seconds) {
        while (result2 == null) delay(500)
      }

      assertNotNull("Second purchase should succeed", result2)
      println("Package 2 purchased successfully")

      println("\nTest 4 PASSED: Multiple packages purchased successfully!")
    } catch (e: Exception) {
      println("Test 4 FAILED")
      println("Error: ${e.message}")
      e.printStackTrace()
      throw AssertionError("Multiple package purchase test failed: ${e.message}", e)
    }
    println()
  }
}

/**
 * Simple test Activity for triggering purchases.
 *
 * This Activity provides the required Activity context for
 * RevenueCat's purchase flow.
 */
class TestPurchaseActivity : Activity() {

  private val scope = kotlinx.coroutines.CoroutineScope(
    kotlinx.coroutines.Dispatchers.Main + SupervisorJob(),
  )

  override fun onDestroy() {
    super.onDestroy()
    scope.cancel()
  }

  fun launchPurchase(
    packageToPurchase: com.revenuecat.purchases.Package,
    callback: (com.revenuecat.purchases.PurchaseResult?, Throwable?) -> Unit,
  ) {
    scope.launch {
      try {
        val purchaseParams = PurchaseParams.Builder(this@TestPurchaseActivity, packageToPurchase)
          .build()

        val result = Purchases.sharedInstance.awaitPurchase(purchaseParams)
        callback(result, null)
      } catch (e: Exception) {
        callback(null, e)
      }
    }
  }
}
