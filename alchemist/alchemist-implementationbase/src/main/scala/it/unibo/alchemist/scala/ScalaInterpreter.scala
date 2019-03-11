/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.scala

import scala.reflect.runtime.currentMirror
import scala.tools.reflect.ToolBox

object ScalaInterpreter {
  import scala.reflect.runtime.currentMirror
  import scala.tools.reflect.ToolBox
  import java.io.File

  private[this] val toolbox = currentMirror.mkToolBox()

  def apply[A](string: String): A = toolbox.eval(toolbox.parse(string)).asInstanceOf[A]

}
