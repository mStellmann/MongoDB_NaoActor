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
// TODO - Config für MongoDB einlesen..
class DBConfigurator extends Actor {
  val cfgReader = new Properties()
  cfgReader.load(new FileInputStream("configs/DBActorSystemConfig.cfg"))
  val robotSNRList = cfgReader.getProperty("robotSerialNumbers").split(",")

  // creating and starting the MongoDBActor
  val childMongo = context.actorOf(Props().withCreator(new MongoDBActor(MongoClient())), name = "MongoDBActor")
  // creating and starting the DBAgent
  val childAgent = context.actorOf(Props().withCreator(new DBAgent(robotSNRList)), name = "DBAgent")

  override def preStart() {
    context.watch(childMongo)
    context.watch(childAgent)
  }

  def receive = {
    case _ => println("DBConfigurator received something") // TODO - supervising bla?!
  }
}
