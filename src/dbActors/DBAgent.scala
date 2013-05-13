package dbActors

import akka.actor.{Props, Actor}
import messages.agentMessages._

/**
 * This actor acts as an agent between the user and our DBActor-System.
 * It starts and supervises the communication-actors.
 *
 * The user can request all robotIDs or a specific communication-actor (e.g. DBAccessFile).
 */
class DBAgent(robotSerialNumberList: Array[String]) extends Actor {
  val childCommands = context.actorOf(Props[DBAccessCommand], name = "DBAccessCommand")
  val childFiles = context.actorOf(Props[DBAccessFile], name = "DBAccessFile")

  override def preStart() {
    context.watch(childCommands)
    context.watch(childFiles)
  }

  def receive = {
    /**
     * This function returns all serialnumbers to the sender.
     */
    case RobotSerialNumbers => sender ! ReceivedRobotSerialNumbers(robotSerialNumberList)

    /**
     * This function returns the requested ActorRefs to the sender.
     */
    case DatabaseActors => sender ! ReceivedDatabaseActors(childCommands, childFiles)
  }
}
