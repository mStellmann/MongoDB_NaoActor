package actors

import akka.actor._

package movementMessages {
  //generell für viele Befehle
  case class SaveMovement(time: Int, command: String, args: List[String])
  //oder spezifischer?
  case class SaveRotation(time: Int, angle: Int, speed: Int)

  case class FindMovementsInTime(from: Int, to: Int)
  case class FoundMovements(movs: List[String])
}

class MovementDBActor(naoID: String) extends Actor {
  import movementMessages._

  val collectionToSaveIn = "mov"
  val dbActor = context.actorFor("../mongoDB")

  override def receive = {

    case SaveMovement(time, command, args) => {
      println("MovSave")
      val content = Map(command -> args)
      dbActor ! dbMessages.Save(collectionToSaveIn, time, naoID, content)
    }

    case SaveRotation(time, angle, speed) => {
      println("MovSaveRot")
      val content = Map("rotiere" -> List(angle.toString, speed.toString))
      dbActor ! dbMessages.Save(collectionToSaveIn, time, naoID, content)
    }

    case FindMovementsInTime(from, to) => {
      println("MovFindInTime")
      dbActor ! dbMessages.FindInColumnBetweenT(collectionToSaveIn, naoID, "time", from, to, sender)
    }

    case dbMessages.FoundInColumnBetweenT(docs, client) => {
      //val movs = docs.convertItBackToSomethingMeanigful
      println("MovFound: " + docs)
      //client ! FoundMovements(Nil)
    }

    case anyThing => println("What's that?: " + anyThing)
  }
}