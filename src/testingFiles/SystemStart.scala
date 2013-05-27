package testingFiles

import com.typesafe.config.ConfigFactory
import akka.actor.{Props, ActorSystem}
import dbActors.DBConfigurator

/**
 * Created with IntelliJ IDEA.
 * User: nao
 * Date: 27.05.13
 * Time: 08:44
 * To change this template use File | Settings | File Templates.
 */
object SystemStart extends App {

  val config = ConfigFactory.load()
  val system = ActorSystem("remoting", config.getConfig("remoting").withFallback(config))

  //val naoActor = system.actorFor("akka://naogateway@192.168.1.100:2550/user/hanna")

  // Create the Akka system
  // val system = ActorSystem("DBSystem")
  val configurator = system.actorOf(Props[DBConfigurator], name = "DBConfigurator")
  println(configurator)

}
