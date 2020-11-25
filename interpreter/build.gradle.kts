plugins {
  kotlin("multiplatform")
}

group = "com.lorenzoog.kofl"
version = "1.0-SNAPSHOT"

repositories {
  mavenCentral()
}

kotlin {
  linuxX64("linuxX64") {
    binaries {
      executable { entryPoint = "com.lorenzoog.kofl.interpreter.main" }
    }
  }

  mingwX64("windowsX64") {
    binaries {
      executable { entryPoint = "com.lorenzoog.kofl.interpreter.main" }
    }
  }

  sourceSets {
    val commonMain by getting {
      kotlin.srcDir("common")

      dependencies {
        implementation(project(":frontend"))
        implementation(kotlin("stdlib-common"))
      }
    }

    val linuxX64Main by getting {
      kotlin.srcDir("linuxX64")
    }

    val windowsX64Main by getting {
      kotlin.srcDir("windowsX64")
    }

    val commonTest by getting {
      kotlin.srcDir("test")

      dependencies {
        implementation(kotlin("test-common"))
        implementation(kotlin("test-annotations-common"))
      }
    }

    all {
      languageSettings.useExperimentalAnnotation("com.lorenzoog.interpreter.KoflResolverInternals")
    }
  }
}

