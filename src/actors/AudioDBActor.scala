package actors

import akka.actor.Actor

package audioMessages {

case class SaveAudioFile(timestamp: Int, name: String, audioFile: Array[Byte], comments: Map[String, List[String]])

case class FindAudioInTime(from: Int, to: Int)

case class FoundAudioFiles(movs: List[String])

}


/**
 *
 */
class AudioDBActor(naoID: String) extends Actor {

  import audioMessages._

  val collectionToSaveIn = "audio"
  val dbActor = context.actorFor("../mongoDB")

  override def receive = ???
}
