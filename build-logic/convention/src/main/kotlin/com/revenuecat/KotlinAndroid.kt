package com.revenuecat

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinBaseExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

/**
 * Configure base Kotlin with Android options
 */
internal fun Project.configureKotlinAndroid(
  commonExtension: CommonExtension<*, *, *, *, *, *>,
) {
  commonExtension.apply {
    compileSdk = 36

    defaultConfig {
      minSdk = 24
    }

    compileOptions {
      sourceCompatibility = JavaVersion.VERSION_17
      targetCompatibility = JavaVersion.VERSION_17
    }

    lint {
      abortOnError = false
    }

    configureKotlin<KotlinAndroidProjectExtension>()
  }
}

/**
 * Configure base Kotlin options
 */
private inline fun <reified T : KotlinBaseExtension> Project.configureKotlin() = configure<T> {
  // Treat all Kotlin warnings as errors (disabled by default)
  // Override by setting warningsAsErrors=true in your ~/.gradle/gradle.properties
  val warningsAsErrors = providers.gradleProperty("warningsAsErrors").map {
    it.toBoolean()
  }.orElse(false)
  when (this) {
    is KotlinAndroidProjectExtension -> compilerOptions
    is KotlinJvmProjectExtension -> compilerOptions
    else -> TODO("Unsupported project extension $this ${T::class}")
  }.apply {
    jvmTarget = JvmTarget.JVM_17
    allWarningsAsErrors = warningsAsErrors
    freeCompilerArgs.addAll(
      "-Xcontext-receivers",
      "-opt-in=kotlin.RequiresOptIn",
      "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
      "-opt-in=com.google.accompanist.pager.ExperimentalPagerApi",
      "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
      "-opt-in=androidx.lifecycle.compose.ExperimentalLifecycleComposeApi",
      "-opt-in=androidx.compose.animation.ExperimentalSharedTransitionApi",
    )
  }
}