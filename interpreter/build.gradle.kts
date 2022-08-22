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
      executable { entryPoint = "me.devgabi.kofl.interpreter.main" }
    }
  }

  sourceSets {
    val commonMain by getting {
      kotlin.srcDir("common")

      dependencies {
        api(Dependencies.Binom.File)
        implementation(project(":frontend"))
        implementation(project(":compiler.common"))
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

    val jvmMain by getting {
      kotlin.srcDir("jvm")
    }
    val jvmTest by getting {
      kotlin.srcDir("jvmTest")
    }

    val nativeMain by getting {
      kotlin.srcDir("native")

      dependencies {
        implementation(Dependencies.Clikt.Clikt)
      }
    }
    val nativeTest by getting {
      kotlin.srcDir("nativeTest")
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
