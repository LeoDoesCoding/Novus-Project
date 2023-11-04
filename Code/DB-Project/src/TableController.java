import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.DefaultStringConverter;

import java.sql.SQLException;

public class TableController {
    @FXML
    private TableView<ObservableList<String>> tableView;

    protected Table handler; //Table object, handles once instance of a table.


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
    public void updateTableView(String dbName, String tableName) throws SQLException {
        //Set DB and table name
        this.handler = new Table(dbName, tableName);

        //Clear existing columns and entries
        tableView.getColumns().clear();
        tableView.getItems().clear();

        ObservableList<String> data = DBcontroller.getColumns(handler.getTable());
        this.handler.setPK(DBcontroller.getIDColumn(handler.getTable())); //Get the primary key column
        handler.colInit(DBcontroller.getColumnTypes(handler.getTable())); //Set data type to string


        //For each column, add to table
        for (int i = 0; i < data.size(); i++) {
            int finalIdx = i;
            TableColumn<ObservableList<String>, String> column = new TableColumn<>(data.get(i).toString());
            column.setPrefWidth(100); //Set width


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
        tableView.getItems().addAll(DBcontroller.getEntries(handler.getTable()));
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



    //TABLE METHODS
    //Check for changes to table
    public boolean isModified() {
        if (handler.isModified()) {
            return true;
        } else { return false; }
    }
}
