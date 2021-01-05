@file:Suppress("MemberVisibilityCanBePrivate")

package com.lorenzoog.kofl.build

object Dependencies {
  object Binom {
    const val Version = "0.1.19"

    const val File = "pw.binom.io:file:$Version"
  }

  object Clikt {
    const val Version = "3.1.0"

    const val Clikt = "com.github.ajalt.clikt:clikt:$Version"
  }
}
