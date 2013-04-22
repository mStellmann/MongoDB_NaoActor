package actors

import akka.actor.Actor

package videoMessages {

case class SaveVideoFile(timestamp: Int, name: String, videoFile: Array[Byte], comments: List[String])

// save Picture ?!?!

}

/**
 *
 */
class VideoDBActor extends Actor {

  import videoMessages._

  val collectionToSaveIn = "video"
  val dbActor = context.actorFor("../mongoDB")

  override def receive = ???
}
