package testingFiles

import akka.actor._
import dbActors.DBConfigurator
import messages.agentMessages._
import messages.userMessages._
import naogateway.value.NaoMessages._
import naogateway.value.NaoMessages.Conversions._
import naogateway.value.NaoVisionMessages._

import akka.actor.ActorSystem
import akka.actor.Actor
import akka.event.Logging
import com.typesafe.config.ConfigFactory
import akka.actor.ActorRef
import akka.actor.Props
import naogateway.value.NaoMessages._
import naogateway.value.NaoMessages.Conversions._
import naogateway.value.NaoVisionMessages._

object DBHelloWorld extends App {


  val config = ConfigFactory.load()
  val system = ActorSystem("remoting", config.getConfig("remoting").withFallback(config))

  val naoActor = system.actorFor("akka://naogateway@192.168.1.100:2550/user/hanna")

  // Create the Akka system
  // val system = ActorSystem("DBSystem")
  system.actorOf(Props[DBConfigurator], name = "DBConfigurator")

  val agent = system.actorSelection("/user/DBConfigurator/DBAgent")

  Thread.sleep(1500)

  system.actorOf(Props[HelloWorldActor], name = "HelloWorldActor")

  Thread.sleep(15000)

  //system.shutdown()

  class HelloWorldActor extends Actor {
    var commandActor: ActorRef = null
    var fileActor: ActorRef = null

    // Getting the Database Actors
    override def preStart = agent ! DatabaseActors;
    naoActor ! Connect
    Thread.sleep(2000)
    var noresponseA: ActorRef = self

    def receive = {
      // receiving the SerialNumbers (names) for each robot
      case ReceivedDatabaseActors(cActor, fActor) => {

        commandActor = cActor
        fileActor = fActor
        sender ! RobotSerialNumbers
      }

      // receiving the SerialNumbers and starting the Test
      case ReceivedRobotSerialNumbers(rsnAry) => {

        commandActor ! SaveCommand(rsnAry(1), System.currentTimeMillis(), Call('ALTextToSpeech, 'say, List("Stehen bleiben!")), List("Gespraech", "Uni", "Datenbank", "Test"))

        commandActor ! SearchCommand(rsnAry(1), commandList = Option(List("TextToSpeech")))
        commandActor ! SearchCommand(rsnAry(1), tagList = Option(List("Gespraech", "Uni", "Datenbank", "Test")))
      }

      case (response: ActorRef, noResponse: ActorRef, vision: ActorRef) => {
        noresponseA = noResponse
      }


      case ReceivedCommand(commandList) =>

        commandList match {
          //          case Left(callList) => for (elem <- callList) noresponseA ! elem
          case Right(errMsg) => println(errMsg)
        }
    }
  }

  // TODO - "Hello World - Get Started"
}