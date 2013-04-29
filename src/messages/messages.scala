package messages


// communication between MongoDBActor and "Use-Case-Actors-For-Saving-In-Database"(further UCAFSID) (e.g. DBAccessFile)
package internalMessages {

import akka.actor.ActorRef
import scala.util.Try

case class Save(collection: String, RobotSerialNumber: String, timestamp: Long, content: Map[String, List[String]])

case class SaveFile(collection: String, RobotSerialNumber: String, timestamp: Long, filename: String, filetyp: String, file: Array[Byte], content: Map[String, List[String]])

case class SearchData(collection: String, RobotSerialNumber: String, timestampStart: Option[Long], timestampEnd: Option[Long], content: Option[Map[String, List[String]]], origin: ActorRef)

case class SearchFile(collection: String, RobotSerialNumber: String, timestampStart: Option[Long], timestampEnd: Option[Long], filetyp: Option[String], content: Option[Map[String, List[String]]], origin: ActorRef)

case class RetrievedData(dataList: Try[List[Map[String, List[AnyRef]]]], origin: ActorRef)

case class RetrievedFile(fileList: Try[List[Map[String, List[AnyRef]]]], origin: ActorRef)

}

// communication between Agent and use-cases
package agentMessages {

import akka.actor.ActorRef
import scala.util.Try

case object RobotSerialNumbers

//case class GetUCAFSIDs(databaseActorTyp: String)

case class DatabaseActor(databaseActorTyp: String)

case class RetrievedRobotSerialNumbers(rsnList: List[String])

//case class RetreivedUCAFSIDs(databaseActorRef: Try[ActorRef])

case class RetrievedDatabaseActors(databaseActorRef: Try[ActorRef])

}

// communication between UCAFSID and use-cases
package userMessages {

import messages.dataTyps._

case class SaveMovement(robotSerialNumber: String, timestamp: Long, movementCommand: String, argumentList: List[String], tagList: List[String] = Nil) extends TMovement

case class SaveAudioFile(robotSerialNumber: String, timestamp: Long, filename: String, filetyp: String, audioFile: Array[Byte], tagList: List[String] = Nil) extends TFile

case class SaveVideoFile(robotSerialNumber: String, timestamp: Long, filename: String, filetyp: String, videoFile: Array[Byte], tagList: List[String] = Nil) extends TFile

case class SavePicture(robotSerialNumber: String, timestamp: Long, filename: String, filetyp: String, imageFile: Array[Byte], tagList: List[String] = Nil) extends TFile

case class SearchMovement(robotSerialNumber: String, timestampStart: Option[Long] = None, timestampEnd: Option[Long] = None, commandList: Option[List[String]] = None, tagList: Option[List[String]] = None)

case class SearchAudioFile(robotSerialNumber: String, timestampStart: Option[Long] = None, timestampEnd: Option[Long] = None, filename: Option[String] = None, filetyp: Option[String] = None, tagList: Option[List[String]] = None)

case class SearchVideoFile(robotSerialNumber: String, timestampStart: Option[Long] = None, timestampEnd: Option[Long] = None, filename: Option[String] = None, filetyp: Option[String] = None, tagList: Option[List[String]] = None)

case class SearchPicture(robotSerialNumber: String, timestampStart: Option[Long] = None, timestampEnd: Option[Long] = None, filename: Option[String] = None, filetyp: Option[String] = None, tagList: Option[List[String]] = None)

case class RetrievedMovement(movementList: Either[List[Movement], String])

case class RetrievedAudioFile(audioFileList: Either[List[AudioFile], String])

case class RetrievedVideoFile(videoFileList: Either[List[VideoFile], String])

case class RetrievedPicture(imageList: Either[List[Picture], String])

}

package dataTyps {

// ----- Traits -----
sealed trait TMovement

sealed trait TFile

// ----- Classes -----
case class Movement(robotSerialNumber: String, timestamp: Long, movementCommand: String, argumentList: List[String], tagList: List[String]) extends TMovement

case class AudioFile(robotSerialNumber: String, timestamp: Long, filename: String, filetyp: String, audioFile: Array[Byte], tagList: List[String]) extends TFile

case class VideoFile(robotSerialNumber: String, timestamp: Long, filename: String, filetyp: String, videoFile: Array[Byte], tagList: List[String]) extends TFile

case class Picture(robotSerialNumber: String, timestamp: Long, filename: String, filetyp: String, imageFile: Array[Byte], tagList: List[String]) extends TFile

}