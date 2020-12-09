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
  jvm {
    java.sourceCompatibility = JavaVersion.VERSION_1_8
    java.targetCompatibility = JavaVersion.VERSION_1_8
  }

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
        entryPoint = "com.lorenzoog.kofl.interpreter.main"
      }
    }

    compilations["main"].cinterops {
      val global by creating {
        defFile = File("$projectDir/runtime/runtime.def")
        includeDirs.headerFilterOnly("$projectDir/runtime")
        compilerOpts("-I/usr/local/include", "-I$projectDir/runtime")
        extraOpts("-libraryPath", "$projectDir/runtime/build")
        extraOpts("-staticLibrary", "libruntime-library.a")
      }
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

    val jvmMain by getting {
      kotlin.srcDir("jvm")
    }

    val nativeMain by getting {
      kotlin.srcDir("native")
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

tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile> {
  kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
  kotlinOptions.jvmTarget = "1.8"
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