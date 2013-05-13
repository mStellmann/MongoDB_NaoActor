package dbActors

import akka.actor.{ActorRef, Actor}
import messages.userMessages._
import messages.internalMessages.{SearchData, ReceivedData}
import scala.util.{Try, Success, Failure}
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
    // Notiz: Muss geprueft werden ob ein None richtig erstellt wird - im content parameter
    case SearchCommand(robotSerialNumber, timestampStart, timestampEnd, commandList, tagList) => mongoDBActor ! SearchData(robotSerialNumber, timestampStart, timestampEnd, Option(Map("commandList" -> commandList.get, "tagList" -> tagList.get)), sender)// TODO

    // TODO - ScalaDoc
      //eigents hinzugefuegt und auch wieder entfaernt, muss nochmal diskutiert werden
   // case SearchData(robotSerialNumber, timestampStart, timestampEnd, content, origin)  => ??? // TODO


    // TODO - ScalaDoc
    case ReceivedData(dataList, origin) => dataList match{
      case Success(list) => {
        for(doc <- list) doc.keys.foreach()
      }
      case Failure(list) => {

      }
    } // TODO


  }
}