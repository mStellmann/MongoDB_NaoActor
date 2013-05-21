package gui

import akka.actor._
import dbActors.DBConfigurator


object DBGui extends App {
  // Create the Akka system
  val gui = new SwingGUI

  val system = ActorSystem("DBSystem")
  system.actorOf(Props[DBConfigurator], name = "DBConfigurator")

  val agent = system.actorSelection("/user/DBConfigurator/DBAgent")
  val model = system.actorOf(Props().withCreator(new ControlActor(agent, gui)), name = "GUIModel")

}

