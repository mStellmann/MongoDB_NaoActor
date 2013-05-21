package gui

import akka.actor.{ActorRef, Actor, ActorSelection}
import messages.agentMessages.{RobotSerialNumbers, DatabaseActors, ReceivedRobotSerialNumbers, ReceivedDatabaseActors}
import scala.swing.ComboBox
import scala.swing.event.ButtonClicked
import messages.userMessages.{ReceivedCommand, SearchCommand}

class ControlActor(agent: ActorSelection, gui: SwingGUI) extends Actor {
  // Actors..
  var commandActor: ActorRef = null
  var fileActor: ActorRef = null


  var cBoxTest: ComboBox[String] = new ComboBox(Nil)

  // listener
  gui.listenTo(gui.button_search)
  gui.reactions += {
    case ButtonClicked(b) => {
      commandActor ! SearchCommand(cBoxTest.selection.item, tagList = Option(List("Gespraech", "Uni", "Datenbank", "Test")))

    }

  }


  // Getting the Database Actors
  override def preStart = agent ! DatabaseActors

  def receive = {
    // receiving the SerialNumbers (names) for each robot
    case ReceivedDatabaseActors(cActor, fActor) => {
      commandActor = cActor
      fileActor = fActor
      sender ! RobotSerialNumbers
    }

    // receiving the SerialNumbers and starting the Test
    case ReceivedRobotSerialNumbers(rsnAry) => {
      cBoxTest = new ComboBox(rsnAry.toList)
      gui.panel_cBoxChooseRobot.contents += cBoxTest
      gui.panel_cBoxChooseRobot.revalidate()
      gui.visible = true
    }

    case ReceivedCommand(commandList) =>

      commandList match {
        case Left(callList) => for (elem <- callList) println(elem)
        case Right(errMsg) => println(errMsg)
      }
  }
}

