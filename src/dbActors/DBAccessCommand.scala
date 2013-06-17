package dbActors

import akka.actor.{ActorRef, Actor}
import messages.userMessages._
import messages.userMessages.SaveCommand
import messages.internalMessages._
import scala.util.{Try, Success, Failure}
import scala.runtime.RichLong
import scala.Some
import naogateway.value.NaoMessages.Call
import naogateway.value.Hawactormsg.MixedValue
import com.mongodb.casbah.Imports._



/**
 * This Actor provides the functionality to logging and reading commands from a Nao-Robot.
 */

// TODO - Gregstar
class DBAccessCommand extends Actor {

  //  val mongoDBActor = context.actorSelection("/user/DBConfigurator/MongoDBActor")
  val mongoDBActor = context.actorFor("/user/DBConfigurator/MongoDBActor")
  //println("dbacces " + mongoDBActor)

  val agent = context.actorSelection("/user/DBConfigurator/DBAgent")


  /**
   * Verarbeitet eingehende Nachrichten
   *
   */
  def receive = {
    
    /** Zerlege den Call Befehl und speichere ihn in der MongoDB */
    case SaveCommand(robotSerialNumber, timestamp, call, tagList) => {
      //println("SaveCommand in DB Access")

      /** Zerlege Call Klass */
      val mongoDBDoc = Map(
        "callModule" -> List(call.module.name.toString),
        "callMethod" -> List(call.method.name.toString),
        "callArgs" -> unpackMixedVals(call.parameters))

      /** Speichere Tags nur klein */
      val content = Map("tags" -> (for (tag <- tagList) yield tag.toLowerCase()).toList)

      mongoDBActor ! Save(call.module.name, robotSerialNumber, timestamp, mongoDBDoc ++ content)
    }

    /** Suche ein Call in der Datenbank */
    case SearchCommand(collection, robotSerialNumber, timestampStart, timestampEnd, tagList) =>
      val searchMap = if (tagList.isDefined) Some(Map("tags" -> tagList.get)) else None
      /** Leite die Frage an die MongoDB weiter */
      mongoDBActor ! SearchData(robotSerialNumber, collection, timestampStart, timestampEnd, searchMap, sender)

    
     /** Verarbeitet die gefunden Daten und baut den Call Befehl zusammen */
    case ReceivedData(dataList, origin) => dataList match {
      case Success(list) => {
        val commands = for (entry <- list) yield {
          //Baue den Call Befehl zusammen
          if (entry.contains("callModule")) {
            val callModule: Symbol = Symbol.apply(entry("callModule")(0).asInstanceOf[String])
            val callMethod: Symbol = Symbol.apply(entry("callMethod")(0).asInstanceOf[String])
            val callArgs: List[MixedValue] = dbTypesToMixedVals(entry("callArgs"))
            (Call(callModule, callMethod, callArgs), entry("time")(0), entry("tags"))
          }

        }
        //val only = commands.filter(_.isInstanceOf[(Call, Any, Any)])
        // val onlyCommands:List[(Call,RichLong,List[String])] = only.foldLeft(List[(Call,RichLong,List[String])] ()) ((list,(call,time,tags)) => list ++ List((call.asInstanceOf[Call],time.asInstanceOf[RichLong],tags.asInstanceOf[List[String]])))
        // val onlyCommands:List[(Call,RichLong,List[String])] = only.foldLeft(Nil) ((list,(call,time,tags)) => list ++ List((call.asInstanceOf[Call],time.asInstanceOf[RichLong],tags.asInstanceOf[List[String]])))
        
        //Cast zu den richtigen Typen fuer die Rueckgabe Nachricht
        val onlyCommands: List[(Call, Long, List[String])] = for ((call, time, tags) <- commands) yield {
          (call.asInstanceOf[Call], time.asInstanceOf[Long], tags.asInstanceOf[List[String]])
        }
        
        //Sortiere die Rueckgabe nach speicherzeitpunkt
        val sortedOnlyCommands = onlyCommands.sortBy(_._2)

        origin ! ReceivedCommand(Left(sortedOnlyCommands))
      }

      //Keine gespeicherten Befehle in der Datenbank gefunden
      case Failure(list) => {
        origin ! ReceivedCommand(Right("Error"))
      }


    }
    /** Fragt die datenbank nach vorhandenen Datenbanken*/
    case GetDatabaseNames => mongoDBActor ! GetDatabaseNamesOrigin(sender)
    
    /** Liefert die vorhanden Datenbanken zurück an den Anfrager*/
    case DatabaseNamesOrigin(list, origin) => origin ! DatabaseNames(list)

  }

  /**
   * Entpackt MixedValue's zu den unterliegenden Typen
   */
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

  /**
   * Wandelt die in der DB gespeicherten Typen wieder zu MixedValue's für den Call Befehl
   */
  def dbTypesToMixedVals(list: List[Any]): List[MixedValue] = {
    import naogateway.value.NaoMessages.Conversions._
    for (arg <- list) yield {
      arg match {
        case x: Int => int2Mixed(x)
        case x: Double => float2Mixed(x.toFloat)
        case x: Boolean => bool2Mixed(x)
        case x: String => string2Mixed(x)
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