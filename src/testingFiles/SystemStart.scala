package testingFiles

import com.typesafe.config.ConfigFactory
import akka.actor.{Props, ActorSystem}
import dbActors.DBConfigurator

/**
 * Startet unser gesamtes System
 * VORHER muss jedoch eine lokale MongoDB gestartet werden
 * Und die IP in der application.conf angepasst werden
 */
object SystemStart extends App {

  val config = ConfigFactory.load()
    
  //TODO Config anpassen so, dass anfragen von extern beantwortet werden
  val system = ActorSystem("remoting", config.getConfig("remoting").withFallback(config))

  val configurator = system.actorOf(Props[DBConfigurator], name = "DBConfigurator")
  println(configurator)
}
