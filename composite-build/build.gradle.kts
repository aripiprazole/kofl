plugins {
  `kotlin-dsl`
}

group = "me.devgabi.kofl"
version = "1.0-SNAPSHOT"

repositories {
  mavenCentral()
}

gradlePlugin {
  plugins.register("composite-build") {
    id = "composite-build"
    implementationClass = "me.devgabi.kofl.build.CompositeBuild"
  }
}
