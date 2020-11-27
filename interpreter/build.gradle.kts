plugins {
  kotlin("multiplatform")
}

group = "com.lorenzoog.kofl"
version = "1.0-SNAPSHOT"

repositories {
  mavenCentral()
  maven("https://repo.binom.pw/releases")
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
        api("pw.binom.io:file:0.1.19")
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
  }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile> {
  kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
  kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompileCommon> {
  kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
}