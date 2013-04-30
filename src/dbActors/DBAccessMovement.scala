package dbActors

import akka.actor.{ActorRef, Actor}
import messages.userMessages.{SearchMovement, SaveMovement}
import messages.internalMessages.RetrievedData
import scala.util.Try

/**
 * This Actor provides the functionality to logging and reading movements from a Nao-Robot.
 */
class DBAccessMovement extends Actor {
  def receive = {
    // TODO - ScalaDoc
    case SaveMovement(robotSerialNumber, timestamp, movementCommand, argumentList, tagList) => ??? // TODO

    // TODO - ScalaDoc
    case SearchMovement(robotSerialNumber, timestampStart, timestampEnd, commandList, tagList) => ??? // TODO

    // TODO - ScalaDoc
    case RetrievedData(dataList, origin)
  }
}