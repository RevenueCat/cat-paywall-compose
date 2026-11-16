import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  `kotlin-dsl`
}

group = "com.revenuecat.articles.paywall.compose.buildlogic"

java {
  sourceCompatibility = JavaVersion.VERSION_17
  targetCompatibility = JavaVersion.VERSION_17
}

// Without this Kotlin targets the JDK running Gradle (21 in CI) while Java targets 17.
kotlin {
  compilerOptions {
    jvmTarget = JvmTarget.JVM_17
  }
}

dependencies {
  compileOnly(libs.android.gradlePlugin)
  compileOnly(libs.kotlin.gradlePlugin)
  compileOnly(libs.compose.compiler.gradlePlugin)
  compileOnly(libs.spotless.gradlePlugin)
}

gradlePlugin {
  plugins {
    register("androidApplicationCompose") {
      id = "revenuecat.android.application.compose"
      implementationClass = "AndroidApplicationComposeConventionPlugin"
    }
    register("androidApplication") {
      id = "revenuecat.android.application"
      implementationClass = "AndroidApplicationConventionPlugin"
    }
    register("androidLibraryCompose") {
      id = "revenuecat.android.library.compose"
      implementationClass = "AndroidLibraryComposeConventionPlugin"
    }
    register("androidLibrary") {
      id = "revenuecat.android.library"
      implementationClass = "AndroidLibraryConventionPlugin"
    }
    register("androidFeature") {
      id = "revenuecat.android.feature"
      implementationClass = "AndroidFeatureConventionPlugin"
    }
    register("androidHilt") {
      id = "revenuecat.android.hilt"
      implementationClass = "AndroidHiltConventionPlugin"
    }
    register("spotless") {
      id = "revenuecat.spotless"
      implementationClass = "SpotlessConventionPlugin"
    }
  }
}
