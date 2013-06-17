package gui

import akka.actor.{ActorRef, Actor}
import messages.agentMessages.{RobotSerialNumbers, DatabaseActors, ReceivedRobotSerialNumbers, ReceivedDatabaseActors}
import scala.swing.{Dialog, ComboBox}
import scala.swing.event.{TableRowsSelected, ButtonClicked}
import messages.userMessages.{SaveCommand, ReceivedCommand, SearchCommand}

import naogateway.value.NaoMessages._
import naogateway.value.NaoMessages.Conversions._
import naogateway.value.NaoVisionMessages._

import naogateway.value.NaoMessages.Call
import java.util.Date
import naogateway.value.NaoMessages
import messages.internalMessages.{DatabaseNames, GetDatabaseNames}
import javax.swing.event.TableModelEvent

/**
 * Controller fuer die DatabaseSwingGUI
 * @param agent Aktorreferenz des DBAgent
 * @param robotActor Aktorreferenz des anzusprechenden Roboters
 * @param gui die zu steuernde GUI
 */
class ControlActor(agent: ActorRef, robotActor: ActorRef, gui: DatabaseSwingGUI, model: Model) extends Actor {
  // ----- Aktorreferenzen der Datenbankaktoren -----
  var commandActor: ActorRef = null
  var fileActor: ActorRef = null

  // ----- Aktorreferenzen der NAOGateway-Aktoren -----
  var noResponse: ActorRef = null
  var response: ActorRef = null

  // ----- GUI-Listener -----
  gui.listenTo(gui.button_search, gui.button_sendToRobot, gui.cBox_starttime, gui.cBox_endtime, gui.table_commandList.selection)

  // ----- GUI-Reactions -----
  gui.reactions += {
    // ----- Suchanfrage starten -----
    case ButtonClicked(gui.button_search) => {
      val rsnr = if (model.cBox_Robots.selection.item != "ALL") Some(model.cBox_Robots.selection.item) else None
      val command = if (model.cBox_Commands.selection.item != "All Commands") Some(model.cBox_Commands.selection.item) else None
      val tStart = if (gui.cBox_starttime.selected) Some(gui.ftxtField_starttime.peer.getValue.asInstanceOf[Date].getTime) else None
      val tEnd = if (gui.cBox_endtime.selected) Some(gui.ftxtField_endtime.peer.getValue.asInstanceOf[Date].getTime) else None
      val tagText = gui.textfield_tags.peer.getText.trim.toLowerCase.replaceAll(" ", "")
      val tags = if (!tagText.isEmpty) Some(tagText.split( """,|;""").toList) else None

      gui.button_sendToRobot.enabled = false

      commandActor ! SearchCommand(rsnr, command, tStart, tEnd, tags)
    }

    // ----- Befehl zum Roboter senden -----
    case ButtonClicked(gui.button_sendToRobot) => gui.table_commandList.selection.rows.isEmpty match {
      case false => for (elem <- gui.table_commandList.selection.rows) {
        val hc = (gui.table_commandList(elem, 0), gui.table_commandList(elem, 1), gui.table_commandList(elem, 2), gui.table_commandList(elem, 3)).hashCode
        noResponse ! model.commandHistoryMap(hc)
      }
      case true => Dialog.showMessage(gui.button_sendToRobot, "Keine gültige Auswahl in der Tabelle!", title = "Auswahl-Fehler")
    }

    // ----- Textfeld des Startzeitpunktes aktivieren -----
    case ButtonClicked(gui.cBox_starttime) => gui.cBox_starttime.selected match {
      case true => gui.ftxtField_starttime.enabled = true; gui.ftxtField_starttime.revalidate()
      case false => gui.ftxtField_starttime.enabled = false; gui.ftxtField_starttime.revalidate()
    }

    // ----- Textfeld des Endzeitpunktes aktivieren -----
    case ButtonClicked(gui.cBox_endtime) => gui.cBox_endtime.selected match {
      case true => gui.ftxtField_endtime.enabled = true; gui.ftxtField_endtime.revalidate()
      case false => gui.ftxtField_endtime.enabled = false; gui.ftxtField_endtime.revalidate()
    }

    // ----- Tabellenselektion -----
    case TableRowsSelected(gui.table_commandList, range, false) => gui.table_commandList.selection.rows.isEmpty match {
      case true => gui.button_sendToRobot.enabled = false
      case false => gui.button_sendToRobot.enabled = true
    }
  }

  /**
   * Vor dem Start des Aktors werden die Datenbankaktoren und die Naogateway-Aktoren abgefragt
   */
  override def preStart() {
    agent ! DatabaseActors
    robotActor ! Connect
  }

  /**
   * Receive-Funktion zum empfangen der Daten des Aktors
   */
  def receive = {
    // ----- Empfangen der Datenbankaktoren -----
    case ReceivedDatabaseActors(cActor, fActor) => {
      commandActor = cActor
      fileActor = fActor
      // ----- gespeicherte Befehlskategorien abfragen -----
      commandActor ! GetDatabaseNames
      // ----- gespeicherte Roboter (Seriennummern | Namen) abfragen -----
      sender ! RobotSerialNumbers

    }

    // ----- Empfangen der Befehlskategorien -----
    case DatabaseNames(databases) => {
      model.cBox_Commands = new ComboBox[String]("All Commands" :: databases.filterNot(_ == "local"))
      gui.panel_cBoxCommands.contents += model.cBox_Commands
      gui.panel_cBoxCommands.revalidate()
    }

    // ----- Empfangen der Roboter (Seriennummern | Namen) -----
    case ReceivedRobotSerialNumbers(rsnAry) => {
      model.cBox_Robots = new ComboBox(rsnAry.toList.reverse)
      gui.panel_cBoxChooseRobot.contents += model.cBox_Robots
      gui.panel_cBoxChooseRobot.revalidate()
      gui.visible = true
    }

    // ----- Empfangen der Naogateway-Aktoren -----
    case (response: ActorRef, noResponse: ActorRef, vision: ActorRef) => {
      this.noResponse = noResponse
      this.response = response
    }

    // ----- Empfagen des  Suchergebnisses -----
    case ReceivedCommand(commandList) =>
      commandList match {
        case Left(callList) => {
          // ----- Tabelle wird geleert -----
          gui.tableModel.getDataVector.removeAllElements()

          val mutableMap = scala.collection.mutable.HashMap[Int, Call]()
          for (elem <- callList) {
            // ----- Abbildung Hashwert -> Call -----
            val ary: Array[AnyRef] = Array(elem._1.module + " (" + elem._1.method + ")", NaoMessages.toString(elem._1.parameters(0)), new Date(elem._2).toString, elem._3.foldLeft("")((res, text) => res + text + ", ")).asInstanceOf[Array[AnyRef]]
            mutableMap.put((ary(0), ary(1), ary(2), ary(3)).hashCode, elem._1)
            gui.tableModel.addRow(ary)
          }
          model.commandHistoryMap = mutableMap.toMap
        }
        // ----- Falls keine Daten gefunden wurden -----
        case Right(errMsg) => {
          gui.tableModel.getDataVector.removeAllElements()
          gui.table_commandList.revalidate()
        }
      }
  }
}

