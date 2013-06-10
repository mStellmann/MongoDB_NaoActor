package gui

import akka.actor._
import dbActors.DBConfigurator
import com.typesafe.config.ConfigFactory


object DBGui extends App {
  // Create the Akka system
  val gui = new SwingGUI
  val config = ConfigFactory.load()
  val system = ActorSystem("remoting", config.getConfig("remoting").withFallback(config))

  // theoretisch über config
  val naoActor = system.actorFor("akka://naogateway@192.168.1.100:2552/user/nila")
  //    val naoActor = system.actorFor("akka://naogateway@192.168.1.100:2550/user/hanna")


  system.actorOf(Props[DBConfigurator], name = "DBConfigurator")

  val agent = system.actorFor("akka://remoting/user/DBConfigurator/DBAgent")
  val model = system.actorOf(Props().withCreator(new ControlActor(agent, naoActor, gui)), name = "GUIActor")
}

