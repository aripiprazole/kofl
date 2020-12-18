plugins {
  kotlin("multiplatform")
}

group = "com.lorenzoog"
version = "1.0-SNAPSHOT"

repositories {
  mavenCentral()
  maven("https://repo.binom.pw/releases")
}

kotlin {
  val hostOs = System.getProperty("os.name")
  val isMingwX64 = hostOs.startsWith("Windows")
  val nativeTarget = when {
    hostOs == "Mac OS X" -> macosX64("native")
    hostOs == "Linux" -> linuxX64("native")
    isMingwX64 -> mingwX64("native")
    else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
  }

  nativeTarget.apply {
    binaries {
      executable("koflc") {
        entryPoint = "com.lorenzoog.kofl.compiler.vm.main"
      }
    }
  }

  sourceSets {
    val commonMain by getting {
      kotlin.srcDir("common")

      dependencies {
        implementation(kotlin("stdlib-common"))
        implementation(project(":compiler.common"))
        implementation(project(":frontend"))
        implementation("com.github.ajalt.clikt:clikt:3.1.0")
        api("pw.binom.io:file:0.1.19")
      }
    }
    val commonTest by getting {
      kotlin.srcDir("test")

      dependencies {
        implementation(kotlin("test-common"))
        implementation(kotlin("test-annotations-common"))
      }
    }

    val nativeMain by getting {
      kotlin.srcDir("native")
    }
    val nativeTest by getting {
      kotlin.srcDir("nativeTest")
    }
  }
}