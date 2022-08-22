package me.devgabi.kofl.interpreter.runtime

@PublishedApi
internal class ReturnException(val value: KoflObject) : Throwable()
