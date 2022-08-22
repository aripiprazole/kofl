import me.devgabi.kofl.build.Dependencies

plugins {
  kotlin("multiplatform")
}

group = "me.devgabi.kofl"
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
        entryPoint = "me.devgabi.kofl.compiler.vm.main"
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
        implementation(Dependencies.Clikt.Clikt)
        api(Dependencies.Binom.File)
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
