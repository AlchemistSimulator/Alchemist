package it.unibo.alchemist.scala

import scala.reflect.runtime.currentMirror
import scala.tools.reflect.ToolBox

object ScalaInterpreter {
  import scala.reflect.runtime.currentMirror
  import scala.tools.reflect.ToolBox
  import java.io.File

  def apply[A](string: String): A = {
    val toolbox = currentMirror.mkToolBox()
    val tree = toolbox.parse(string)
    toolbox.eval(tree).asInstanceOf[A]
  }
}