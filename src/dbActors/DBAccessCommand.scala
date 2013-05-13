package dbActors

import akka.actor.{ActorRef, Actor}
import messages.userMessages._
import messages.internalMessages.ReceivedData

/**
 * This Actor provides the functionality to logging and reading commands from a Nao-Robot.
 */

// TODO - Gregstar
class DBAccessCommand extends Actor {

  override def preStart = {

    val mongoDBActor = context.actorFor("mongoDBActor")


  }

  def receive = {
    // TODO - ScalaDoc
    case SaveCommand(robotSerialNumber, timestamp, call, tagList) => // TODO

    // TODO - ScalaDoc
    case SearchCommand(robotSerialNumber, timestampStart, timestampEnd, commandList, tagList) => ??? // TODO

    // TODO - ScalaDoc
    case ReceivedData(dataList, origin) => ??? // TODO
  }
}