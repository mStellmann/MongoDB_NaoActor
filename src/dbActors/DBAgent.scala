package dbActors

import akka.actor.Actor

/**
 * This actor acts as an agent between the user and our DBActor-System.
 * It starts and supervises the communication-actors.
 *
 * The user can request all robotIDs or a specific communication-actor (e.g. DBAccessFile).
 */
class DBAgent extends Actor{
  def receive = {
    ??? // TODO
  }
}
