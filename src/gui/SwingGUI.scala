package gui

import scala.swing._
import scala.swing.event.ButtonClicked
import java.text.SimpleDateFormat
import java.util.Date
import java.awt.Dimension
import scala.swing.Dimension
import scala.collection.mutable.ListBuffer
import javax.swing.table.{AbstractTableModel, TableRowSorter, DefaultTableModel}
import javax.swing.JTable


class SwingGUI extends MainFrame {

  title = "Simple DB App for Robots"
  preferredSize = new Dimension(1000, 800)
  resizable = false

  val panel_cBoxChooseRobot = new FlowPanel()
  val panel_cBoxCommands = new FlowPanel()
  val button_search = new Button("Los, suchen!")
  val button_sendToNao = new Button("Back to NAO") {
    enabled = false
  }

  val cBox_starttime = new CheckBox
  val cBox_endtime = new CheckBox
  val textField_tags = new TextField {
    preferredSize = new Dimension(300, 18)

    minimumSize = new Dimension(300, 18)
    maximumSize = new Dimension(300, 18)
  }

  // ----- Datums-Felder -----
  val ftxtField_starttime = new FormattedTextField(new SimpleDateFormat("dd.MM.yyyy HH:mm:ss"))
  ftxtField_starttime.peer.setValue(new Date())
  ftxtField_starttime.enabled = false;
  val ftxtField_endtime = new FormattedTextField(new SimpleDateFormat("dd.MM.yyyy HH:mm:ss"))
  ftxtField_endtime.peer.setValue(new Date())
  ftxtField_endtime.enabled = false;

  val controlPanel = new GridBagPanel {
    add(panel_cBoxChooseRobot, (0, 0))
    add(panel_cBoxCommands, (0, 1))
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

  val tableModel = new MyTableModel(new Array[Array[AnyRef]](0), Array[AnyRef]("Command", "Content", "Timestamp", "Tags"))

  val table_commandList = new Table() {
    model = tableModel
    selection.intervalMode = Table.IntervalMode.MultiInterval
  }


  val scrollPane_CommandList = new ScrollPane {
    contents = table_commandList
  }


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


class MyTableModel(model: Array[Array[AnyRef]], columnNames: Array[AnyRef]) extends DefaultTableModel(model, columnNames) {
  override def isCellEditable(row: Int, column: Int) = false
}
