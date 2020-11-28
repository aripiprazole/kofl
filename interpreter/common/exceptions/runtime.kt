package com.lorenzoog.kofl.interpreter.exceptions

import com.lorenzoog.kofl.frontend.KoflException

class KoflRuntimeException(message: String) : KoflException("runtime", message)