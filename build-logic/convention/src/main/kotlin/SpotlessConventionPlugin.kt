import com.diffplug.gradle.spotless.SpotlessExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class SpotlessConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      pluginManager.apply("com.diffplug.spotless")

      extensions.configure<SpotlessExtension> {
        kotlin {
          target("**/*.kt")
          targetExclude("**/build/**/*.kt")
          ktlint()
            .editorConfigOverride(
              mapOf(
                "indent_size" to 2,
                "continuation_indent_size" to 2,
                // Spotless never passed the removed userData("android") to ktlint 0.50, so this code base
                // was formatted with ktlint's former default style. ktlint 1.x defaults to ktlint_official.
                "ktlint_code_style" to "intellij_idea",
                // Composables are PascalCase by convention.
                "ktlint_function_naming_ignore_when_annotated_with" to "Composable",
              ),
            )
          licenseHeaderFile(rootProject.file("$rootDir/spotless/copyright.kt"))
        }
        format("kts") {
          target("**/*.kts")
          targetExclude("**/build/**/*.kts")
          // Look for the first line that doesn't have a block comment (assumed to be the license)
          licenseHeaderFile(rootProject.file("spotless/copyright.kts"), "(^(?![\\/ ]\\*).*$)")
        }
        format("xml") {
          target("**/*.xml")
          targetExclude("**/build/**/*.xml")
          // Look for the first XML tag that isn't a comment (<!--) or the xml declaration (<?xml)
          licenseHeaderFile(rootProject.file("spotless/copyright.xml"), "(<[^!?])")
        }
      }
    }
  }
}
