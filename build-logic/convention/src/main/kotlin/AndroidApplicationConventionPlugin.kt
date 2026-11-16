import com.android.build.api.dsl.ApplicationExtension
import com.revenuecat.configureKotlinAndroid
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidApplicationConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      // AGP 9 compiles Kotlin itself (built-in Kotlin); applying org.jetbrains.kotlin.android is an error.
      pluginManager.apply("com.android.application")

      extensions.configure<ApplicationExtension> {
        configureKotlinAndroid(this)
        defaultConfig.targetSdk = 37
      }
    }
  }
}
