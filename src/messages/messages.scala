package messages

// communication between MongoDBActor and "Use-Case-Actors-For-Saving-In-Database"(further DatabaseActor) (e.g. DBFacade)
package internalMessages {

import akka.actor.ActorRef
import scala.util.Try
import naogateway.value.NaoMessages.Call

/**
 * Schickt eine Liste mit den Collections ( Bsp: ALMotion )
 *
 * @param databases
 */
case class DatabaseNames(databases: List[String])

/**
 * Interne Nachricht, schickt eine Liste mit den Collections  ( Bsp: ALMotion )
 * @param databases
 * @param origin  urspruengliche Anfrage Referenz
 */
case class DatabaseNamesOrigin(databases: List[String], origin: ActorRef)


/**
 * Anfrage fuer die Collection Namen,
 * liefert DatabaseNames zurueck
 */
case object GetDatabaseNames

/**
 * Interne Nachricht
 * @param origin
 */
case class GetDatabaseNamesOrigin(origin: ActorRef)

/**
 * Interne Nachricht
 * @param collection   Bsp: ALMotion
 * @param robotSerialNumber  Die Seriennummer des Roboters
 * @param timestamp    Zeitstempel
 * @param content      Tags ( Bsp: augen, nachricht, [erleichtert die Suche])
 */
case class Save(collection: String, robotSerialNumber: String, timestamp: Long, content: Map[String, List[Any]])

/**
 * Nachricht User->DBFacade
 * @param collection    BSp: ALTextToSpeach
 * @param robotSerialNumber  Die Seriennummer des Roboters
 * @param timestamp   Zeitstempel
 * @param command     Befehl
 * @param content     Tags ( Bsp: augen, nachricht, [erleichtert die Suche])
 */
case class SaveCommand(collection: String, robotSerialNumber: String, timestamp: Long, command: Call, content: Map[String, List[AnyVal]])

/**
 * Nachricht User->DBFacade
 * @param collection   Bsp: ALMotion
 * @param robotSerialNumber  Die Seriennummer des Roboters
 * @param timestamp  Zeitstempel
 * @param filename
 * @param filetyp
 * @param file     Datei
 * @param content  Tags ( Bsp: augen, nachricht, [erleichtert die Suche])
 */
case class SaveFile(collection: String, robotSerialNumber: String, timestamp: Long, filename: String, filetyp: String, file: Array[Byte], content: Map[String, List[String]])

/**
 * Suchnachricht User->DBFacade
 * @param collection    Bsp: ALMotion
 * @param robotSerialNumber  Die Seriennummer des Roboters
 * @param timestampStart  Zeitstempel Anfang (suche "von" )
 * @param timestampEnd    Zeitstempel Ende   (suche "bis" )
 * @param content      Tags ( Bsp: augen, nachricht, [erleichtert die Suche])
 * @param origin
 */
case class SearchData(collection: Option[String], robotSerialNumber: Option[String], timestampStart: Option[Long], timestampEnd: Option[Long], content: Option[Map[String, List[String]]], origin: ActorRef)

/**
 * Suchnachricht User->DBFacade
 * @param collection    Bsp: ALMotion
 * @param robotSerialNumber   Die Seriennummer des Roboters
 * @param timestampStart     Zeitstempel Anfang (suche "von" )
 * @param timestampEnd       Zeitstempel Ende   (suche "bis" )
 * @param filetyp      Dateityp
 * @param filename   Dateiname
 * @param content    Tags ( Bsp: augen, nachricht, [erleichtert die Suche])
 * @param origin      Sender
 */
case class SearchFile(collection: Option[String], robotSerialNumber: Option[String], timestampStart: Option[Long], timestampEnd: Option[Long], filetyp: Option[String], filename: Option[String], content: Option[Map[String, List[String]]], origin: ActorRef)

//case class SearchFile(robotSerialNumber: String, timestampStart: Option[Long], timestampEnd: Option[Long], filetyp: Option[String], content: Option[Array[Byte]], origin: ActorRef)
/**
 * MongoDBActor -> DBFacade
 * @param dataList
 * @param origin
 */
case class ReceivedData(dataList: Try[List[Map[String, List[Any]]]], origin: ActorRef)

/**
 * MongoDBActor -> DBFacade
 * @param fileList  Liste mit Dateien
 * @param origin
 */
case class ReceivedFile(fileList: Try[List[(String, String, Long, Array[Byte])]], origin: ActorRef)

}

// communication between Agent and use-cases
package agentMessages {

import akka.actor.{ActorSelection, ActorRef}
import scala.util.Try

case object RobotSerialNumbers

case object DatabaseActors

case class ReceivedRobotSerialNumbers(rsnList: Array[String])

case class ReceivedDatabaseActors(commandAccess: ActorRef, fileAccess: ActorRef)

}

