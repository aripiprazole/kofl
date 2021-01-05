package com.lorenzoog.kofl.frontend

fun String?.escape(): String {
  return toString().replace("\\", "\\\\").replace("\n", "\\n").replace("\b", "\\b").replace("\r", "\\r")
}

fun String?.unescape(): String {
  return toString().replace("\\n", "\n").replace("\\b", "\b").replace("\\r", "\r")
}