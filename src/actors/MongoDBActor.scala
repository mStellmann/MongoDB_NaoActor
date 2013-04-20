package actors

import akka.actor._
import com.mongodb.casbah.Imports._

package dbMessages {
  case class Save(collection: String, timestamp: Int, naoID: String, content: Map[String, List[String]])

  case class FindInColumnBetweenT(collection: String, naoID: String, column: String, from: Int, to: Int, client: ActorRef)

  case class FoundInColumnBetweenT(docs: List[(String, Option[List[String]])], client: ActorRef)
}

class MongoDBActor extends Actor {

  //ThreadPool!?
  val mongoClient = MongoClient()

  import dbMessages._
  override def receive = {

    case Save(collection, time, naoID, content) => {
      println("DBSave")
      val mongoCollection = mongoClient(collection)(naoID)

      val mongoDbDocument = MongoDBObject("time" -> time)
      val entry = mongoDbDocument ++ content.asDBObject

      mongoCollection += entry
    }

    case FindInColumnBetweenT(collection, naoID, column, from, to, client) => {
      println("DBFind")
      val mongoCollection = mongoClient(collection)(naoID)

      val found = mongoCollection.find(column $lte to $gte from)

      val res = for (
        entry <- found;
        name <- entry.keys;
        if name != "_id" //;
      //TODO Types
      ) yield (name -> entry.getAs[List[String]](name))

      //println(res.mkString("\n"))

      sender ! FoundInColumnBetweenT(res.toList, client)
    }

    case anyThing => println("What's that?: " + anyThing)
  }
}
