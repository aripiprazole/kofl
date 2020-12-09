package com.lorenzoog.kofl.frontend

fun dump(stmts: List<Stmt>) {
  println("simple ast dump:")
  println(stmts.joinToString(";\n"))
}
