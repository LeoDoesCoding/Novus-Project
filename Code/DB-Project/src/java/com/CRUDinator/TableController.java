package com.CRUDinator;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.DefaultStringConverter;

import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;

//Controller for each TableView
public class TableController {
    @FXML
    private TableView<ObservableList<String>> tableView;

    protected Table handler; //Table object, handles once instance of a table.


    //Adds new (blank) column
    @FXML
    private void addColumn() {
        int finalIdx = tableView.getColumns().size(); //Get current size of table
        TableColumn<ObservableList<String>, String> column = new TableColumn<>("Column" + (finalIdx + 1));
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
            ObservableList<String> selectedRow = tableView.getSelectionModel().getSelectedItem();
            handler.newEntry(selectedRow.get(0), event.getNewValue(), column.getText());
        });

        tableView.getColumns().add(column);
    }


    //Gets column names and entries
    public void loadTable(String tableName) throws SQLException {
        //Set DB and table name
        this.handler = new Table(tableName);

        //Clear existing columns and entries
        tableView.getColumns().clear();
        tableView.getItems().clear();

        ObservableList<String> data = DBcontroller.getColumns(handler.getTable());
        this.handler.setColIDs(data);
        this.handler.setPK(DBcontroller.getIDColumn(handler.getTable())); //Get the primary key column


        //For each column, add to table
        for (int i = 0; i < data.size(); i++) {
            int finalIdx = i;
            AtomicBoolean isInitialLoad = new AtomicBoolean(true);
            TableColumn<ObservableList<String>, String> column = new TableColumn<>(data.get(i));
            column.setPrefWidth(100);

            //Factory (gets column data)
            column.setCellValueFactory(param -> {
                String cellValue = param.getValue().get(finalIdx);
                return new SimpleStringProperty(cellValue);
            });


            //Make cells editable.
            column.setCellFactory(col -> new TextFieldTableCell<ObservableList<String>, String>(new DefaultStringConverter()) {
                @Override
                public void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);

                    if (!isInitialLoad.get() == true) {
                        setText(item);
                        if (isValid(item, column.getText())) { //Valid entry
                            handler.newEntry(getTableRow().getItem().get(0), item, column.getText());
                            setStyle("-fx-border-color: #EDEDED; -fx-border-width: 1;");
                        } else { //Invalid entry
                            handler.badEntry(getTableRow().getItem().get(0), item, column.getText());
                            setStyle("-fx-border-color: red; -fx-border-width: 2;");
                        }
                    }
                }
            });
            column.setEditable(true);


            column.setOnEditCommit(event -> {
                isInitialLoad.set(false);
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


    //Checks entry and if it is valid according to column rules
    public boolean isValid(String data, String colName) {
        //Get column type (either by hanlder if stored, or by database)
        int type;
        if (handler.isPresent(colName)) {
            type = handler.getType(colName);
            //System.out.println(tableView.getColumns().get(colInd).getText());
        } else {
            type = DBcontroller.getColumnType(handler.getTable(), colName);
        }

        switch (type) {
            case 4: //Is Integer
                try {
                    Integer.parseInt(data);
                } catch (NumberFormatException e) {
                    return false;
                } //Is not an integer
                break;
            default:
                break;
        }
        return true;
    }


    //TABLE METHODS
    //Check for changes to table
    public boolean isModified() {
        if (handler.isModified()) {
            return true;
        } else {
            return false;
        }
    }

    public boolean invalidsPresent() {
        if (handler.invalidsPresent()) {
            return true;
        } else {
            return false;
        }
    }
}
