package gui

import akka.actor._
import dbActors.DBConfigurator
import com.typesafe.config.ConfigFactory
import scala.swing._

/**
 * Startet die GUI und erzeugt ein Aktorensystem fuer die Datenbank und stellt die Verbindung zum Aktorensystem
 * des NAOGateways her
 */
object DBGui extends App {
  // ----- View und Model erstellen -----
  val gui = new DatabaseSwingGUI
  val model = new Model

  // ----- Dialog um den anzusprechenden Roboter zu waehlen -----
  var chooseRobot = 'DEFAULT
  val dialog_chooseRobot = new Dialog() {
    title = "Roboterauswahl"
    modal = true

    val button_Nila = Button("Nila") {
      chooseRobot = 'NILA
      close()
    }

    val button_Hanna = Button("Hanna") {
      chooseRobot = 'HANNA
      close()
    }

    contents = new BorderPanel {
      add(new Label("Roboter zum empfangen der Nachrichten:"), BorderPanel.Position.North)
      add(new FlowPanel() {
        contents += button_Nila
        contents += button_Hanna
        border = Swing.EmptyBorder(1, 5, 1, 5)
      }, BorderPanel.Position.Center)
      border = Swing.EmptyBorder(10, 10, 10, 10)
    }
    centerOnScreen()
    open()
  }

  // ----- Verbindung zum Naogateway -----
  val config = ConfigFactory.load()
  val system = ActorSystem("remoting", config.getConfig("remoting").withFallback(config))

  // ----- Auswahl des anzusprechenden Roboters -----
  val naoActor = chooseRobot match {
    case 'NILA => system.actorFor("akka://naogateway@192.168.1.100:2552/user/nila")
    case 'HANNA => system.actorFor("akka://naogateway@192.168.1.100:2550/user/hanna")
    case 'DEFAULT => system.actorFor("akka://naogateway@192.168.1.100:2552/user/nila")
  }

  // ----- Erstellen des Aktorensystems der Datenbank -----
  system.actorOf(Props[DBConfigurator], name = "DBConfigurator")
  val agent = system.actorFor("akka://remoting/user/DBConfigurator/DBAgent")

  // ----- Erstellung des Controller der GUI -----
  system.actorOf(Props().withCreator(new ControlActor(agent, naoActor, gui, model)), name = "GUIActor")
}