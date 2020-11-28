package com.lorenzoog.kofl.interpreter.typing

fun KoflType.isAssignableBy(another: KoflType?): Boolean {
  return this == KoflType.Primitive.Any || this == another
}

fun KoflType.isNumber(): Boolean {
  return this == KoflType.Primitive.Int || this == KoflType.Primitive.Double
}