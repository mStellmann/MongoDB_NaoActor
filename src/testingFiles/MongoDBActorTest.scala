package testingFiles

import dbActors._
import akka.actor._
import akka.event.Logging
import messages.internalMessages._
import com.mongodb.casbah.MongoClient
import java.io.{ File, FileInputStream }
import naogateway.value.NaoMessages._
import naogateway.value.NaoMessages.Conversions._
import naogateway.value.NaoVisionMessages._

/**
 * interne TestKlasse die direkt den MongoDBActor anspricht
 * Beispiel fuer File saving und suchen
 */
object MongoDBActorTest extends App {
  val system = ActorSystem("DBSystem")
  val mongoDB = system.actorOf(Props().withCreator(new MongoDBActor(MongoClient(), Array("Nila", "Hanna"))), name = "mongoDBActor")
  //Find something in the DB
  val findActor = system.actorOf(Props[FindActor], name = "FindActor")

  //Save some Random Stuff
  mongoDB ! Save("movs", "Nila", 23479812, Map("forw" -> List(8), "tags" -> List("gehen", "stolpern")))
  mongoDB ! Save("movs", "Nila", 23479813, Map("back" -> List(8, 3.789)))

  //Read a File and get its ByteArray
  val file = new File("documents/NaoProjekt-DB_Dokumentation.doc")
  val in = new FileInputStream(file)
  val bytes = new Array[Byte](file.length.toInt)
  in.read(bytes)
  in.close()

  //Save a file
  mongoDB ! SaveFile("filetest", "Nila", 1234353, "Doku.doc", "application/msword", bytes, Map("tags" -> List("complete", "awesome")))

  //Let the DB work
  Thread.sleep(1000)
  //Find something
  findActor ! "Find"

  class FindActor extends Actor {

    def receive = {

      case "Find" => {
        //Search things
        mongoDB ! SearchData(Some("ALTextToSpeech"), Some("Nila"), None, Some(23479815), Some(Map("tags" -> List("gehen"))), self)
        mongoDB ! SearchData(None,None,None,None, None, self)
        mongoDB ! SearchFile(Some("filetest"), None, None, None, None, None, Some(Map("tags" -> List("complete", "awesome"))), self)
        mongoDB ! SearchFile(None, None, None, None, Some("application/msword"), Some("Doku.doc"), None, self)

      }

      case x => println("Got: " + x)
    }
  }
  Thread.sleep(2000);
  system.shutdown
}