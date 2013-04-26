package actors

import akka.actor._
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.gridfs.Imports._


package dbMessages {

case class Save(collection: String, timestamp: Int, naoID: String, content: Map[String, List[String]])

case class FindInColumnBetweenT(collection: String, naoID: String, column: String, from: Int, to: Int, client: ActorRef)

case class FoundInColumnBetweenT(docs: List[Map[String, AnyRef]], client: ActorRef)

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

      val found = (mongoCollection.find(column $lte to $gte from)).toList

      val res = for (entry <- found) yield {
        for (kvTupel <- entry if (kvTupel._1 != "_id")) yield kvTupel
      }
      //      sender ! FoundInColumnBetweenT(res, client) TODO
    }

      // case FindDataBetweenTimestamps

    case anyThing => println("What's that?: " + anyThing)
  }
}
