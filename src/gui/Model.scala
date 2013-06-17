package gui

import scala.swing.ComboBox
import naogateway.value.NaoMessages.Call
import akka.actor.ActorRef

/**
 * Model-Klasse der DatabaseSwingGUI
 */
class Model {
  // ----- Aktorreferenzen der Datenbankaktoren -----
  var commandActor: ActorRef = null
  var fileActor: ActorRef = null

  // ----- Aktorreferenzen der NAOGateway-Aktoren -----
  var noResponse: ActorRef = null
  var response: ActorRef = null

  // ----- ComboBox für die Roboterwahl -----
  var cBox_Robots: ComboBox[String] = new ComboBox(Nil)
  // ----- ComboBox für die Befehlswahl -----
  var cBox_Commands: ComboBox[String] = new ComboBox(Nil)
  // ----- Abbildung des Hashwerts der Daten aus der Tabelle auf den abzusendenten Befehls -----
  var commandHistoryMap: Map[Int, Call] = Map[Int, Call]()
}
