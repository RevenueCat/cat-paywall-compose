import com.android.build.api.dsl.LibraryExtension
import com.revenuecat.configureKotlinAndroid
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidLibraryConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      pluginManager.apply("com.android.library")

      extensions.configure<LibraryExtension> {
        configureKotlinAndroid(this)
        // AGP 9 removed defaultConfig.targetSdk from the library DSL; a library's targetSdk only
        // ever applied to its test APK and to lint, which is what these two replace.
        testOptions.targetSdk = 37
        lint.targetSdk = 37
      }
    }
  }
}
