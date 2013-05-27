package gui

import akka.actor.{ActorRef, Actor, ActorSelection}
import messages.agentMessages.{RobotSerialNumbers, DatabaseActors, ReceivedRobotSerialNumbers, ReceivedDatabaseActors}
import scala.swing.ComboBox
import scala.swing.event.{SelectionChanged, ListSelectionChanged, ButtonClicked}
import messages.userMessages.{ReceivedCommand, SearchCommand}

import naogateway.value.NaoMessages._
import naogateway.value.NaoMessages.Conversions._
import naogateway.value.NaoVisionMessages._

import naogateway.value.NaoMessages.Call
import java.util.Date

class ControlActor(agent: ActorRef, naoActor: ActorRef, gui: SwingGUI) extends Actor {
  // Actors..
  var commandActor: ActorRef = null
  var fileActor: ActorRef = null

  var noResponse: ActorRef = null
  var response: ActorRef = null

  var cBox_Robots: ComboBox[String] = new ComboBox(Nil)
  var commandHistoryMap: Map[String, Call] = Map[String, Call]()

  // listener
  gui.listenTo(gui.button_search, gui.button_sendToNao, gui.cBox_starttime, gui.cBox_endtime, gui.list_commandList.selection)

  gui.reactions += {
    case ButtonClicked(gui.button_search) => {
      val rsnr = if (cBox_Robots.selection.item == "ALL") None else Some(cBox_Robots.selection.item)
      val command = if (gui.cBox_commands.selection.item == "All Commands") None else Some(gui.cBox_commands.selection.item)
      //      val tStart = if(gui.cBox_starttime.selected)


      //      commandActor ! SearchCommand(cBox_Robots.selection.item)
    }

    case ButtonClicked(gui.button_sendToNao) => {
      for (elem <- gui.list_commandList.selection.items)
        noResponse ! commandHistoryMap(elem.asInstanceOf[String])


    }

    case ButtonClicked(gui.cBox_starttime) => gui.cBox_starttime.selected match {
      case true => gui.ftxtField_starttime.enabled = true; gui.ftxtField_starttime.revalidate();
      case false => gui.ftxtField_starttime.enabled = false; gui.ftxtField_starttime.revalidate();
    }

    case ButtonClicked(gui.cBox_endtime) => gui.cBox_endtime.selected match {
      case true => gui.ftxtField_endtime.enabled = true; gui.ftxtField_endtime.revalidate();
      case false => gui.ftxtField_endtime.enabled = false; gui.ftxtField_endtime.revalidate();
    }

    case SelectionChanged(gui.list_commandList) => gui.list_commandList.selection.items.isEmpty match {
      case true => gui.button_sendToNao.enabled = false
      case false => gui.button_sendToNao.enabled = true
    }

  }


  // Getting the Database Actors
  override def preStart = {
    agent ! DatabaseActors
    naoActor ! Connect
  }

  def receive = {
    // receiving the SerialNumbers (names) for each robot
    case ReceivedDatabaseActors(cActor, fActor) => {
      commandActor = cActor
      fileActor = fActor
      sender ! RobotSerialNumbers
    }

    // receiving the SerialNumbers and starting the Test
    case ReceivedRobotSerialNumbers(rsnAry) => {
      cBox_Robots = new ComboBox(rsnAry.toList)
      gui.panel_cBoxChooseRobot.contents += cBox_Robots
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
          val mutableMap = scala.collection.mutable.HashMap[String, Call]()
          gui.list_commandList.listData = for (elem <- callList) yield {
            val text = elem._1 + " " + new Date(elem._2) + " " + elem._3
            mutableMap.put(text, elem._1)

            text
          }
          commandHistoryMap = mutableMap.toMap
        }
        case Right(errMsg) => println(errMsg)
      }
  }
}

