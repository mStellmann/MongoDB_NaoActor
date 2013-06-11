package dbActors

import akka.actor.Actor
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.gridfs.Imports._
import com.mongodb.casbah.commons.MongoDBObject
import scala.collection.mutable.MutableList
import scala.Some
import scala.util.{ Try, Success, Failure }
import naogateway.value.Hawactormsg
import naogateway.value.NaoMessages._
import naogateway.value.NaoMessages.Conversions._
import naogateway.value.Hawactormsg.MixedValue
import messages.internalMessages.Save
import messages.internalMessages.SearchData
import messages.internalMessages.ReceivedData
import messages.internalMessages.SaveFile
import messages.internalMessages.SearchFile
import messages.internalMessages.ReceivedFile
import messages.internalMessages.GetDatabaseNamesOrigin
import messages.internalMessages.DatabaseNamesOrigin

/**
 * This actor works as an adapter for the connection and communication with a MongoDB-Database.
 * It is a basic interface for saving and finding objects in the database.
 */
class MongoDBActor(mongoDBClient: MongoClient, robotNames: Array[String]) extends Actor {
  import com.mongodb.casbah.commons.conversions.scala._
  /**GridFS doesn't work well with JodaTime*/
  DeregisterJodaTimeConversionHelpers()

  //println("MongoDbActor created")

