package dbActors

import akka.actor.{ActorRef, Actor}
import messages.userMessages._
import messages.internalMessages.{SearchData, ReceivedData}

/**
 * This Actor provides the functionality to logging and reading commands from a Nao-Robot.
 */

// TODO - Gregstar
class DBAccessCommand extends Actor {


  val mongoDBActor = context.actorSelection("/user/DBConfigurator/MongoDBActor")
  val agent = context.actorSelection("/user/DBConfigurator/DBAgent")


  def receive = {
    // TODO - ScalaDoc
    case SaveCommand(robotSerialNumber, timestamp, call, tagList) => mongoDBActor ! save(call.module.name, robotSerialNumber, timestamp, tagList)



    // TODO - ScalaDoc
    case SearchCommand(robotSerialNumber, timestampStart, timestampEnd, commandList, tagList) => mongoDBActor ! SearchData()// TODO

    // TODO - ScalaDoc
    case SearchData(collection, robotSerialNumber, timestampStart, timestampEnd, content, origin)  => ??? // TODO


    // TODO - ScalaDoc
    case ReceivedData(dataList, origin) => ??? // TODO


  }
}