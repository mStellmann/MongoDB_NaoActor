package gui

import scala.swing.ComboBox
import naogateway.value.NaoMessages.Call

/**
 * Model-Klasse der DatabaseSwingGUI
 */
class Model {
  var cBox_Robots: ComboBox[String] = new ComboBox(Nil)
  var cBox_Commands: ComboBox[String] = new ComboBox(Nil)
  var commandHistoryMap: Map[Int, Call] = Map[Int, Call]()
}
