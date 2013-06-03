package dbActors

import akka.actor.{Props, Actor}
import messages.agentMessages._
import messages.internalMessages.GetDatabaseNamesOrigin

/**
 * This actor acts as an agent between the user and our DBActor-System.
 * It starts and supervises the communication-actors.
 *
 * The user can request all robotIDs or a specific communication-actor (e.g. DBAccessCommand).
 */
class DBAgent(robotSerialNumberList: Array[String]) extends Actor {

  println("DBAgent - erstellt")
  val childCommands = context.actorOf(Props[DBAccessCommand], name = "DBAccessCommand")
  println(childCommands)
  val childFiles = context.actorOf(Props[DBAccessFile], name = "DBAccessFile")

  override def preStart() {

    println("DBAgent preStart")
    context.watch(childCommands)
    context.watch(childFiles)
  }

  def receive = {
    /**
     * This function returns all serialnumbers to the sender.
     */

    case RobotSerialNumbers =>  println("DBAgent preStart - case RobotSerialNumbers") ;sender ! ReceivedRobotSerialNumbers(robotSerialNumberList) ; println("DBAgent preStart - ReceivedRobotSerialNumbers an Sender")

    /**
     * This function returns the requested ActorRefs to the sender.
     */
    case DatabaseActors => println("DBAgent preStart -case DatabaseActors") ; sender ! ReceivedDatabaseActors(childCommands, childFiles)   ; println("DBAgent preStart -ReceivedDatabaseActors an Sender")
    case x => println("Unexpexted Message Send (DBAgent)")
  }
}
