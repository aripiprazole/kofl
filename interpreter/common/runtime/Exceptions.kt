package com.lorenzoog.kofl.interpreter.runtime

@PublishedApi
internal class ReturnException(val value: KoflObject) : Throwable()