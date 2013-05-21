package testingFiles

import akka.actor._
import dbActors._
import com.mongodb.casbah.MongoClient
import messages.internalMessages._
import naogateway.value.NaoMessages._
import naogateway.value.NaoMessages.Conversions._
import java.io.{ File, FileInputStream }

object MongoDBActorTest extends App {

  import akka.actor.ActorSystem
  import akka.actor.Actor
  import akka.event.Logging
  import com.typesafe.config.ConfigFactory
  import akka.actor.ActorRef
  import akka.actor.Props
  import naogateway.value.NaoMessages._
  import naogateway.value.NaoMessages.Conversions._
  import naogateway.value.NaoVisionMessages._
  val config = ConfigFactory.load()
  val system = ActorSystem("remoting", config.getConfig("remoting").withFallback(config))

  val naoActor = system.actorFor("akka://naogateway@192.168.1.100:2550/user/hanna")

  //  val system = ActorSystem("DBSystem")
  val mongoDB = system.actorOf(Props().withCreator(new MongoDBActor(MongoClient())), name = "mongoDBActor")
  //Find something in the DB
  val findActor = system.actorOf(Props[FindActor], name = "FindActor")

  //Save some Random Stuff
  mongoDB ! Save("movs", "nila", 23479812, Map("forw" -> List(8), "tags" -> List("gehen", "stolpern")))
  mongoDB ! Save("movs", "nila", 23479813, Map("back" -> List(8, 3.789)))

  //Save a Command
  //  val command = Call('ALTextToSpeech, 'say, List("Stehen bleiben!", true, 1, 1F, 3.asInstanceOf[Byte], Seq[Any](1, 2F)))
  val command = Call('ALTextToSpeech, 'say, List("b"))
  println("Saved Command: " + command);
  mongoDB ! SaveCommand("movs", "nila", 23479813, command, Map("back" -> List(8, 3.789), "tags" -> List("a", "b")))

  //Read a File and get its ByteArray
  val file = new File("documents/NaoProjekt-DB_Dokumentation.doc")
  val in = new FileInputStream(file)
  val bytes = new Array[Byte](file.length.toInt)
  in.read(bytes)
  in.close()

  //Save a file
  mongoDB ! SaveFile("filetest", "ALL", 1234353, "Doku.doc", "application/msword", bytes, Map("tags" -> List("complete", "awesome")))

  //Let DB work
  Thread.sleep(1000)

  //Find something
  findActor ! "Find"

  class FindActor extends Actor {

    override def preStart = naoActor ! Connect

    var noresponseA: ActorRef = self
    //    Thread.sleep(2000)

    def receive = {
      case x: Call => {
        println(" Got  Command: " + x)
        //TODO von DB to Nao
        if (noresponseA != self) {
          println("Call Nao " + x)
          noresponseA ! x
        }
      }
      case (response: ActorRef, noResponse: ActorRef, vision: ActorRef) => {
        println("Got Actor Infos")
        noresponseA = noResponse
      }
      case "Find" => {
        //Search things
        mongoDB ! SearchData("nila", None, Some(23479815), Some(Map("tags" -> List("a", "b"))), self)

        //TODO Get a File
      }
      case x => println("Got: " + x)
    }
  }
  Thread.sleep(2000);
  system.shutdown
}