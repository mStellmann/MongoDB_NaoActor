package dbActors

import akka.actor.{ActorRef, Actor}
import messages.userMessages._
import messages.internalMessages.RetrievedData

/**
 * This Actor provides the functionality to logging and reading commands from a Nao-Robot.
 */

// TODO - Gregstar
class DBAccessCommand extends Actor {
  def receive = {
    // TODO - ScalaDoc
    case SaveCommand(robotSerialNumber, timestamp, call, tagList) => ??? // TODO

    // TODO - ScalaDoc
    case SearchCommand(robotSerialNumber, timestampStart, timestampEnd, commandList, tagList) => ??? // TODO

    // TODO - ScalaDoc
    case RetrievedData(dataList, origin) => ??? // TODO
  }
}