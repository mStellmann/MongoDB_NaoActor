package dbActors

import akka.actor.Actor
import messages.internalMessages.{SearchFile, SearchData, SaveFile, Save}
import com.mongodb.casbah.MongoClient

/**
 * This actor works as an adapter for the connection and communication with a MongoDB-Database.
 * It is a basic interface for saving and finding objects in the database.
 */
class MongoDBActor(mongoDBClient: MongoClient) extends Actor {

  /* Anmerkungen
    anyrefs aus der db matchen
   in der DB -> Enum    --- typen in enum speichern und gegen enum matchen
  */
  def receive = {
    // TODO - ScalaDoc
    case Save(collection, robotSerialNumber, timestamp, content) => ??? // TODO

    // TODO - ScalaDoc
    case SaveFile(collection, robotSerialNumber, timestamp, filename, filetyp, file, content) => ??? // TODO

    // TODO - ScalaDoc
    case SearchData(collection, robotSerialNumber, timestampStart, timestampEnd, content, origin) => ??? // TODO

    // TODO - ScalaDoc
    case SearchFile(collection, robotSerialNumber, timestampStart, timestampEnd, filetyp, content, origin) => ??? // TODO
  }
}