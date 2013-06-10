package gui

import akka.actor.{ActorRef, Actor}
import messages.agentMessages.{RobotSerialNumbers, DatabaseActors, ReceivedRobotSerialNumbers, ReceivedDatabaseActors}
import scala.swing.ComboBox
import scala.swing.event.{TableRowsSelected, ButtonClicked}
import messages.userMessages.{SaveCommand, ReceivedCommand, SearchCommand}

import naogateway.value.NaoMessages._
import naogateway.value.NaoMessages.Conversions._
import naogateway.value.NaoVisionMessages._

import naogateway.value.NaoMessages.Call
import java.util.Date
import naogateway.value.NaoMessages
import messages.internalMessages.{DatabaseNames, GetDatabaseNames}

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

  // listener
  gui.listenTo(gui.button_search, gui.button_sendToNao, gui.cBox_starttime, gui.cBox_endtime, gui.table_commandList.selection)

  gui.reactions += {
    case ButtonClicked(gui.button_search) => {
      val rsnr = if (model.cBox_Robots.selection.item != "ALL") Some(model.cBox_Robots.selection.item) else None
      val command = if (model.cBox_Commands.selection.item != "All Commands") Some(model.cBox_Commands.selection.item) else None
      val tStart = if (gui.cBox_starttime.selected) Some(gui.ftxtField_starttime.peer.getValue.asInstanceOf[Date].getTime) else None
      val tEnd = if (gui.cBox_endtime.selected) Some(gui.ftxtField_endtime.peer.getValue.asInstanceOf[Date].getTime) else None
      val tagText = gui.textfield_tags.peer.getText.trim.toLowerCase.replaceAll(" ", "")
      val tags = if (!tagText.isEmpty) Some(tagText.split( """,|;""").toList) else None

      gui.button_sendToNao.enabled = false

      commandActor ! SearchCommand(rsnr, command, tStart, tEnd, tags)
    }

    case ButtonClicked(gui.button_sendToNao) => {
      for (elem <- gui.table_commandList.selection.rows) {
        val hc = (gui.table_commandList(elem, 0), gui.table_commandList(elem, 1), gui.table_commandList(elem, 2), gui.table_commandList(elem, 3)).hashCode
        noResponse ! model.commandHistoryMap(hc)
      }
    }

    case ButtonClicked(gui.cBox_starttime) => gui.cBox_starttime.selected match {
      case true => gui.ftxtField_starttime.enabled = true; gui.ftxtField_starttime.revalidate()
      case false => gui.ftxtField_starttime.enabled = false; gui.ftxtField_starttime.revalidate()
    }

    case ButtonClicked(gui.cBox_endtime) => gui.cBox_endtime.selected match {
      case true => gui.ftxtField_endtime.enabled = true; gui.ftxtField_endtime.revalidate()
      case false => gui.ftxtField_endtime.enabled = false; gui.ftxtField_endtime.revalidate()
    }

    case TableRowsSelected(gui.table_commandList, range, false) => gui.table_commandList.selection.rows.isEmpty match {
      case true => gui.button_sendToNao.enabled = false
      case false => gui.button_sendToNao.enabled = true
    }

  }


  // Getting the Database Actors
  override def preStart() {
    agent ! DatabaseActors
    robotActor ! Connect
  }

  def receive = {
    // receiving the SerialNumbers (names) for each robot
    case ReceivedDatabaseActors(cActor, fActor) => {
      commandActor = cActor
      fileActor = fActor

      // testingExamples
      //            commandActor ! SaveCommand("Nila", System.currentTimeMillis(), Call('ALTextToSpeech, 'say, List("Hallo")), List("Gespraech", "Uni", "Datenbank", "Test"))
      //            commandActor ! SaveCommand("Nila", (System.currentTimeMillis() + 86400000), Call('ALTextToSpeech, 'say, List("Mein Name ist Nila")), List("Pistole", "Terminator"))
      //            commandActor ! SaveCommand("Nila", (System.currentTimeMillis() + 86400000 * 2), Call('ALTextToSpeech, 'say, List("Ich bin ein Roboter")), List("Angst"))
      //            commandActor ! SaveCommand("Nila", (System.currentTimeMillis() + 86400000 * 3), Call('ALTextToSpeech, 'say, List("peng peng!")), List("Kampf", "Action"))
      //
      //            commandActor ! SaveCommand ("Nila", (System.currentTimeMillis() - 1000),  Call('ALLeds, 'randomEyes, List(3.0f)), List("Augen", "Random"))
      //            commandActor ! SaveCommand ("Nila", (System.currentTimeMillis() - 500),  Call('ALLeds, 'rotateEyes, List(0x00FF0000, 1.0f, 3.0f)), List("Augen", "Rot"))
      //
      //            commandActor ! SaveCommand ("Nila", (System.currentTimeMillis()- 2000 ),  Call('ALRobotPosture, 'goToPosture, List("Stand", 1.0f)), List("aufstehen"))
      //            commandActor ! SaveCommand ("Nila", (System.currentTimeMillis() - 1500),  Call('ALRobotPosture, 'goToPosture, List("Sit", 1.0f)), List("hinsetzen"))
      //
      //            commandActor ! SaveCommand ("Nila", (System.currentTimeMillis()+ 86400000 * 5),  Call('ALMotion, 'setStiffnesses, List("Body", 0.0f)), List("loose"))
      //            commandActor ! SaveCommand ("Nila", (System.currentTimeMillis()+ 86400000 * 6),  Call('ALMotion, 'setStiffnesses, List("Body", 1.0f)), List("stiff"))

      commandActor ! GetDatabaseNames
      sender ! RobotSerialNumbers

    }

    case DatabaseNames(databases) => {
      model.cBox_Commands = new ComboBox[String]("All Commands" :: databases.filterNot(_ == "local"))
      gui.panel_cBoxCommands.contents += model.cBox_Commands
      gui.panel_cBoxCommands.revalidate()
    }

    // receiving the SerialNumbers and starting the GUI
    case ReceivedRobotSerialNumbers(rsnAry) => {
      model.cBox_Robots = new ComboBox(rsnAry.toList.reverse)
      gui.panel_cBoxChooseRobot.contents += model.cBox_Robots
      gui.panel_cBoxChooseRobot.revalidate()
      gui.visible = true
    }

    case (response: ActorRef, noResponse: ActorRef, vision: ActorRef) => {
      this.noResponse = noResponse
      this.response = response
    }

    case ReceivedCommand(commandList) =>
      commandList match {
        case Left(callList) => {
          gui.tableModel.getDataVector.removeAllElements()
          val mutableMap = scala.collection.mutable.HashMap[Int, Call]()
          for (elem <- callList) {
            val ary: Array[AnyRef] = Array(elem._1.module + " (" + elem._1.method + ")", NaoMessages.toString(elem._1.parameters(0)), new Date(elem._2).toString, elem._3.foldLeft("")((res, text) => res + text + ", ")).asInstanceOf[Array[AnyRef]]
            mutableMap.put((ary(0), ary(1), ary(2), ary(3)).hashCode, elem._1)
            gui.tableModel.addRow(ary)
          }
          model.commandHistoryMap = mutableMap.toMap
        }

        case Right(errMsg) => {
          gui.tableModel.getDataVector.removeAllElements()
          gui.table_commandList.revalidate()
        }
      }
  }
}

