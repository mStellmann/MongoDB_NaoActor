package dbActors

import akka.actor.{Props, Actor}
import com.mongodb.casbah.MongoClient
import java.util.Properties
import java.io.FileInputStream

/**
 * This actor initializes our DBActor-System and supervises the own actor-childs.
 * First of all it creates the MongoDBActor to providing the communication with the Database.
 * Afterwards the DBAgent will be created, which provides the user->system-communication.
 */

// TODO - Matthias
class DBConfigurator extends Actor {
  /*  Anmerkungen:
  aus der CFG muss die datenbank connection gelesen werden
  dbClient beim erstellen des mongodbactors erstellen?? dependency injection..
  */
  val cfgReader = new Properties()
  cfgReader.load(new FileInputStream("configs/DBActorSystemConfig.cfg"))

  override def preStart = {


    // cfgReader.getProperty("robotSerialNumbers").split(",")
    val agentList = Nil

    // creating and starting the MongoDBActor
    context.actorOf(Props().withCreator(new MongoDBActor(MongoClient())), name = "mongoDBActor")
    // creating and starting the DBAgent
    context.actorOf(Props().withCreator(new DBAgent(agentList)), name = "DBAgent")
  }

  def receive = {
    ??? // TODO
  }
}
