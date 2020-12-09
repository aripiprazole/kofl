plugins {
  kotlin("multiplatform")
}

group = "com.lorenzoog.kofl"
version = "1.0-SNAPSHOT"

repositories {
  mavenCentral()
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
      executable {
        entryPoint = "com.lorenzoog.kofl.vm.main"
      }
    }

    compilations["main"].cinterops {
      val runtime by creating {
        defFile = File("$projectDir/runtime/runtime.def")
        includeDirs.headerFilterOnly("$projectDir/runtime")

        compilerOpts("-I/usr/local/include", "-I$projectDir/runtime")
        extraOpts("-libraryPath", "$projectDir/runtime/build")
        extraOpts("-staticLibrary", "libruntime.a")
      }
    }
  }

  /**
   * Targets configuration omitted.
   * To find out how to configure the targets, please follow the link:
   * https://kotlinlang.org/docs/reference/building-mpp-with-gradle.html#setting-up-targets
   */
  sourceSets {
    val nativeMain by getting {
      kotlin.srcDir("native")

      dependencies {
        implementation(project(":frontend"))
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
  kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
}
