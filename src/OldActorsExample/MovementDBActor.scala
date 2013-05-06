package OldActorsExample

import akka.actor._

package movementMessages {

//generell fuer viele Befehle
case class SaveMovement(time: Int, command: String, args: List[String], tags: List[String])

//oder spezifischer?
case class SaveRotation(time: Int, angle: Int, speed: Int)

case class FindMovementsInTime(from: Int, to: Int)

case class FoundMovements(command: String, movs: List[String])

}

class MovementDBActor(naoID: String) extends Actor {

  import movementMessages._

  val collectionToSaveIn = "mov"
  val dbActor = context.actorFor("../mongoDB")

  override def receive = {

    case SaveMovement(time, command, args, tags) => {
      println("MovSave")
      val content = Map("command" -> List(command), "args" -> args)
      dbActor ! dbMessages.Save(collectionToSaveIn, time, naoID, content ++ Map("tags" -> tags))
    }

    case FindMovementsInTime(from, to) => {
      println("MovFindInTime")
      dbActor ! dbMessages.FindInColumnBetweenT(collectionToSaveIn, naoID, "time", from, to, sender)
    }

    // TODO find(rotiere) .... tag

    case dbMessages.FoundInColumnBetweenT(docs, client) => {
      //val movs = docs.convertItBackToSomethingMeanigful
      println("MovFound: " + docs)
      //client ! FoundMovements(Nil)
    }

    case anyThing => println("What's that?: " + anyThing)
  }
}