  def receive = {

    /**
     * Speichere die content Map als MongoDocument in der DB db unter der Collection robotSerialNumber
     */
    case Save(db, robotSerialNumber, timestamp, content) => {
      val mongoDBDoc = MongoDBObject("time" -> List(timestamp))

      /**Speichere nur unterstuezte DatenTypen ab*/
      val contentWithSupportedTypes = convertMapToSaveTypes(content)
      val dBEntry = mongoDBDoc ++ contentWithSupportedTypes.asDBObject

      val mongoCollection = mongoDBClient(db)(robotSerialNumber)
      save(mongoCollection, dBEntry)
    }

    /**
     * Suche nach Daten
     * bei Nones durchsuche alle Moeglichkeiten
     */
    case SearchData(collections, robotSerialNumber, timestampStart, timestampEnd, contentWithTags, origin) => {

      /** Time Teil der Query */
      val startSearchTime = timestampStart.getOrElse(0L)
      val endSearchTime = timestampEnd.getOrElse(Long.MaxValue)
      val searchWithTimeQuery = {
        {
          ("time" $gte startSearchTime $lte endSearchTime)
        }
      }

      /** Tag Teil der Query */
      val tags = contentWithTags.getOrElse(Map())
      val tagSearchQuery = for (entry <- tags) yield MongoDBObject(entry._1 -> MongoDBObject("$in" -> entry._2))

      /** Gesamt Query */
      val andQuery = MongoDBObject();
      val andList = List[MongoDBObject](searchWithTimeQuery) ++ tagSearchQuery
      andQuery.put("$and", andList);

      /** Abzufragende DBs */
      val dbToQuery = if (collections.isDefined) List(collections.get) else mongoDBClient.getDatabaseNames.toList

      /** Abzufragende Collections */
      val robotSerialList = robotSerialNumber match {
        case None => robotNames.toList
        case Some(id) => List(id)
      }

      /** Gefundene Dokumente */
      val foundList = for {
        db <- dbToQuery
        robotSerial <- robotSerialList
      } yield {
        val mongoCollection = mongoDBClient(db)(robotSerial)
        val finalSearchRequest = andQuery
        //println("Searching For:" + finalSearchRequest + " in " + db)
        val found = mongoCollection.find(finalSearchRequest)
        found
      }

      /** Gefundene Dokumente auf List[Any] gecastet fuer die Rueckgabe Nachricht*/
      val documentsFound = (for {
        found <- foundList
        document <- found
      } yield (for {
        //moegliche Verbesserung pruefe ob Liste
        //derzeit speichern wir alles als Liste
        //aus der db kommend ist alles eine Liste von AnyRef
        (key, value) <- document if (key != "_id" && value.isInstanceOf[BasicDBList])
      } yield ((key, value.asInstanceOf[BasicDBList].toList))).toMap[String, List[Any]]).toList

      if (documentsFound.isEmpty)
        sender ! ReceivedData(Failure(new NoSuchElementException("Nothing Found")), origin)
      else {
        sender ! ReceivedData(Success(documentsFound), origin)
      }
    }

    /**
     * Speichere das File aus dem Byte[] in der DB
     * mit der content Map als Metadata + robotSerial und time
     */
    case SaveFile(db, robotSerialNumber, timestamp, filename, filetyp, file, content) => {

      val mongoDBDoc = MongoDBObject("time" -> List(timestamp), "robotSerialNumber" -> List(robotSerialNumber))
      /**Speichere nur unterstuezte DatenTypen ab*/
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

    /**
     * Suche nach Files
     * bei Nones durchsuche alle Moeglichkeiten
     * Suche nach Time oder Tags funktioniert noch nicht!
     */
    case SearchFile(collection, robotSerialNumber, timestampStart, timestampEnd, filetyp, filename, content, origin) => {

      /** Time Teil der Query */
      val startSearchTime = timestampStart.getOrElse(0L)
      val endSearchTime = timestampEnd.getOrElse(Long.MaxValue)
      val searchWithTimeQuery = "time" $gte startSearchTime $lte endSearchTime

      /** Tag Teil der Query */
      val tags = content.getOrElse(Map())
      val elemTags = for (entry <- tags) yield (entry._1 -> MongoDBObject("$in" -> entry._2))

      //TODO metadata query funktionstuechtig machen
      val metadata = elemTags ++ searchWithTimeQuery

      val name = filename.getOrElse("")
      val fType = filetyp.getOrElse("")

      val andQuery = MongoDBObject()
      val andList = List(MongoDBObject("filename" -> name), MongoDBObject("contentType" -> fType), MongoDBObject("metadata" -> content))
      andQuery.put("$or", andList)

      /** Abzufragende DBs */
      val dbsToQuery = if (collection.isDefined) List(collection.get) else mongoDBClient.getDatabaseNames.toList

      /** Gefundene Dokumente */
      val foundList = for {
        db <- dbsToQuery
      } yield {
        val mongoDB: MongoDB = mongoDBClient.getDB(db)
        val gridfs = GridFS(mongoDB)
        val finalSearchRequest = andQuery
        //println("Searching For:" + finalSearchRequest + " in " + db)
        val found = gridfs.find(finalSearchRequest)
        found
      }

      //println(foundList)

      /** Aus Gefundenen Dokumente Filename FileTyp Date byte[] fuer die Rueckgabe Nachricht extrahieren*/
      val docsFound = (for {
        found <- foundList
        file <- found
      } yield (file.getFilename(), file.getContentType(), file.getUploadDate().getTime(),
        { //File in Byte[] verwandeln
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

    /**
     * GibT alle vorhanden DatenbankNamen zurueck
     */
    case GetDatabaseNamesOrigin(origin) => sender ! DatabaseNamesOrigin(mongoDBClient.getDatabaseNames.toList, origin)

    case x => println("mongoDB got unexpected " + x)
  }

  /**
   * Speichere das DBObject in der Collection
   */
  def save(collection: MongoCollection, entry: DBObject) {
    collection += entry
  }

  /**
   * Iteriert ueber eine Map und extrahiert in der DB speicherbare Sachen
   */
  def convertMapToSaveTypes(map: Map[String, List[Any]]): Map[String, List[Any]] = {
    for {
      (key, value) <- map
    } yield (key, for {
      vali <- value
    } yield getSaveableStuff(vali))
  }

  /**
   * Gibt von der DB unterstuezten Typen zurueck
   * Bei nicht unterstuezten wird ein String generiert
   */
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
}
  // Code Reste
  //
  //jetzt in DB Access
  //      val commands = for (entry <- docsFound) yield {
  //        if (entry.contains("callModule")) {
  //          val callModule: Symbol = Symbol.apply(entry("callModule")(0).asInstanceOf[String])
  //          val callMethod: Symbol = Symbol.apply(entry("callMethod")(0).asInstanceOf[String])
  //          val callArgs: List[MixedValue] = dbTypesToMixedVals(entry("callArgs"))
  //          Call(callModule, callMethod, callArgs)
  //        }
  //      }
  //    case SaveCommand(db, robotSerialNumber, timestamp, command, content) => {
  //
  //      val mongoDBDoc = Map(
  //        "callModule" -> List(command.module.name.toString),
  //        "callMethod" -> List(command.method.name.toString),
  //        "callArgs" -> unpackMixedVals(command.parameters))
  //
  //      self ! Save(db, robotSerialNumber, timestamp, mongoDBDoc ++ content)
  //    }
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