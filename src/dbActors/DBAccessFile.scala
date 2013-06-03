package dbActors

import akka.actor.Actor
import messages.userMessages._
import messages.userMessages.SaveVideoFile
import messages.userMessages.SaveAudioFile
import messages.userMessages.SavePicture
import messages.internalMessages.ReceivedFile
import messages.userMessages.SearchAudioFile

/**
 * This Actor provides the functionality to logging and reading files/data from a Nao-Robot.
 */


@Deprecated
class DBAccessFile extends Actor {
  def receive = {
    // TODO - ScalaDoc
    case SaveAudioFile(robotSerialNumber, timestamp, filename, filetyp, audioFile, tagList) => ??? // TODO

    // TODO - ScalaDoc
    case SaveVideoFile(robotSerialNumber, timestamp, filename, filetyp, videoFile, tagList) => ??? // TODO

    // TODO - ScalaDoc
    case SavePicture(robotSerialNumber: String, timestamp: Long, filename: String, filetyp: String, imageFile, tagList) => ??? // TODO

    // TODO - ScalaDoc
    case SearchAudioFile(robotSerialNumber: String, collections, timestampStart, timestampEnd, filename, filetyp, tagList) => ??? // TODO

    // TODO - ScalaDoc
    case SearchVideoFile(robotSerialNumber: String, collections, timestampStart, timestampEnd, filename, filetyp, tagList) => ??? // TODO

    // TODO - ScalaDoc
    case SearchPicture(collections, robotSerialNumber, timestampStart, timestampEnd, filename, filetyp, tagList) => ??? // TODO

    // TODO - ScalaDoc
    case ReceivedFile(fileList, origin) => ??? // TODO
  }
}