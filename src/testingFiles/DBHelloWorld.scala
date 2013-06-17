package testingFiles

import akka.actor._
import dbActors.DBConfigurator
import messages.agentMessages._
import messages.userMessages._
import akka.event.Logging
import com.typesafe.config.ConfigFactory
import naogateway.value.NaoMessages._
import naogateway.value.NaoMessages.Conversions._
import naogateway.value.NaoVisionMessages._
import messages.internalMessages.GetDatabaseNames

object DBHelloWorld extends App {

  val config = ConfigFactory.load()
  val system = ActorSystem("remoting", config.getConfig("remoting").withFallback(config))

  val naoActor = system.actorFor("akka://naogateway@192.168.1.100:2552/user/nila")

  // DBConfigurator startet unser System muss auf dem MongoDB Rechner gestartet werden
  // DONT system.actorOf(Props[DBConfigurator], name = "DBConfigurator")
  // Thread.sleep(1500)

  val agent = system.actorFor("akka://naogateway@192.168.1.112:2554/user/DBConfigurator/DBAgent")
  System.out.println(agent)
  system.actorOf(Props[HelloWorldActor], name = "HelloWorldActor")

  //Aktor besser mit become realisieren!
  class HelloWorldActor extends Actor {
    var commandActor: ActorRef = null
    var fileActor: ActorRef = null

    // Getting the Database Actors
    override def preStart = agent ! DatabaseActors;
    System.out.println("preStart - DatabaseActors")
    naoActor ! Connect
    System.out.println("preStart - Connect")
    Thread.sleep(2000)

    var noresponseA: ActorRef = self

    def receive = {
    	
      //NaoGateWay Aktoren
      case (response: ActorRef, noResponse: ActorRef, vision: ActorRef) => {
        noresponseA = noResponse
      }

      //Unsere Datenbank Aktoren
      case ReceivedDatabaseActors(cActor, fActor) => {
        commandActor = cActor
        fileActor = fActor
        // receiving the SerialNumbers (names) for each robot
        sender ! RobotSerialNumbers
      }

      // receiving the SerialNumbers and starting the Test
      case ReceivedRobotSerialNumbers(rsnAry) => {
        //Speicher Call
        commandActor ! SaveCommand(rsnAry(1), System.currentTimeMillis(), Call('ALTextToSpeech, 'say, List("Stehen bleiben!")), List("Gespraech", "Uni", "Datenbank", "Test"))
        //Suche mit Options
        commandActor ! SearchCommand(Some(rsnAry(1)))
        commandActor ! SearchCommand(None, tagList = Option(List("Gespraech", "Uni", "Datenbank", "Test")))

      }

      //Antwort auf die Suchanfrage
      case ReceivedCommand(commandList) =>
        commandList match {
          case Left(callList) => for (elem <- callList) println(elem)
          case Right(errMsg) => println(errMsg)
        }

    }
  }
}