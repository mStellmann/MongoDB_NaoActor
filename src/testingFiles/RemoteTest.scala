package testingFiles

import akka.actor.ActorSystem
import akka.actor.Actor
import akka.event.Logging
import com.typesafe.config.ConfigFactory
import akka.actor.ActorRef
import akka.actor.Props
import naogateway.value.NaoMessages._
import naogateway.value.NaoMessages.Conversions._
import naogateway.value.NaoVisionMessages._

object RemoteTest extends App {


  val config = ConfigFactory.load()
  val system = ActorSystem("remoting", config.getConfig("remoting").withFallback(config))

  //    val naoActor = system.actorFor("akka://naogateway@192.168.1.100:2550/user/hanna")
  val naoActor = system.actorFor("akka://naogateway@192.168.1.100:2552/user/nila")

  system.actorOf(Props[MyResponseTestActor])

  class MyResponseTestActor extends Actor {
    override def preStart = naoActor ! Connect

    def receive = {
      case (response: ActorRef, noResponse: ActorRef, vision: ActorRef) => {
        trace(response)
        trace(noResponse)
        trace(vision)
        //        response ! Call('ALTextToSpeech, 'getVolume)
        //        response ! Call('ALTextToSpeech, 'getAvailableVoices)
        //        response ! Call('ALTextToSpeech, 'getVoice)
        //        noResponse ! Call('ALTextToSpeech, 'setVoice, List("Kenny22Enhanced"))
        //        response ! Call('ALTextToSpeech, 'getVoice)

        while (true) {
          noResponse ! Call('ALTextToSpeech, 'say, List(readLine()))
        }
        //        response ! Call('ALTextToSpeech, 'say, List("Stehen bleiben!"))
        //        vision ! VisionCall(Resolutions.k4VGA,ColorSpaces.kBGR,Frames._20)
        //        vision ! RawVisionCall(Resolutions.k4VGA,ColorSpaces.kBGR,Frames._20)
      }
      case x => trace(x)
    }

    def trace(a: Any) = log.info(a.toString)

    def error(a: Any) = log.warning(a.toString)

    def wrongMessage(a: Any, state: String) = log.warning("wrong message: " + a + " in " + state)

    val log = Logging(context.system, this)


  }

  Thread.sleep(2000)
  system.shutdown()
}