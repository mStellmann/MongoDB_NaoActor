package gui

import scala.swing._

import java.text.SimpleDateFormat
import java.util.Date
import scala.swing.Dimension
import javax.swing.table.DefaultTableModel
import javax.swing.UIManager

/**
 * Scala Swing GUI um die Datenbankfunktionalitaet abzubilden
 */
class DatabaseSwingGUI extends MainFrame {
  // ----- Optionsfelder des MainFrames -----
  title = "Simple DB App for Robots"
  preferredSize = new Dimension(1000, 800)
  resizable = false
  UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName)

  // ----- Auswahlfelder fuer die Wahl des Roboters und des Befehls -----
  val panel_cBoxChooseRobot = new FlowPanel()
  val panel_cBoxCommands = new FlowPanel()

  // ----- Buttons -----
  val button_search = new Button("Los, suchen!")
  val button_sendToRobot = new Button("Back to Robot") {
    enabled = false
  }

  // ----- Textfeld fuer die Tagsuche -----
  val textfield_tags = new TextField {
    preferredSize = new Dimension(400, 30)
    minimumSize = new Dimension(400, 30)
    maximumSize = new Dimension(400, 30)
    tooltip = "einzelne Elemente trennen durch: ',' oder  ';'"
  }

  // ----- Datumsfelder und deren Checkboxen -----
  val cBox_starttime = new CheckBox
  val ftxtField_starttime = new FormattedTextField(new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")) {
    peer.setValue(new Date())
    enabled = false
  }

  val cBox_endtime = new CheckBox
  val ftxtField_endtime = new FormattedTextField(new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")) {
    peer.setValue(new Date())
    enabled = false
  }

  // ----- HeaderPanel zum einstellen der Suchparameter -----
  val panel_header = new BoxPanel(Orientation.Vertical) {
    contents += new GridBagPanel {
      add(panel_cBoxChooseRobot, (0, 0))
      add(panel_cBoxCommands, (0, 1))
      add(cBox_starttime, (1, 0))
      add(cBox_endtime, (1, 1))
      add(new Label("Choose StartTime"), (2, 0))
      add(ftxtField_starttime, (3, 0))
      add(new Label("Choose EndTime"), (2, 1))
      add(ftxtField_endtime, (3, 1))
      add(button_search, (4, 0))
      add(button_sendToRobot, (4, 1))
      border = Swing.EmptyBorder(10, 5, 2, 5)
    }
    contents += new FlowPanel {
      contents += new Label("Tags: ")
      contents += textfield_tags
    }
  }

  // ----- Tabelle fuer die Darstellung der Suchergebnisse -----
  val tableModel = new MyTableModel(new Array[Array[AnyRef]](0), Array[AnyRef]("Command", "Content", "Timestamp", "Tags"))
  val table_commandList = new Table() {
    model = tableModel
    selection.intervalMode = Table.IntervalMode.MultiInterval
  }

  // ----- Hauptpanel des Mainframes -----
  contents = new BorderPanel {
    add(panel_header, BorderPanel.Position.North)
    add(new ScrollPane {
      contents = table_commandList
    }, BorderPanel.Position.Center)
  }

}

/**
 * Erweiterung des DefaultTableModel um das editieren einzelner Zellen zu unterbinden
 * @param model Daten der Tabelle
 * @param columnNames Spaltennamen der Tabelle
 */
class MyTableModel(model: Array[Array[AnyRef]], columnNames: Array[AnyRef]) extends DefaultTableModel(model, columnNames) {
  override def isCellEditable(row: Int, column: Int) = false
}
