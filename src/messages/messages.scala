package messages

// communication between MongoDBActor and "Use-Case-Actors-For-Saving-In-Database"(further DatabaseActor) (e.g. DBAccessFile)
package internalMessages {

  import akka.actor.ActorRef
  import scala.util.Try
  import naogateway.value.NaoMessages.Call

  case class Save(collection: String, robotSerialNumber: String, timestamp: Long, content: Map[String, List[Any]])

  case class SaveCommand(collection: String, robotSerialNumber: String, timestamp: Long, command: Call, content: Map[String, List[AnyVal]])

  case class SaveFile(collection: String, robotSerialNumber: String, timestamp: Long, filename: String, filetyp: String, file: Array[Byte], content: Map[String, List[String]])

  case class SearchData(collection: Option[String], robotSerialNumber: String, timestampStart: Option[Long], timestampEnd: Option[Long], content: Option[Map[String, List[String]]], origin: ActorRef)

  case class SearchFile(robotSerialNumber: String, timestampStart: Option[Long], timestampEnd: Option[Long], filetyp: Option[String], content: Option[Map[String, List[String]]], origin: ActorRef)

  case class ReceivedData(dataList: Try[List[Map[String, List[Any]]]], origin: ActorRef)

  case class ReceivedFile(fileList: Try[List[Map[String, List[Any]]]], origin: ActorRef)

}

// communication between Agent and use-cases
package agentMessages {

  import akka.actor.{ ActorSelection, ActorRef }
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

  case class SaveCommand(robotSerialNumber: String, timestamp: Long, naoCommand: Call, tagList: List[String] = Nil) extends TCall

  case class SaveAudioFile(robotSerialNumber: String, timestamp: Long, filename: String, filetyp: String, audioFile: Array[Byte], tagList: List[String] = Nil) extends TFile

  case class SaveVideoFile(robotSerialNumber: String, timestamp: Long, filename: String, filetyp: String, videoFile: Array[Byte], tagList: List[String] = Nil) extends TFile

  case class SavePicture(robotSerialNumber: String, timestamp: Long, filename: String, filetyp: String, imageFile: Array[Byte], tagList: List[String] = Nil) extends TFile

  //case class SearchMovement(robotSerialNumber: String, timestampStart: Option[Long] = None, timestampEnd: Option[Long] = None, commandList: Option[List[String]] = None, tagList: Option[List[String]] = None)

  case class SearchCommand(robotSerialNumber: String, collection: Option[String] = None, timestampStart: Option[Long] = None, timestampEnd: Option[Long] = None, commandList: Option[List[String]] = None, tagList: Option[List[String]] = None)

  case class SearchAudioFile(robotSerialNumber: String, collection: Option[String] = None, timestampStart: Option[Long] = None, timestampEnd: Option[Long] = None, filename: Option[String] = None, filetyp: Option[String] = None, tagList: Option[List[String]] = None)

  case class SearchVideoFile(robotSerialNumber: String, collection: Option[String] = None, timestampStart: Option[Long] = None, timestampEnd: Option[Long] = None, filename: Option[String] = None, filetyp: Option[String] = None, tagList: Option[List[String]] = None)

  case class SearchPicture(robotSerialNumber: String, collection: Option[String] = None, timestampStart: Option[Long] = None, timestampEnd: Option[Long] = None, filename: Option[String] = None, filetyp: Option[String] = None, tagList: Option[List[String]] = None)

  // For the Use-Case-Actors
  //case class ReceivedMovement(movementList: Either[List[Movement], String])

  case class ReceivedCommand(commandList: Either[List[Call], String])

  case class ReceivedAudioFile(audioFileList: Either[List[AudioFile], String])

  case class ReceivedVideoFile(videoFileList: Either[List[VideoFile], String])

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

  case class AudioFile(robotSerialNumber: String, timestamp: Long, filename: String, filetyp: String, audioFile: Array[Byte], tagList: List[String]) extends TFile

  case class VideoFile(robotSerialNumber: String, timestamp: Long, filename: String, filetyp: String, videoFile: Array[Byte], tagList: List[String]) extends TFile

  case class Picture(robotSerialNumber: String, timestamp: Long, filename: String, filetyp: String, imageFile: Array[Byte], tagList: List[String]) extends TFile

}