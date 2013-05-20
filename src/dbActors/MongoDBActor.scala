package dbActors

import akka.actor.Actor
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.gridfs.Imports._
import messages.internalMessages.{ Save, SaveCommand, SaveFile }
import messages.internalMessages.{ SearchData, SearchFile }
import messages.internalMessages.{ ReceivedData, ReceivedFile }
import scala.util.{ Try, Success, Failure }
import com.mongodb.casbah.commons.MongoDBObject
import naogateway.value.Hawactormsg
import naogateway.value.NaoMessages._
import naogateway.value.NaoMessages.Conversions._
import scala.collection.mutable.MutableList
import naogateway.value.Hawactormsg.MixedValue

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

      val mongoDBDoc = Map(
        "callModule" -> List(command.module.name.toString),
        "callMethod" -> List(command.method.name.toString),
        "callArgs" -> unpackMixedVals(command.parameters))

      self ! Save(db, robotSerialNumber, timestamp, mongoDBDoc ++ content)
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
      //println(mongoDBClient.getDatabaseNames)
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
      //      println("Searching For:" + finalSearchRequest)

      val found = mongoCollection.find(finalSearchRequest)

      val docsFound = (for {
        document <- found
      } yield (for {
        //TODO if list else lassen keien forcompr
        (key, value) <- document if (key != "_id" && value.isInstanceOf[BasicDBList])
      } yield ((key, value.asInstanceOf[BasicDBList].toList))).toMap[String, List[Any]]).toList

      //TODO in DB Access
      val commands = for (entry <- docsFound) yield {
        if (entry.contains("callModule")) {
          val callModule: Symbol = Symbol.apply(entry("callModule")(0).asInstanceOf[String])
          val callMethod: Symbol = Symbol.apply(entry("callMethod")(0).asInstanceOf[String])
          val callArgs: List[MixedValue] = dbTypesToMixedVals(entry("callArgs"))
          Call(callModule, callMethod, callArgs)
        }
      }

      if (docsFound.isEmpty)
        sender ! ReceivedData(Failure(new NoSuchElementException("Nothing Found")), origin)
      else {
        for (command <- commands if command != ()) sender ! command
        sender ! ReceivedData(Success(docsFound), origin)
      }
    }

    // TODO 
    case SearchFile(robotSerialNumber, timestampStart, timestampEnd, filetyp, content, origin) => ???
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

  def getSaveableStuff(from: Any) = {
    from match {
      case x: Int => x
      case x: Byte => x
      case x: Float => x
      case x: Double => x
      case x: Boolean => x
      case x: String => x
      case x: List[_] => x
      case x: scala.collection.immutable.StringOps => x.repr
      case other => "NOT_SAVEABLE_MONGODBACTOR.SCALA_[ToStringed: " + other.toString + "|Type: " + other.getClass + "]"
    }
  }

  def unpackMixedVals(list: List[MixedValue]): List[Any] = {
    for (arg <- list) yield {
      if (arg.hasInt) arg.getInt
      else if (arg.hasFloat()) arg.getFloat
      else if (arg.hasBool()) arg.getBool
      else if (arg.hasString()) arg.getString
      else if (arg.hasBinary()) arg.getBinary
      else if (arg.getArrayCount() > 0) {
        import scala.collection.JavaConversions._
        unpackMixedVals(arg.getArrayList().toList)
      }
    }
  }

  def dbTypesToMixedVals(list: List[Any]): List[MixedValue] = {
    import naogateway.value.NaoMessages.Conversions._
    for (arg <- list) yield {
      arg match {
        case x: Int => int2Mixed(x)
        case x: Double => float2Mixed(x.toFloat)
        case x: Boolean => bool2Mixed(x)
        case x: String => string2Mixed(x)
        //        case x: Byte => x
        case x: BasicDBList => {
          val floatList = for (i <- 0 until x.size()) yield {
            x.toList(i) match {
              case d: Double => d.toFloat
              case x => x
            }
          }
          anyToMixedVal(floatList)
        }
      }
    }
  }
}