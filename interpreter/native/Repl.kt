@file:OptIn(ExperimentalUnsignedTypes::class)

package me.devgabi.kofl.interpreter

import kotlinx.cinterop.MemScope
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.staticCFunction
import platform.posix.ECHO
import platform.posix.ICANON
import platform.posix.ICRNL
import platform.posix.IEXTEN
import platform.posix.ISIG
import platform.posix.STDIN_FILENO
import platform.posix.TCSAFLUSH
import platform.posix.atexit
import platform.posix.exit
import platform.posix.getenv
import platform.posix.system
import platform.posix.tcgetattr
import platform.posix.tcsetattr
import platform.posix.termios
import pw.binom.io.file.File
import pw.binom.io.file.read
import pw.binom.io.readText
import pw.binom.io.utf8Reader

// save copy of original termios to apply again to terminal
// when the user quit the program
val orig_termios = nativeHeap.alloc<termios>()

@OptIn(ExperimentalUnsignedTypes::class)
internal fun MemScope.enterCBreakMode() {
  // will trigger [quitRawMode] when program exits
  atexit(staticCFunction(::quitRawMode))

  val raw = alloc<termios>()

  // put all [STDIN_FILENO] attrs into [raw]
  tcgetattr(STDIN_FILENO, raw.ptr)

  raw.reinterpret<termios>().apply {
    c_iflag = c_iflag and ICRNL.toUInt().inv()
    c_lflag = c_lflag and (
      ECHO.toUInt()
        or ICANON.toUInt()
        or IEXTEN.toUInt()
        or ISIG.toUInt()
      ).inv()
  }

  tcsetattr(STDIN_FILENO, TCSAFLUSH, raw.ptr)
}

internal fun quitRawMode() {
  tcsetattr(STDIN_FILENO, TCSAFLUSH, orig_termios.ptr)
}

internal fun clearScreen() {
  if (getenv("TERM") != null) {
    // clear screen if it is in a terminal
    system("clear")
  }
}

// TODO: handle arrow keys
@OptIn(ExperimentalUnsignedTypes::class)
internal fun startRepl(logger: Logger, debug: Boolean, path: String): Unit = memScoped {
  val eval = createEval(Interpreter(debug, repl = true, logger))

  File(path).read().utf8Reader().readText().also { code ->
    eval(code)

    if (debug) {
      logger.trace("STDLIB")
      logger.trace(code)
      logger.trace("END STDLIB")
    }
  }

  clearScreen()

  if (debug) {
    logger.println()
    logger.println()
  }

  logger.println("KOFL's repl. Type :quit to exit the program. Enjoy it 😃")
  logger.println()

  while (true) {
    logger.print("kofl> ")

    when (val line = readLine().orEmpty()) {
      ":quit" -> exit(0)
      ":clear" -> clearScreen()
      else ->
        try {
          eval(line) // interpret and run the provided code in the line
        } catch (error: Throwable) {
          logger.handleError(error)
        }
    }

    logger.println()
  }
}
