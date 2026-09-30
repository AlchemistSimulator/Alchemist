/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.scala

import java.util.Objects
import javax.script.ScriptEngineManager
import scala.util.{Success, Try}

object ScalaInterpreter:
  private val engine = Objects.requireNonNull(
    new ScriptEngineManager().getEngineByName("scala"),
    "No Scala JSR-223 engine found: is scala3-repl on the runtime classpath?"
  )

  // one global lock, since the REPL state is shared; use an engine pool if parallel loading gets slow
  def apply[A](code: String): A = engine.synchronized:
    // The Scala 3 engine reports compilation errors as missing classes or null results: wrapping tells them apart
    Try(engine.eval(s"Some[Any]({\n$code\n})")) match
      case Success(Some(value)) => value.asInstanceOf[A]
      case result =>
        throw new IllegalArgumentException(s"Unable to evaluate Scala code: $code", result.failed.getOrElse(null))
