package dbActors

import akka.actor.Actor
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.gridfs.Imports._
import messages.internalMessages._
import scala.util.{ Try, Success, Failure }
import com.mongodb.casbah.commons.MongoDBObject
import naogateway.value.Hawactormsg
import naogateway.value.NaoMessages._
import naogateway.value.NaoMessages.Conversions._
import scala.collection.mutable.MutableList
import naogateway.value.Hawactormsg.MixedValue
import messages.internalMessages.ReceivedFile
import scala.util.Failure
import scala.Some
import messages.internalMessages.SearchData
import messages.internalMessages.SaveFile
import messages.internalMessages.SearchFile
import messages.internalMessages.Save
import scala.util.Success
import messages.internalMessages.ReceivedData

// TODO - Max
/**
 * This actor works as an adapter for the connection and communication with a MongoDB-Database.
 * It is a basic interface for saving and finding objects in the database.
 */
class MongoDBActor(mongoDBClient: MongoClient, robotNames: Array[String]) extends Actor {
  import com.mongodb.casbah.commons.conversions.scala._
  //JodaTime doesn't work with JodaTime 
  DeregisterJodaTimeConversionHelpers()

  println("MongoDbActor created")

  def receive = {
    case Save(db, robotSerialNumber, timestamp, content) => {
      val mongoDBDoc = MongoDBObject("time" -> List(timestamp))
      val contentWithSupportedTypes = convertMapToSaveTypes(content)
      val dBEntry = mongoDBDoc ++ contentWithSupportedTypes.asDBObject

      val mongoCollection = mongoDBClient(db)(robotSerialNumber)
      save(mongoCollection, dBEntry)
    }

    //    case SaveCommand(db, robotSerialNumber, timestamp, command, content) => {
    //
    //      val mongoDBDoc = Map(
    //        "callModule" -> List(command.module.name.toString),
    //        "callMethod" -> List(command.method.name.toString),
    //        "callArgs" -> unpackMixedVals(command.parameters))
    //
    //      self ! Save(db, robotSerialNumber, timestamp, mongoDBDoc ++ content)
    //    }

    case SearchData(collections, robotSerialNumber, timestampStart, timestampEnd, content, origin) => {

      val start = timestampStart.getOrElse(0L)
      val end = timestampEnd.getOrElse(Long.MaxValue)
      val searchTime = {
        {
          ("time" $gte start $lte end)
        }
      }

      val tags = content.getOrElse(Map())
      val elemTags = for (entry <- tags) yield MongoDBObject(entry._1 -> MongoDBObject("$in" -> entry._2))

      val andQuery = MongoDBObject();
      val andList = List[MongoDBObject](searchTime) ++ elemTags //tags.asDBObject);
      andQuery.put("$and", andList);

      val dbToQuery = if (collections.isDefined) List(collections.get) else mongoDBClient.getDatabaseNames.toList

      val robotSerialList = robotSerialNumber match {
        case None => robotNames.toList
        case Some(id) => List(id)
      }

      val foundList = for {
        db <- dbToQuery
        robotSerial <- robotSerialList
      } yield {
        val mongoCollection = mongoDBClient(db)(robotSerial)

        val finalSearchRequest = andQuery
        println("Searching For:" + finalSearchRequest + " in " + db)

        val found = mongoCollection.find(finalSearchRequest)
        found
      }

      val docsFound = (for {
        found <- foundList
        document <- found
      } yield (for {
        //TODO if list else lassen keien forcompr
        //in db everthing is a List from AnyRef
        (key, value) <- document if (key != "_id" && value.isInstanceOf[BasicDBList])
      } yield ((key, value.asInstanceOf[BasicDBList].toList))).toMap[String, List[Any]]).toList

      //TODO in DB Access
      //      val commands = for (entry <- docsFound) yield {
      //        if (entry.contains("callModule")) {
      //          val callModule: Symbol = Symbol.apply(entry("callModule")(0).asInstanceOf[String])
      //          val callMethod: Symbol = Symbol.apply(entry("callMethod")(0).asInstanceOf[String])
      //          val callArgs: List[MixedValue] = dbTypesToMixedVals(entry("callArgs"))
      //          Call(callModule, callMethod, callArgs)
      //        }
      //      }

      if (docsFound.isEmpty)
        sender ! ReceivedData(Failure(new NoSuchElementException("Nothing Found")), origin)
      else {
        //        for (command <- commands if command != ()) println(command) //sender ! command
        sender ! ReceivedData(Success(docsFound), origin)
      }
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
      gfsFile.save
    }

    // TODO 
    case SearchFile(collection, robotSerialNumber, timestampStart, timestampEnd, filetyp, filename, content, origin) => {

      val start = timestampStart.getOrElse(0L)
      val end = timestampEnd.getOrElse(Long.MaxValue)
      val searchTime = "time" $gte start $lte end

      val tags = content.getOrElse(Map())
      val elemTags = for (entry <- tags) yield (entry._1 -> MongoDBObject("$in" -> entry._2))
      //TODO search Metadata
      val metadata = elemTags ++ searchTime

      val name = filename.getOrElse("")
      val fType = filetyp.getOrElse("")

      val andList = List(MongoDBObject("filename" -> name), MongoDBObject("contentType" -> fType), MongoDBObject("metadata" -> content))
      val andQuery = MongoDBObject();
      andQuery.put("$or", andList);

      val dbToQuery = if (collection.isDefined) List(collection.get) else mongoDBClient.getDatabaseNames.toList
      val foundList = for {
        db <- dbToQuery
      } yield {
        val mongoDB: MongoDB = mongoDBClient.getDB(db)
        val gridfs = GridFS(mongoDB)

        val finalSearchRequest = andQuery
        println("Searching For:" + finalSearchRequest + " in " + db)

        val found = gridfs.find(finalSearchRequest)
        found
      }
      println(foundList)
      val docsFound = (for {
        found <- foundList
        file <- found
      } yield (file.getFilename(), file.getContentType(), file.getUploadDate().getTime(),
        {
          val inputStream = file.getInputStream()
          val byteArray = new Array[Byte](file.getLength().toInt)
          inputStream.read(byteArray)
          byteArray
        }))

      if (docsFound.isEmpty)
        sender ! ReceivedFile(Failure(new NoSuchElementException("Nothing Found")), origin)
      else {
        sender ! ReceivedFile(Success(docsFound), origin)
      }
    }
    case GetDataBaseNames(origin)  =>  sender ! DataBaseNamesOrigin(mongoDBClient.getDatabaseNames.toList,origin)
    case x => println("mongoDB got unexpected " + x)
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

  //  def unpackMixedVals(list: List[MixedValue]): List[Any] = {
  //    for (arg <- list) yield {
  //      if (arg.hasInt) arg.getInt
  //      else if (arg.hasFloat()) arg.getFloat
  //      else if (arg.hasBool()) arg.getBool
  //      else if (arg.hasString()) arg.getString
  //      else if (arg.hasBinary()) arg.getBinary
  //      else if (arg.getArrayCount() > 0) {
  //        import scala.collection.JavaConversions._
  //        unpackMixedVals(arg.getArrayList().toList)
  //      }
  //    }
  //  }
  //
  //  def dbTypesToMixedVals(list: List[Any]): List[MixedValue] = {
  //    import naogateway.value.NaoMessages.Conversions._
  //    for (arg <- list) yield {
  //      arg match {
  //        case x: Int => int2Mixed(x)
  //        case x: Double => float2Mixed(x.toFloat)
  //        case x: Boolean => bool2Mixed(x)
  //        case x: String => string2Mixed(x)
  //        //        case x: Byte => x
  //        case x: BasicDBList => {
  //          val floatList = for (i <- 0 until x.size()) yield {
  //            x.toList(i) match {
  //              case d: Double => d.toFloat
  //              case x => x
  //            }
  //          }
  //          anyToMixedVal(floatList)
  //        }
  //      }
  //    }
  //  }
}