package testingFiles

import akka.actor._
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons
import java.io.File

object testRunner extends App {
  // Create an Akka system
  val system = ActorSystem("DBSystem")

  // create the result listener, which will print the result and shutdown the system
  val dbActor1 = system.actorOf(Props[DBActor], name = "dbact")

  // start the calculation
  dbActor1 ! StringMsg("key1","Hilfe -> Halllllo")
  dbActor1 ! "WHAHAHAHA"

}
class DBActor extends Actor {

  // Connect to default - localhost, 27017
  val mongoClient = MongoClient()
  // mongoConn: com.mongodb.casbah.MongoConnection

  val mongoDB = mongoClient("casbah_test")("test_data")

  val commandObj = MongoDBObject ( )


  override def receive = {
    case StringMsg(key,value) => {
      val mongoObj = MongoDBObject(key -> value)
      mongoDB += mongoObj
      println("Message in DB eingetragen")

      //        println(mongoDB.find("key1"))
      for (x <- mongoDB.find("key1" $exists true)) println(x)
    }
    case _ => println("Irgendwas ist passiert")
  }
}

case class StringMsg(key: String, value: String)

case class SaveCommand(timestamp: Int, naoID: String, commands: List[(String,String)])
case class SaveAudioFile(timestamp: Int, naoID: String, audioFile: File)
