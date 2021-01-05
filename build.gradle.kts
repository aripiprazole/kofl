plugins {
  kotlin("multiplatform") version "1.4.30-M1"
  id("org.jlleitschuh.gradle.ktlint") version "9.4.1"
  id("composite-build")
}

group = "com.lorenzoog"
version = "1.0-SNAPSHOT"

repositories {
  jcenter()
  mavenCentral()
}

allprojects {
  apply(plugin = "composite-build")
  apply(plugin = "org.jlleitschuh.gradle.ktlint")

  ktlint {
    android.set(true)
  }
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
