import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.VBox;
import javafx.util.converter.DefaultStringConverter;

import javax.swing.*;
import java.sql.SQLException;

public class Controller {
    @FXML
    private TableView<ObservableList<String>> tableView;

    @FXML
    private VBox sidebar;

    @FXML
    private ToggleButton toggleButton;

    @FXML
    private Button addColumnButt;

    @FXML
    private ComboBox<String> DBcomboBox;

    private Table handler = new Table(); //Table object, handles once instance of a table.


    public void init() throws SQLException {
        //Populate combobox
        DBcomboBox.getItems().setAll(DBcontroller.getDatabases());
    }


    //Adds new (blank) column
    @FXML
    private void addColumn() {
        int finalIdx = tableView.getColumns().size(); //Get current size of table
        TableColumn<ObservableList<String>, String> column = new TableColumn<>("Column" + (finalIdx + 1));
        handler.typeAdd(12); //Add to list of current view's columns
        handler.addColumn("Column" + (finalIdx + 1)); //New column

        //Populate with empty string
        for (ObservableList<String> row : tableView.getItems()) {
            row.add("");
        }

        // Make cells editable.
        column.setCellFactory(TextFieldTableCell.forTableColumn(new DefaultStringConverter()));
        column.setEditable(true);

        column.setOnEditCommit(event -> {
            ObservableList<String> rowData = event.getTableView().getItems().get(event.getTablePosition().getRow());
            rowData.set(finalIdx, event.getNewValue());
            ObservableList<String> selectedRow = (ObservableList<String>) tableView.getSelectionModel().getSelectedItem();
            handler.newEntry(selectedRow.get(0), event.getNewValue(), column.getText());
        });

        tableView.getColumns().add(column);
    }



    //Gets column names and entries
    public void updateTableView() throws SQLException {
        //Clear existing columns and entries

        tableView.getColumns().clear();
        tableView.getItems().clear();

        ObservableList<String> data = DBcontroller.getColumns();
        this.handler.setPK(DBcontroller.getIDColumn()); //Get the primary key column

        //For each column, add to table
        for (int i = 0; i < data.size(); i++) {
            int finalIdx = i;
            TableColumn<ObservableList<String>, String> column = new TableColumn<>(data.get(i).toString());
            column.setPrefWidth(100); //Set width
            //System.out.println(handler.IDs);
            handler.colInit(DBcontroller.getColumnTypes()); //Set data type to string


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
                handler.newEntry(selectedRow.get(0), event.getNewValue(), column.getText());
            });
            tableView.getColumns().add(column);
        }

        //Entries into table
        tableView.getItems().addAll(DBcontroller.getEntries());
        //String IDColumn = DBcontroller.getIDColumn();
        //handler.addRows(DBcontroller.getColumn(IDColumn));
        //handler.addRows(DBcontroller.getIDs());
    }


    @FXML
    private void saveToDatabase() throws SQLException {
        //SWITCH COMMENT TO SAVE TO DATABASE
        DBcontroller.saveToDatabase(handler.saveToDatabase());
        //handler.saveToDatabase();
    }

    @FXML
    private void addRow() {
        ObservableList<String> emptyRow = FXCollections.observableArrayList();

        //Empty string for eac column
        for (int i = 0; i < tableView.getColumns().size(); i++) {
            emptyRow.add("");
        }

        tableView.getItems().add(emptyRow);
        String ID = handler.addRow(); //Add row with arbuitary value as key.

        //Add primary key (by iterating tableView's columns)
        for (TableColumn<ObservableList<String>, ?> column : tableView.getColumns()) { //Javafx kinda sucks
            if (column.getText().equals(handler.getPK())) {
                emptyRow.set(tableView.getColumns().indexOf(column), ID);
            }
        }
    }
}
