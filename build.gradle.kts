plugins {
  kotlin("multiplatform") version "1.4.21"
}

group = "com.lorenzoog"
version = "1.0-SNAPSHOT"

repositories {
  jcenter()
  mavenCentral()
}

kotlin {
  jvm()

  val hostOs = System.getProperty("os.name")
  val isMingwX64 = hostOs.startsWith("Windows")
  when {
    hostOs == "Mac OS X" -> macosX64("native")
    hostOs == "Linux" -> linuxX64("native")
    isMingwX64 -> mingwX64("native")
    else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
  }

  sourceSets {
    val nativeMain by getting
    val nativeTest by getting
  }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile> {
  kotlinOptions.freeCompilerArgs += listOf("-Xopt-in=kotlin.RequiresOptIn")
}

