package dbActors

import akka.actor.Actor
import messages.agentMessages.{DatabaseActor, RobotSerialNumbers}

/**
 * This actor acts as an agent between the user and our DBActor-System.
 * It starts and supervises the communication-actors.
 *
 * The user can request all robotIDs or a specific communication-actor (e.g. DBAccessFile).
 */

// TODO - Matthias
class DBAgent(robotSerialNumberList: List[String]) extends Actor {
  def receive = {
    // TODO - ScalaDoc
    case RobotSerialNumbers => ??? // TODO

    // TODO - ScalaDoc
    case DatabaseActor(databaseActorTyp) => ??? // TODO
  }
}
