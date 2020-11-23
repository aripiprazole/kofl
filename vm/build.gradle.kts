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
      executable {
        entryPoint = "com.lorenzoog.kofl.vm.main"
      }
    }
  }
  /* Targets configuration omitted.
  *  To find out how to configure the targets, please follow the link:
  *  https://kotlinlang.org/docs/reference/building-mpp-with-gradle.html#setting-up-targets */

  sourceSets {
    val linuxX64Main by getting {
      kotlin.srcDir("linuxX64")

      dependencies {
        implementation(project(":interpreter"))
      }
    }

    val commonMain by getting {
      kotlin.srcDir("common")

      dependencies {
        implementation(kotlin("stdlib-common"))
      }
    }

    val commonTest by getting {
      kotlin.srcDir("test")

      dependencies {
        implementation(kotlin("test-common"))
        implementation(kotlin("test-annotations-common"))
      }
    }

    all {
      languageSettings.enableLanguageFeature("InlineClasses")
    }
  }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile> {
  kotlinOptions.freeCompilerArgs += listOf("-Xopt-in=kotlin.RequiresOptIn")
}
