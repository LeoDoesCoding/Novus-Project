import javafx.animation.TranslateTransition;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.util.converter.DefaultStringConverter;

import java.sql.SQLException;
import java.util.List;

public class Controller {
    @FXML
    private TableView tableView;

    @FXML
    private VBox sidebar;

    @FXML
    private ToggleButton toggleButton;

    @FXML
    private Button addColumnButt;


    //Adds column to view
    /*public void addColumn() {
        TableColumn<ObservableList<StringProperty>, String> newColumn = new TableColumn<>("Column " + (tableView.getColumns().size() + 1));
        newColumn.setCellValueFactory(param -> param.getValue().get(tableView.getColumns().indexOf(newColumn)));

        newColumn.setCellFactory(TextFieldTableCell.forTableColumn(new DefaultStringConverter()));
        newColumn.setEditable(true);

        //Allows user to edit cell
        newColumn.setOnEditCommit(event -> {
            int row = event.getTablePosition().getRow();
            int col = tableView.getColumns().indexOf(newColumn);
            tableView.getItems().get(row).get(col).set(event.getNewValue());
        });

        tableView.getColumns().add(newColumn);
    }*/

    //Adds new (blank) column
    @FXML
    private void addColumn(ActionEvent event) {
        TableColumn<String, String> newColumn = new TableColumn<>("Column " + (tableView.getColumns().size() + 1));
        newColumn.setId("col" + tableView.getColumns().size() + 1);
        tableView.getColumns().add(newColumn);
        DataHandler.newColumn("col" + tableView.getColumns().size());
    }

    //Adds row to view
    /*public static void addRow(TableView<ObservableList<StringProperty>>) {
        ObservableList<StringProperty> row = FXCollections.observableArrayList();
        for (int i = 0; i < tableView.getColumns().size(); i++) {
            row.add(new SimpleStringProperty(""));
        }
        tableView.getItems().add(row);
    }*/


    //Set column names to table
    public void getColumnNames() {
        try {
            ObservableList<String> data = DBcontroller.getColumns();
            //For each column, add to table
            for (int i = 0; i < data.size(); i++) {
                int finalIdx = i;
                TableColumn<ObservableList<String>, String> column = new TableColumn<>(data.get(i).toString());

                //Factory (gets column data)
                column.setCellValueFactory(param -> {
                    String cellValue = param.getValue().get(finalIdx);
                    return new SimpleStringProperty(cellValue);
                });
                tableView.getColumns().add(column);
            }
        } catch (
                SQLException e) {
            e.printStackTrace();
        }
    }


    //Gets column names and entries
    public void updateTableView() throws SQLException {
        //Clear existing columns and entries
        tableView.getColumns().clear();
        tableView.getItems().clear();

        ObservableList<String> data = DBcontroller.getColumns();
        //For each column, add to table
        for (int i = 0; i < data.size(); i++) {
            int finalIdx = i;
            TableColumn<ObservableList<String>, String> column = new TableColumn<>(data.get(i).toString());

            //Factory (gets column data)
            column.setCellValueFactory(param -> {
                String cellValue = param.getValue().get(finalIdx);
                return new SimpleStringProperty(cellValue);
            });
            tableView.getColumns().add(column);

        }

        //Entries into table
        tableView.getItems().addAll(DBcontroller.getEntries());
    }
}