// communication between UCAFSID and use-cases
package userMessages {

import messages.dataTyps._
import naogateway.value.NaoMessages.Call

//case class SaveMovement(robotSerialNumber: String, timestamp: Long, movementCommand: String, argumentList: List[AnyVal], tagList: List[String] = Nil) extends TMovement
/**
 * ControlAgent -> DBFacade
 * @param robotSerialNumber   Die Seriennummer des Roboters
 * @param timestamp       Zeitstempel
 * @param naoCommand      Der Nao-Befehl der gespeichert werden soll
 * @param tagList         Liste von Tags (optional)
 */
case class SaveCommand(robotSerialNumber: String, timestamp: Long, naoCommand: Call, tagList: List[String] = Nil) extends TCall

/**
 * ControlAgent -> DBFacade
 * @param robotSerialNumber    Die Seriennummer des Roboters
 * @param timestamp      Zeitstempel
 * @param filename      Dateiname
 * @param filetyp       Dateityp
 * @param audioFile
 * @param tagList
 */
@Deprecated
case class SaveAudioFile(robotSerialNumber: String, timestamp: Long, filename: String, filetyp: String, audioFile: Array[Byte], tagList: List[String] = Nil) extends TFile

/**
 * ControlAgent -> DBFacade
 * @param robotSerialNumber
 * @param timestamp
 * @param filename
 * @param filetyp
 * @param videoFile
 * @param tagList
 */
@Deprecated
case class SaveVideoFile(robotSerialNumber: String, timestamp: Long, filename: String, filetyp: String, videoFile: Array[Byte], tagList: List[String] = Nil) extends TFile

/**
 * ControlAgent -> DBFacade
 * @param robotSerialNumber
 * @param timestamp
 * @param filename
 * @param filetyp
 * @param imageFile
 * @param tagList
 */
@Deprecated
case class SavePicture(robotSerialNumber: String, timestamp: Long, filename: String, filetyp: String, imageFile: Array[Byte], tagList: List[String] = Nil) extends TFile

//case class SearchMovement(robotSerialNumber: String, timestampStart: Option[Long] = None, timestampEnd: Option[Long] = None, commandList: Option[List[String]] = None, tagList: Option[List[String]] = None)
/**
 * User -> DBFacade
 * @param robotSerialNumber
 * @param collection
 * @param timestampStart
 * @param timestampEnd
 * @param tagList
 */
case class SearchCommand(robotSerialNumber: Option[String], collection: Option[String] = None, timestampStart: Option[Long] = None, timestampEnd: Option[Long] = None, tagList: Option[List[String]] = None)

/**
 * User -> DBFacade
 * @param robotSerialNumber
 * @param collection
 * @param timestampStart
 * @param timestampEnd
 * @param filename
 * @param filetyp
 * @param tagList
 */
@Deprecated
case class SearchAudioFile(robotSerialNumber: String, collection: Option[String] = None, timestampStart: Option[Long] = None, timestampEnd: Option[Long] = None, filename: Option[String] = None, filetyp: Option[String] = None, tagList: Option[List[String]] = None)

/**
 * User -> DBFacade
 * @param robotSerialNumber
 * @param collection
 * @param timestampStart
 * @param timestampEnd
 * @param filename
 * @param filetyp
 * @param tagList
 */
@Deprecated
case class SearchVideoFile(robotSerialNumber: String, collection: Option[String] = None, timestampStart: Option[Long] = None, timestampEnd: Option[Long] = None, filename: Option[String] = None, filetyp: Option[String] = None, tagList: Option[List[String]] = None)

/**
 * User -> DBFacade
 * @param robotSerialNumber
 * @param collection
 * @param timestampStart
 * @param timestampEnd
 * @param filename
 * @param filetyp
 * @param tagList
 */
@Deprecated
case class SearchPicture(robotSerialNumber: String, collection: Option[String] = None, timestampStart: Option[Long] = None, timestampEnd: Option[Long] = None, filename: Option[String] = None, filetyp: Option[String] = None, tagList: Option[List[String]] = None)

// For the Use-Case-Actors
//case class ReceivedMovement(movementList: Either[List[Movement], String])

//case class ReceivedCommand(commandTimestampTagList: Either[(List[Call], List[Long], List[String]), String])
/**
 *
 * @param commandTimestampTagList
 */
case class ReceivedCommand(commandTimestampTagList: Either[(List[(Call, Long, List[String])]), String])

@Deprecated
case class ReceivedAudioFile(audioFileList: Either[List[AudioFile], String])

@Deprecated
case class ReceivedVideoFile(videoFileList: Either[List[VideoFile], String])

@Deprecated
case class ReceivedPicture(imageList: Either[List[Picture], String])

}

package dataTyps {

import naogateway.value.NaoMessages.Call

// ----- Traits -----
sealed trait TMovement

sealed trait TCall

sealed trait TFile

// ----- Classes -----
//case class Movement(robotSerialNumber: String, timestamp: Long, movementCommand: String, argumentList: List[AnyVal], tagList: List[String]) extends TMovement

case class SavedCommand(robotSerialNumber: String, timestamp: Long, naoCommand: Call, tagList: List[String] = Nil) extends TCall

@Deprecated
case class AudioFile(robotSerialNumber: String, timestamp: Long, filename: String, filetyp: String, audioFile: Array[Byte], tagList: List[String]) extends TFile

@Deprecated
case class VideoFile(robotSerialNumber: String, timestamp: Long, filename: String, filetyp: String, videoFile: Array[Byte], tagList: List[String]) extends TFile

@Deprecated
case class Picture(robotSerialNumber: String, timestamp: Long, filename: String, filetyp: String, imageFile: Array[Byte], tagList: List[String]) extends TFile

}