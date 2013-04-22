package actors

import akka.actor.Actor

package audioMessages {

case class SaveAudioFile(timestamp: Int, name: String, audioFile: Array[Byte], comments: List[String])

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
