package dbActors

import akka.actor.Actor

/**
 * This actor initializes our DBActor-System and supervises the own actor-childs.
 * First of all it creates the MongoDBActor to providing the communication with the Database.
 * Afterwards the DBAgent will be created, which provides the user->system-communication.
 */
class DBConfigurator extends Actor {
  /*  Anmerkungen:
  aus der CFG muss die datenbank connection gelesen werden
  dbClient beim erstellen des mongodbactors erstellen?? dependency injection..
  */

  def receive = {
    ??? // TODO
  }
}
