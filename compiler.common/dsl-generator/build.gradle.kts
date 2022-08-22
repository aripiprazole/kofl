plugins {
  idea
  id("io.gitlab.arturbosch.detekt") version "1.15.0"
  kotlin("multiplatform")
}

group = "me.devgabi"
version = "1.0-SNAPSHOT"

repositories {
  mavenCentral()
  maven("https://jitpack.io")
}

detekt {
  failFast = true // fail build on any finding
  buildUponDefaultConfig = true // preconfigure defaults

  reports {
    html.enabled = false
    xml.enabled = false
    txt.enabled = false
    sarif.enabled = false
  }
}

tasks.detekt {
  jvmTarget = "1.8"
}

kotlin {
  jvm()

  /*
   * Targets configuration omitted.
   * To find out how to configure the targets, please follow the link:
   * https://kotlinlang.org/docs/reference/building-mpp-with-gradle.html#setting-up-targets
   */
  sourceSets {
    val commonMain by getting {
      dependencies {
        implementation(kotlin("stdlib-common"))
      }
    }
    val commonTest by getting {
      dependencies {
        implementation(kotlin("test-common"))
        implementation(kotlin("test-annotations-common"))
      }
    }

    val jvmMain by getting {
      kotlin.srcDir("jvm")

      val kotlinVersion = "1.4.21"

      dependencies {
        implementation("com.squareup:kotlinpoet:1.6.0")
        implementation("org.jetbrains.kotlin:kotlin-compiler:$kotlinVersion")
        implementation("org.jetbrains.kotlin:kotlin-compiler-embeddable:$kotlinVersion")
        implementation("org.jetbrains.kotlin:kotlin-scripting-jvm:$kotlinVersion")
        compileOnly("org.jetbrains.kotlin:kotlin-scripting-jvm-host:$kotlinVersion")
      }
    }
  }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
  kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
}
