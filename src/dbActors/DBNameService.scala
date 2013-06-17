package dbActors

import akka.actor.{Props, Actor}
import messages.agentMessages._
import messages.internalMessages.GetDatabaseNamesOrigin

/**
 * This actor acts as an agent between the user and our DBActor-System.
 * It starts and supervises the communication-actors.
 *
 * The user can request all robotIDs or a specific communication-actor (e.g. DBFacade).
 */
class DBNameService(robotSerialNumberList: Array[String]) extends Actor {

  println("DBNameService - erstellt")
  val childCommands = context.actorOf(Props[DBFacade], name = "DBFacade")
  println(childCommands)
  val childFiles = context.actorOf(Props[DBAccessFile], name = "DBAccessFile")

  override def preStart() {

    println("DBNameService preStart")
    context.watch(childCommands)
    context.watch(childFiles)
  }

  def receive = {
    /**
     * This function returns all serialnumbers to the sender.
     */

    case RobotSerialNumbers => println("DBNameService preStart - case RobotSerialNumbers"); sender ! ReceivedRobotSerialNumbers(robotSerialNumberList); println("DBNameService preStart - ReceivedRobotSerialNumbers an Sender")

    /**
     * This function returns the requested ActorRefs to the sender.
     */
    case DatabaseActors => println("DBNameService preStart -case DatabaseActors"); sender ! ReceivedDatabaseActors(childCommands, childFiles); println("DBNameService preStart -ReceivedDatabaseActors an Sender")
    case x => println("Unexpexted Message Send (DBNameService)")
  }
}
