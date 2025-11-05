pluginManagement {
  includeBuild("build-logic")
  repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "articles-paywall-compose"
include(":app")
include(":core:model")
include(":core:network")
include(":core:designsystem")
include(":core:navigation")
include(":core:data")
include(":feature:home")
include(":feature:article")
include(":feature:paywalls")
include(":feature:account")
include(":feature:subscriptions")
include(":baselineprofile")
