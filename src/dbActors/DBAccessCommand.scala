package dbActors

import akka.actor.{ActorRef, Actor}
import messages.userMessages._
import messages.internalMessages._
import scala.util.{Try, Success, Failure}
import messages.userMessages.SaveCommand
import naogateway.value.Hawactormsg.MixedValue
import com.mongodb.casbah.Imports._
import scala.util.Failure
import messages.userMessages.SearchCommand
import scala.util.Success
import messages.userMessages.SaveCommand
import naogateway.value.NaoMessages.Call
import scala.runtime.RichLong
import scala.util.Failure
import scala.Some
import messages.userMessages.SearchCommand
import messages.userMessages.ReceivedCommand
import messages.internalMessages.SearchData
import naogateway.value.NaoMessages.Call
import messages.internalMessages.Save
import scala.util.Success
import messages.internalMessages.ReceivedData
import messages.userMessages.SaveCommand

/**
 * This Actor provides the functionality to logging and reading commands from a Nao-Robot.
 */

// TODO - Gregstar
class DBAccessCommand extends Actor {

  //  val mongoDBActor = context.actorSelection("/user/DBConfigurator/MongoDBActor")
  val mongoDBActor = context.actorFor("/user/DBConfigurator/MongoDBActor")
  println("dbacces " + mongoDBActor)

  val agent = context.actorSelection("/user/DBConfigurator/DBAgent")

  def receive = {
    // TODO - ScalaDoc
    //case SaveCommand(robotSerialNumber, timestamp, call, tagList) => mongoDBActor ! Save(call.module.name, robotSerialNumber, timestamp, tagList)
    case SaveCommand(robotSerialNumber, timestamp, call, tagList) => {
      println("SaveCommand in DB Access")

      val mongoDBDoc = Map(
        "callModule" -> List(call.module.name.toString),
        "callMethod" -> List(call.method.name.toString),
        "callArgs" -> unpackMixedVals(call.parameters))

      val content = Map("tags" -> (for (tag <- tagList) yield tag.toLowerCase()).toList)

      mongoDBActor ! Save(call.module.name, robotSerialNumber, timestamp, mongoDBDoc ++ content)
    }

    // TODO - ScalaDoc
    // Notiz: Muss geprueft werden ob ein None richtig erstellt wird - im content parameter
    case SearchCommand(collection, robotSerialNumber, timestampStart, timestampEnd, tagList) =>
      val searchMap = if (tagList.isDefined) Some(Map("tags" -> tagList.get)) else None
      mongoDBActor ! SearchData(robotSerialNumber, collection, timestampStart, timestampEnd, searchMap, sender) // TODO

    // TODO - ScalaDoc
    //eigents hinzugefuegt und auch wieder entfaernt, muss nochmal diskutiert werden
    // case SearchData(robotSerialNumber, timestampStart, timestampEnd, content, origin)  => ??? // TODO

    // TODO - ScalaDoc
    case ReceivedData(dataList, origin) => dataList match {

      case Success(list) => {
        val commands = for (entry <- list) yield {
          if (entry.contains("callModule")) {
            val callModule: Symbol = Symbol.apply(entry("callModule")(0).asInstanceOf[String])
            val callMethod: Symbol = Symbol.apply(entry("callMethod")(0).asInstanceOf[String])
            val callArgs: List[MixedValue] = dbTypesToMixedVals(entry("callArgs"))
            (Call(callModule, callMethod, callArgs), entry("time")(0), entry("tags"))
          }

        }
        val only = commands.filter(_.isInstanceOf[(Call, Any, Any)])
        // val onlyCommands:List[(Call,RichLong,List[String])] = only.foldLeft(List[(Call,RichLong,List[String])] ()) ((list,(call,time,tags)) => list ++ List((call.asInstanceOf[Call],time.asInstanceOf[RichLong],tags.asInstanceOf[List[String]])))
        // val onlyCommands:List[(Call,RichLong,List[String])] = only.foldLeft(Nil) ((list,(call,time,tags)) => list ++ List((call.asInstanceOf[Call],time.asInstanceOf[RichLong],tags.asInstanceOf[List[String]])))
        val onlyCommands: List[(Call, Long, List[String])] = for ((call, time, tags) <- only) yield {
          (call.asInstanceOf[Call], time.asInstanceOf[Long], tags.asInstanceOf[List[String]])
        }
        origin ! ReceivedCommand(Left(onlyCommands))
      }

      case Failure(list) => {
        origin ! ReceivedCommand(Right("Error"))
      }
    } // TODO
//    case ReceivedFile(fileList, origin) => dataList match {
//
//      case Success(list) => {
//        val files = for (entry <- list) yield {
//          if (entry.contains("")) {
//
//
//          }
//
//        }
//
//        }
//        origin ! ReceivedAudioFile(Left(onlyAudioFiles))
//        origin ! ReceivedVideoFile(Left(onlyVideoFiles))
//
//
//      case Failure(list) => {
//        //origin ! ReceivedCommand(Right("Error"))
//      }
//    }
  }

  def unpackMixedVals(list: List[MixedValue]): List[Any] = {
    for (arg <- list) yield {
      if (arg.hasInt) arg.getInt
      else if (arg.hasFloat()) arg.getFloat
      else if (arg.hasBool()) arg.getBool
      else if (arg.hasString()) arg.getString
      else if (arg.hasBinary()) arg.getBinary
      else if (arg.getArrayCount() > 0) {
        import scala.collection.JavaConversions._
        unpackMixedVals(arg.getArrayList().toList)
      }
    }
  }

  def dbTypesToMixedVals(list: List[Any]): List[MixedValue] = {
    import naogateway.value.NaoMessages.Conversions._
    for (arg <- list) yield {
      arg match {
        case x: Int => int2Mixed(x)
        case x: Double => float2Mixed(x.toFloat)
        case x: Boolean => bool2Mixed(x)
        case x: String => string2Mixed(x)
        //        case x: Byte => x
        case x: BasicDBList => {
          val floatList = for (i <- 0 until x.size()) yield {
            x.toList(i) match {
              case d: Double => d.toFloat
              case x => x
            }
          }
          anyToMixedVal(floatList)
        }
      }
    }
  }
}