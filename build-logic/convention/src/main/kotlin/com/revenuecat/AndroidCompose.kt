package com.revenuecat

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension

/**
 * Configure Compose-specific options
 */
internal fun Project.configureAndroidCompose(
  commonExtension: CommonExtension,
) {
  pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

  commonExtension.buildFeatures.compose = true

  // Strong skipping is the compiler default since Kotlin 2.0.20 and the flag is deprecated.
  extensions.configure<ComposeCompilerGradlePluginExtension> {
    reportsDestination = layout.buildDirectory.dir("compose_compiler")
  }
}
