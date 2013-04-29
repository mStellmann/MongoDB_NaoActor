package messages


// communication between MongoDBActor and "Use-Case-Actors-For-Saving-In-Database"(further UCAFSID) (e.g. DBAccessFile)
package internalMessages {

import akka.actor.ActorRef
import scala.util.Try

case class Save(collection: String, RobotSerialNumber: String, timestamp: Long, content: Map[String, List[String]])

case class SaveFile(collction: String, RobotSerialNumber: String, timestamp: Long, filename: String, filetyp: String, file: Array[Byte], content: Map[String, List[String]])

case class GetData(collection: String, RobotSerialNumber: String, timestampStart: Option[Long], timestampEnd: Option[Long], content: Option[Map[String, List[String]]], origin: ActorRef)

case class GetFile(collection: String, RobotSerialNumber: String, timestampStart: Option[Long], timestampEnd: Option[Long], filetyp: Option[String], content: Option[Map[String, List[String]]], origin: ActorRef)

case class RetreiveData(dataList: Try[List[Map[String, List[AnyRef]]]], origin: ActorRef)

case class RetreiveFile(fileList: Try[List[Map[String, List[AnyRef]]]], origin: ActorRef)

}

// communication between Agent and use-cases
package agentMessages {

import akka.actor.ActorRef
import scala.util.Try

case class GetRobotSerialNumbers()

case class GetUCAFSIDs(databaseActorTyp: String)

case class GetDatabaseActors(databaseActorTyp: String)

case class RetreivedRobotSerialNumbers(rsnList: List[String])

case class RetreivedUCAFSIDs(databaseActorRef: Try[ActorRef])

case class RetreivedDatabaseActors(databaseActorRef: Try[ActorRef])

}

// communication between UCAFSID and use-cases
package userMessages {

import messages.dataTyps._

case class SaveMovement(robotSerialNumber: String, timestamp: Long, movementCommand: String, argumentList: List[String], tagList: List[String]) extends TMovement

case class SaveAudioFile(robotSerialNumber: String, timestamp: Long, filename: String, filetyp: String, audioFile: Array[Byte], tagList: List[String]) extends TFile {
  override val file = audioFile
}

case class SaveVideoFile(robotSerialNumber: String, timestamp: Long, filename: String, filetyp: String, videoFile: Array[Byte], tagList: List[String]) extends TFile {
  override val file = videoFile
}

case class SavePicture(robotSerialNumber: String, timestamp: Long, filename: String, filetyp: String, imageFile: Array[Byte], tagList: List[String]) extends TFile {
  override val file = imageFile
}

case class GetMovement(robotSerialNumber: String, timestampStart: Option[Long] = None, timestampEnd: Option[Long] = None, commandList: Option[List[String]] = None, tagList: Option[List[String]] = None)

case class GetAudioFile(robotSerialNumber: String, timestampStart: Option[Long] = None, timestampEnd: Option[Long] = None, filename: Option[String] = None, filetyp: Option[String] = None, tagList: Option[List[String]] = None)

case class GetVideoFile(robotSerialNumber: String, timestampStart: Option[Long] = None, timestampEnd: Option[Long] = None, filename: Option[String] = None, filetyp: Option[String] = None, tagList: Option[List[String]] = None)

case class GetPicture(robotSerialNumber: String, timestampStart: Option[Long] = None, timestampEnd: Option[Long] = None, filename: Option[String] = None, filetyp: Option[String] = None, tagList: Option[List[String]] = None)

case class RetreivedMovement(movementList: Either[List[Movement], String])

case class RetreivedAudioFile(audioFileList: Either[List[AudioFile], String])

case class RetreivedVideoFile(videoFileList: Either[List[VideoFile], String])

case class RetreivedPicture(imageList: Either[List[Picture], String])

}

package dataTyps {

// ----- Traits -----
sealed trait TMovement {
  val robotSerialNumber: String
  val timestamp: Long
  val movementCommand: String
  val argumentList: List[String]
  val tagList: List[String]
}

sealed trait TFile {
  val robotSerialNumber: String
  val timestamp: Long
  val filename: String
  val filetyp: String
  val file: Array[Byte]
  val tagList: List[String]
}

// ----- Classes -----
case class Movement(robotSerialNumber: String, timestamp: Long, movementCommand: String, argumentList: List[String], tagList: List[String]) extends TMovement

case class AudioFile(robotSerialNumber: String, timestamp: Long, filename: String, filetyp: String, audioFile: Array[Byte], tagList: List[String]) extends TFile {
  override val file = audioFile
}

case class VideoFile(robotSerialNumber: String, timestamp: Long, filename: String, filetyp: String, videoFile: Array[Byte], tagList: List[String]) extends TFile {
  override val file = videoFile
}

case class Picture(robotSerialNumber: String, timestamp: Long, filename: String, filetyp: String, imageFile: Array[Byte], tagList: List[String]) extends TFile {
  override val file = imageFile
}

}