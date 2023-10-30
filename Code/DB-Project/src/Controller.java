import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.VBox;
import javafx.util.converter.DefaultStringConverter;

import java.sql.SQLException;
import java.util.ArrayList;

public class Controller {
    @FXML
    private TableView tableView;

    @FXML
    private VBox sidebar;

    @FXML
    private ToggleButton toggleButton;

    @FXML
    private Button addColumnButt;
    private ArrayList<Integer> columnDatas = new ArrayList<Integer>(); //columns data types


    //Adds new (blank) column
    @FXML
    private void addColumn() {
        TableColumn<String, String> newColumn = new TableColumn<>("Column " + (tableView.getColumns().size() + 1));
        newColumn.setId("col" + tableView.getColumns().size() + 1);
        columnDatas.add(12); //Set data type to string

        newColumn.setCellFactory(TextFieldTableCell.forTableColumn(new DefaultStringConverter()));
        newColumn.setEditable(true);

        //Allows user to edit cell
        newColumn.setOnEditCommit(event -> {
            int row = event.getTablePosition().getRow();
            int col = tableView.getColumns().indexOf(newColumn);
            ObservableList<String> rowData = (ObservableList<String>) tableView.getItems().get(row);
            rowData.set(col, event.getNewValue());
        });


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
            columnDatas=DBcontroller.getColumnTypes(); //Set data type to string

            //Factory (gets column data)
            column.setCellValueFactory(param -> {
                String cellValue = param.getValue().get(finalIdx);
                return new SimpleStringProperty(cellValue);
            });


            //Make cells editable.
            column.setCellFactory(TextFieldTableCell.forTableColumn(new DefaultStringConverter()));
            column.setEditable(true);

            column.setOnEditCommit(event -> {
                ObservableList<String> rowData = event.getTableView().getItems().get(event.getTablePosition().getRow());
                rowData.set(finalIdx, event.getNewValue());
                ObservableList<String> selectedRow = (ObservableList<String>) tableView.getSelectionModel().getSelectedItem();
                DataHandler.newEntry(selectedRow.get(0), event.getNewValue(), columnDatas.get(event.getTablePosition().getColumn()), column.getText());
            });
            tableView.getColumns().add(column);
        }

        //Entries into table
        tableView.getItems().addAll(DBcontroller.getEntries());
    }
}
