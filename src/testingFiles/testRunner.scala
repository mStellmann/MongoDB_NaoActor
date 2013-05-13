package testingFiles

import akka.actor._
import dbActors.DBConfigurator
import messages.agentMessages.{ReceivedRobotSerialNumbers, RobotSerialNumbers, ReceivedDatabaseActors, DatabaseActors}

object testRunner extends App {

  // Create the Akka system
  val system = ActorSystem("DBSystem")
  system.actorOf(Props[DBConfigurator], name = "DBConfig")

  val agent = system.actorFor("DBAgent")

  system.actorOf(Props[TestActor], name = "TestActor")

  Thread.sleep(2000)
  system.shutdown()


  class TestActor extends Actor {
    override def preStart = agent ! DatabaseActors

    def receive = {
      case ReceivedDatabaseActors(cActor, fActor) => {
        println("Actors: " + cActor + " | " + fActor)
        sender ! RobotSerialNumbers
      }
      case ReceivedRobotSerialNumbers(rsnAry) => println(rsnAry)
    }
  }

}