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