package actors

import akka.actor.Actor

package videoMessages {

case class SaveVideoFile(timestamp: Int, name: String, videoFile: Array[Byte], comments: Map[String, List[String]])

case class SavePictureFile(timestamp: Int, name: String, pictureFile: Array[Byte], comments: Map[String, List[String]])

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
