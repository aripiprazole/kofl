package com.lorenzoog.kofl.interpreter.runtime

class ReturnException(val value: KoflObject) : Throwable()