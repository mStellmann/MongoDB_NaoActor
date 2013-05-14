package testingFiles

import akka.actor._
import dbActors._
import com.mongodb.casbah.MongoClient
import messages.internalMessages._
import naogateway.value.NaoMessages._
import naogateway.value.NaoMessages.Conversions._
import java.io.{ File, FileInputStream }

object MongoDBActorTest extends App {
  val system = ActorSystem("DBSystem")
  val mongoDB = system.actorOf(Props().withCreator(new MongoDBActor(MongoClient())), name = "mongoDBActor")

  //Save some Random Stuff
    mongoDB ! Save("movs", "nila", 23479812, Map("forw" -> List(8), "tags" -> List("gehen", "stolpern")))
    mongoDB ! Save("movs", "nila", 23479813, Map("back" -> List(8, 3.789)))

  //Save a Command
    val command = Call('ALTextToSpeech, 'say, List("Stehen bleiben!"))
    mongoDB ! SaveCommand("movs", "nila", 23479813, command, Map("back" -> List(8, 3.789), "tags" -> List("a", "b")))

  //Read a File and get its ByteArray
    val file = new File("documents/NaoProjekt-DB_Dokumentation.doc")
    val in = new FileInputStream(file)
    val bytes = new Array[Byte](file.length.toInt)
    in.read(bytes)
    in.close()

  //Save a file
    mongoDB ! SaveFile("filetest", "ALL", 1234353, "Doku.doc", "application/msword", bytes, Map("tags" -> List("complete", "awesome")))

  class FindActor extends Actor {
    
    //Search things
    mongoDB ! SearchData("nila", None, Some(23479815), Some(Map("tags" -> List("gehen", "stolpern"))), self)

    //TODO Get a File
    
    def receive = {
      case x => println("Got: " + x)
    }
  }
  //Find something in the DB
  system.actorOf(Props[FindActor], name = "FindActor")

  Thread.sleep(2000);
  system.shutdown
}