plugins {
  `kotlin-dsl`
}

group = "com.lorenzoog.kofl"
version = "1.0-SNAPSHOT"

repositories {
  mavenCentral()
}

gradlePlugin {
  plugins.register("composite-build") {
    id = "composite-build"
    implementationClass = "com.lorenzoog.kofl.build.CompositeBuild"
  }
}
