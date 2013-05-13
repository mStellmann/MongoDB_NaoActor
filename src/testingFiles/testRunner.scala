package testingFiles

import akka.actor._
import dbActors.DBConfigurator
import messages.agentMessages.DatabaseActors

object testRunner extends App {

  // Create the Akka system
  val system = ActorSystem("DBSystem")
  system.actorOf(Props[DBConfigurator], name = "DBConfig")

  val agent = system.actorFor("DBAgent")

  class TestActor extends Actor {
    override def preStart = agent ! DatabaseActors

    def receive = {
      case ReceivedDa
    }
  }

}