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

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

//Controller for each TableView
public class TableController {
    @FXML private TableView<ObservableList<String>> tableView;
    private Scanner myscanner = new Scanner(System.in);

    protected Table handler; //Table object, handles once instance of a table.


    //Adds new (blank) column
    @FXML private void addColumn() throws SQLException {
        String columnName = "Column" + tableView.getColumns().size(); //Get current size of table
        handler.addColumn(columnName);
        addColumnHandler(columnName);

        for (ObservableList<String> row : tableView.getItems()) {
            row.add("");
        }
    }


    //Loads table view
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
            addColumnHandler(data.get(i));
        }

        //Entries into table
        System.out.println("Get DB entries. . . ");
        tableView.getItems().addAll(DBcontroller.getEntries(handler.getTable()));
    }



    //Adds column (called by loadTable and addColumn)
    private void addColumnHandler(String name) throws SQLException {
        final String[] debug = new String[1];
        int finalIdx = tableView.getColumns().size();
        AtomicBoolean reloadReady = new AtomicBoolean(false);
        TableColumn<ObservableList<String>, String> column = new TableColumn<>(name);
        column.setPrefWidth(100);

        //Factory (gets column data)
        column.setCellValueFactory(param -> {
            String cellValue = param.getValue().get(finalIdx);
            return new SimpleStringProperty(cellValue);
        });


        //When cell is updated, validate it
        column.setCellFactory(col -> new TextFieldTableCell<>(new DefaultStringConverter()) {
            @Override public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (reloadReady.get()) {
                    if (isValid(item, column.getText())) { // Valid entry
                        handler.newEntry(getTableRow().getItem().get(0), item, column.getText());
                        setStyle("-fx-border-color: #EDEDED; -fx-border-width: 1;");
                    } else { // Invalid entry
                        handler.badEntry(getTableRow().getItem().get(0), item, column.getText());
                        setStyle("-fx-border-color: red; -fx-border-width: 2;");
                    }
                    reloadReady.set(false);
                }
            }
        });

        //Make cell editable
        column.setEditable(true);

        //On edit, switch off initial load (for update)
        column.setOnEditCommit(event -> {
            ObservableList<String> rowData = event.getTableView().getItems().get(event.getTablePosition().getRow());
            rowData.set(finalIdx, event.getNewValue());
            reloadReady.set(true);
        });

        tableView.getColumns().add(column);
    }



    @FXML private void addRow() {
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

        //Check column type against value entered
        switch (type) {
            case -15: //Is Big Int
                try { Long.parseLong(data);
                } catch (NumberFormatException e) {
                    return false; //Invalid input
                } break;
            case 3: //Is Decimal
                try { BigDecimal check = new BigDecimal(data); //Convert to decimal
                if (check.toString() != data) { return false; } //Check if it is the same
                } catch (NumberFormatException e) {
                    return false; //Invalid input
                } break;
            case 4: //Is Integer
                try { Integer.parseInt(data);
                } catch (NumberFormatException e) {
                    return false; //Invalid input
                } break;
            case 6: //Is Float (passed as double)
            case 8: //Is Double
                try { Double.parseDouble(data);
                } catch (NumberFormatException e) {
                    return false; //Invalid input
                } break;
            case 7: //Is Double
                try { Double.parseDouble(data);
                } catch (NumberFormatException e) {
                    return false; //Invalid input
                } break;
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
