plugins {
  kotlin("multiplatform")
}

group = "com.lorenzoog"
version = "1.0-SNAPSHOT"

repositories {
  mavenCentral()
}

kotlin {
  jvm()

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
      executable { entryPoint = "com.lorenzoog.kofl.compiler.vm.main" }
    }
  }

  /**
   * Targets configuration omitted.
   * To find out how to configure the targets, please follow the link:
   * https://kotlinlang.org/docs/reference/building-mpp-with-gradle.html#setting-up-targets
   */
  sourceSets {
    val commonMain by getting {
      kotlin.srcDir("common")

      dependencies {
        implementation(project(":frontend"))
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

    val nativeMain by getting {
      kotlin.srcDir("native")
    }
    val nativeTest by getting {
      kotlin.srcDir("nativeTest")
    }

    val jvmMain by getting {
      kotlin.srcDir("jvm")
    }
    val jvmTest by getting {
      kotlin.srcDir("jvmTest")
    }
  }
}