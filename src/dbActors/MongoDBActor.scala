package dbActors

import akka.actor.Actor
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.gridfs.Imports._
import messages.internalMessages.{ Save, SaveCommand, SaveFile }
import messages.internalMessages.{ SearchData, SearchFile }
import messages.internalMessages.{ ReceivedData, ReceivedFile }
import scala.util.{ Try, Success, Failure }
import com.mongodb.casbah.commons.MongoDBObject

// TODO - Max
/**
 * This actor works as an adapter for the connection and communication with a MongoDB-Database.
 * It is a basic interface for saving and finding objects in the database.
 */
class MongoDBActor(mongoDBClient: MongoClient) extends Actor {

  def receive = {
    case Save(db, robotSerialNumber, timestamp, content) => {
      val mongoDBDoc = MongoDBObject("time" -> List(timestamp))
      val contentWithSupportedTypes = convertMapToSaveTypes(content)
      val dBEntry = mongoDBDoc ++ contentWithSupportedTypes.asDBObject

      val mongoCollection = mongoDBClient(db)(robotSerialNumber)
      save(mongoCollection, dBEntry)
    }

    //TODO In DBAccessCommand
    case SaveCommand(db, robotSerialNumber, timestamp, command, content) => {

      //TODO callArgs saveTypes see NaoMessages toString
      val mongoDBDoc = MongoDBObject("time" -> List(timestamp), "callModule" -> List(command.module.toString),
        "callMethod" -> List(command.method.toString), "callArgs" -> List(command.stringParameters))

      val contentWithSupportedTypes = convertMapToSaveTypes(content)
      val dBEntry = mongoDBDoc ++ contentWithSupportedTypes.asDBObject

      val mongoCollection = mongoDBClient(db)(robotSerialNumber)
      save(mongoCollection, dBEntry)
    }

    case SaveFile(db, robotSerialNumber, timestamp, filename, filetyp, file, content) => {

      val mongoDBDoc = MongoDBObject("time" -> List(timestamp), "robotSerialNumber" -> List(robotSerialNumber))
      val contentWithSupportedTypes = convertMapToSaveTypes(content)
      val metaData = mongoDBDoc ++ contentWithSupportedTypes.asDBObject

      val mongoDB: MongoDB = mongoDBClient.getDB(db)
      val gridfs = GridFS(mongoDB)

      val gfsFile = gridfs.createFile(file)
      gfsFile.filename = filename
      gfsFile.contentType = filetyp
      gfsFile.metaData = metaData
      //gfsFile.aliases.+=("")
      gfsFile.save
    }
    // TODO - collection als Option
    case SearchData(robotSerialNumber, timestampStart, timestampEnd, content, origin) => {
      //TODO search in all dbs
      println(mongoDBClient.getDatabaseNames)
      val db = "movs"

      val mongoCollection = mongoDBClient(db)(robotSerialNumber)

      val start = timestampStart.getOrElse(0L)
      val end = timestampEnd.getOrElse(Long.MaxValue)
      //TODO search forcontent
      //      val tags = content.getOrElse(Map())
      //      for((k,v) <- tags)yield MongoDBObject(k $elemMatch v(0))
      //      val elemmatch = MongoDBObject("tags" -> MongoDBList("gehen")) 
      //      val search2: MongoDBObject = { { ("time" ) } }
      //      val ser = MongoDBObject("$elemMatch" -> elemmatch)
      //      println(ser)

      val search = { { ("time" $gte start $lte end) } }

      val finalSearchRequest = search //++ tags.asDBObject
      println("Searching For:" + finalSearchRequest)

      val found = mongoCollection.find(finalSearchRequest)

      //val findAll = mongoCollection.find(new BasicDBObject)

      val docsFound = (for {
        document <- found
      } yield (for {
        (key, value) <- document if (key != "_id" && value.isInstanceOf[BasicDBList])
      } yield ((key, value.asInstanceOf[BasicDBList].toList))).toMap[String, List[Any]]).toList

      if (docsFound.isEmpty)
        sender ! ReceivedData(Failure(new NoSuchElementException("Nothing Found")), origin)
      else sender ! ReceivedData(Success(docsFound), origin)
    }
    // TODO - ScalaDoc
    case SearchFile(robotSerialNumber, timestampStart, timestampEnd, filetyp, content, origin) => ??? // TODO
  }

  def save(collection: MongoCollection, entry: DBObject) {
    collection += entry
  }

  def convertMapToSaveTypes(map: Map[String, List[Any]]): Map[String, List[Any]] = {
    for {
      (key, value) <- map
    } yield (key, for {
      vali <- value
    } yield getSaveableStuff(vali))
  }

  //TODO Maybe convert this to a Type
  def getSaveableStuff(from: Any) = {
    from match {
      case x: Int => x
      case x: Byte => x
      case x: Float => x
      case x: Double => x
      case x: Boolean => x
      case x: String => x
      case x: scala.collection.immutable.StringOps => x.repr
      case other => "NOT_SAVEABLE_MONGODBACTOR.SCALA_[ToStringed: " + other.toString + "|Type: " + other.getClass + "]"
    }
  }
}