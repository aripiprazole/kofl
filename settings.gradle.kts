rootProject.name = "kofl"
enableFeaturePreview("GRADLE_METADATA")

pluginManagement {
  repositories {
    google()
    jcenter()
    gradlePluginPortal()
  }
}

includeBuild("composite-build")

include("interpreter")
include("frontend")
include("compiler.common")
include("compiler.vm")
include("compiler.common:dsl-generator")
