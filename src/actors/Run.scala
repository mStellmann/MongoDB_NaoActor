package actors

import akka.actor._
import com.mongodb.casbah.Imports._

object Run {

  def main(args: Array[String]) {
    val system = ActorSystem("DBSystem")

    val dbActor = system.actorOf(Props[MongoDBActor], name = "mongoDB")
    val movActor = system.actorOf(Props(new MovementDBActor("hanna")), name = "movHannaToDBActor")

    import movementMessages._
    movActor ! SaveMovement(2, "rotiere", List("3", "2"), List("Hallo", "Dieter", "Taggy Tag"))
    movActor ! SaveMovement(1, "rotiere", List("45", "2"), Nil)
    movActor ! SaveRotation(3, 47, 2)

    Thread.sleep(500)
    movActor ! FindMovementsInTime(0, 5)

    //import dbMessages._
    //dbActor ! Save("mov", 1, "hanna", Map("tags" -> List("kuehlschrank"), "move" -> List("3m")))
    //dbActor ! FindInColumnBetween("mov", "hanna", "time", 0, 5)

    Thread.sleep(1500)
    system.shutdown

    //CleanUp DB
    MongoClient()("mov")("hanna").drop
  }

}