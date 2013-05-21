package gui

import scala.swing._
import scala.swing.event.ButtonClicked
import java.text.SimpleDateFormat
import java.util.Date


class SwingGUI extends Frame {
  title = "Simple DB App for Robots"
  preferredSize = new Dimension(1000, 800)
  resizable = false

  val commandMap = Map("All Commands" -> "None", "Text to Speech" -> "ALTextToSpeech")

  val panel_cBoxChooseRobot = new FlowPanel()
  val cBox_commands = new ComboBox(commandMap.keys.toList)
  val button_search = new Button("Los, suchen!")
  val button_sendToNao = new Button("Back to NAO") // TODO Einbauen..
  // ---------- init() ----------


  val mainPanel = new GridBagPanel {
    val gbc = new Constraints()
    gbc.grid = (0, 0)
    add(panel_cBoxChooseRobot, gbc)
    gbc.grid = (0, 1)
    add(new Label("Choose StartTime"), gbc)
    gbc.grid = (1, 1)
    add(new DatePanel, gbc)
    gbc.grid = (0, 2)
    add(new Label("Choose StartTime"), gbc)
    gbc.grid = (1, 2)
    add(new DatePanel, gbc)
    gbc.grid = (1, 0)
    add(cBox_commands, gbc)
    gbc.grid = (0, 3)
    add(button_search, gbc)
    gbc.grid = (1, 3)
    add(button_sendToNao, gbc)

  }
  contents = mainPanel
}


class DatePanel extends FlowPanel {
  val formTextfield = new FormattedTextField(new SimpleDateFormat("dd.MM.yyyy HH:mm:ss"))
  formTextfield.peer.setValue(new Date())

  //  val test = tf1.peer.getValue().asInstanceOf[Date]
  //  println(test.getTime)

  contents += formTextfield
}