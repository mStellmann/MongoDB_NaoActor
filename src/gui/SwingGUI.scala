package gui

import scala.swing._
import scala.swing.event.ButtonClicked
import java.text.SimpleDateFormat
import java.util.Date
import java.awt.Dimension
import scala.swing.Dimension
import scala.collection.mutable.ListBuffer


class SwingGUI extends MainFrame {

  title = "Simple DB App for Robots"
  preferredSize = new Dimension(1000, 800)
  resizable = false

  val commandMap = Map("All Commands" -> "None", "Text to Speech" -> "ALTextToSpeech")

  val panel_cBoxChooseRobot = new FlowPanel()
  val cBox_commands = new ComboBox(commandMap.keys.toList)
  val button_search = new Button("Los, suchen!")
  val button_sendToNao = new Button("Back to NAO")
  // TODO Einbauen..
  val cBox_starttime = new CheckBox
  val cBox_endtime = new CheckBox
  val textField_tags = new TextField


  val ftxtField_starttime = new FormattedTextField(new SimpleDateFormat("dd.MM.yyyy HH:mm:ss"))
  ftxtField_starttime.peer.setValue(new Date())
  ftxtField_starttime.enabled = false;
  val ftxtField_endtime = new FormattedTextField(new SimpleDateFormat("dd.MM.yyyy HH:mm:ss"))
  ftxtField_endtime.peer.setValue(new Date())
  ftxtField_endtime.enabled = false;

  val controlPanel = new GridBagPanel {
    add(panel_cBoxChooseRobot, (0, 0))
    add(cBox_commands, (0, 1))
    add(cBox_starttime, (1, 0))
    add(cBox_endtime, (1, 1))
    add(new Label("Choose StartTime"), (2, 0))
    add(ftxtField_starttime, (3, 0))
    add(new Label("Choose EndTime"), (2, 1))
    add(ftxtField_endtime, (3, 1))
    add(button_search, (4, 0))
    add(button_sendToNao, (4, 1))
    border = Swing.EmptyBorder(10)

    add(new Label("Tags: "), (0, 2))
    add(textField_tags, (1, 2))
  }

  //  val list_commandList = new ListView(List[String]())
  val list_commandList = new ListView[String]() {
    listData = ListBuffer()
  }


  val scrollPane_CommandList = new ScrollPane()
  scrollPane_CommandList.contents = list_commandList


  val mainPanel = new BorderPanel {
    add(controlPanel, BorderPanel.Position.North)
    add(scrollPane_CommandList, BorderPanel.Position.Center)
  }


  // ------------ MENU ------------
  menuBar = new MenuBar
  val fileMenu = new Menu("File")
  val exitItem = new MenuItem(Action("Exit") {
    dispose()
    System.exit(0)
  })
  menuBar.contents += fileMenu
  fileMenu.contents += exitItem
  //  -----------------------------


  contents = mainPanel
}


//class DatePanel extends FlowPanel {
//
//
//  //  val test = tf1.peer.getValue().asInstanceOf[Date]
//  //  println(test.getTime)
//
//  contents += formTextfield
//}