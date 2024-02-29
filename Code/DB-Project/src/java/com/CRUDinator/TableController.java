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

    //Used for managing cell reload actions
    public enum Reload {
        FALSE, STYLE, ALL;
    }

    protected Table handler; //Table object, handles once instance of a table.
    Reload reloadReady = Reload.FALSE;


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
        tableView.getItems().addAll(DBcontroller.getEntries(handler.getTable()));
    }


    //Adds column (called by loadTable and addColumn)
    private void addColumnHandler(String name) throws SQLException {
        int finalIdx = tableView.getColumns().size();
        TableColumn<ObservableList<String>, String> column = new TableColumn<>(name);
        column.setPrefWidth(100);

        //Factory (gets column data)
        column.setCellValueFactory(param -> {
            String cellValue = param.getValue().get(finalIdx);
            return new SimpleStringProperty(cellValue);
        });


        //When cell is updated, validate it
        column.setCellFactory(col -> new TextFieldTableCell<>(new DefaultStringConverter()) {

            //If old ID (imported or generated) is not stored, store it before edits are made.
            @Override
            public void startEdit() {
                handler.storeRowID(getTableRow().getIndex(), getTableRow().getItem().get(0));
                super.startEdit();
            }

            @Override public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                //Check if style and/or data needs updating
                if (reloadReady != Reload.FALSE) {
                    if (isValid(item, tableView.getColumns().indexOf(column))) { // Valid entry
                        //If it was formally invalid, decrease bad entry counter.
                        if (getStyle().contains("-fx-border-color: red")) {
                            handler.badCounter -= 1;
                        }

                        //If reload is for data update too, store data.
                        if (reloadReady == Reload.ALL) {
                            handler.newEntry(getTableRow().getIndex(), item, tableView.getColumns().indexOf(column));
                        }
                        setStyle("-fx-border-color: #EDEDED; -fx-border-width: 1;");

                    } else { // Invalid entry. Save to badEntry list.
                        //If it was formally valid, increase bad entry counter.
                        if (getStyle() == "" || getStyle().contains("-fx-border-color: #EDEDED")) {
                            handler.badCounter += 1;
                        }
                        setStyle("-fx-border-color: red; -fx-border-width: 2;");
                    }

                    reloadReady = Reload.FALSE;
                }
            }
        });

        //Make cell editable
        column.setEditable(true);

        //On edit, switch off initial load (for update)
        column.setOnEditCommit(event -> {
            ObservableList<String> rowData = event.getTableView().getItems().get(event.getTablePosition().getRow());
            rowData.set(finalIdx, event.getNewValue());
            reloadReady = Reload.ALL;
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
        String ID = handler.newRow(tableView.getItems().size()-1); //Add row with generated ID

        //Add primary key (by iterating tableView's columns)
        for (TableColumn<ObservableList<String>, ?> column : tableView.getColumns()) {
            if (column.getText().equals(handler.getPK())) {
                emptyRow.set(tableView.getColumns().indexOf(column), ID);
                reloadReady = Reload.STYLE; //Update style (not data, otherwise it counts the ID as having been modified)
            }
        }
    }


    //ENTRY VALIDATION
    //Checks entry and if it is valid according to column rules
    public boolean isValid(String data, int colID) {
        //Get column type
        int type = handler.getType(colID);
        System.out.println("Type:" + type + " precision: " + handler.getPrecision(colID) + " scale: " + handler.getScale(colID));

        //Check column type against value entered
        switch (type) {
            case -6: //Is Tiny Int
                try {
                    Byte.parseByte(data);
                } catch (NumberFormatException e) {
                    return false; //Invalid input
                }
                //Check precision and scale
                if (!numericalPrec(Double.valueOf(data), colID)) { return false; }
                break;
            case -5: //Is Big Int
                try { Long.parseLong(data);
                } catch (NumberFormatException e) {
                    return false; //Invalid input
                }
                //Check precision and scale
                if (!numericalPrec(Double.valueOf(data), colID)) { return false; }
                break;
            case 2:
                if (!numericalPrec(Double.valueOf(data), colID)) { return false; }
                break;
            case 3: //Is Decimal
                try { BigDecimal check = new BigDecimal(data); //Convert to decimal
                if (!check.toString().equals(data)) { return false; } //Check if it is the same
                } catch (NumberFormatException e) {
                    return false; //Invalid input
                }
                //Check precision and scale
                if (!numericalPrec(Double.valueOf(data), colID)) { return false; }
                if (!numericalScale(data, colID)) { return false; }
                break;
            case 4: //Is Integer
                try { Integer.parseInt(data);
                } catch (NumberFormatException e) {
                    return false; //Invalid input
                }
                //Check precision and scale
                if (!numericalPrec(Double.valueOf(data), colID)) { return false; }
                break;
            case 6: //Is Float (passed as double)
            case 8: //Is Double
                try { Double.parseDouble(data);
                } catch (NumberFormatException e) {
                    return false; //Invalid input
                }
                //Check precision and scale
                if (!numericalPrec(Double.valueOf(data), colID)) { return false; }
                if (!numericalScale(data, colID)) { return false; }
                break;
            case 7: //Is Real
                try { Float.parseFloat(data);
                } catch (NumberFormatException e) {
                    return false; //Invalid input
                }
                //Check precision and scale
                if (!numericalPrec(Double.valueOf(data), colID)) { return false; }
                if (!numericalScale(data, colID)) { return false; }
                break;
            case 1: //Is Char
            case 12: //Is varchar
            case -15: //Is Nchar
            case -9: //Is Nvarchar
                //The check for char, varchar, nchar and nvarchar is the same. Char length is padded when as a query.
                handler.getPrecision(colID);
                if (data.length() > handler.getPrecision(colID)) { return false; } //Invalid input
                break;
            default:
        }
        return true;
    }

    //Check precision of a numerical value
    public boolean numericalPrec(double data, int colID) {
        System.out.println(Double.toString(Math.abs(data)).replace(".", "").length() + " Prec: " + handler.getPrecision(colID));
        return Double.toString(Math.abs(data)).replace(".", "").length() <= handler.getPrecision(colID);
    }

    //Check scale of a numerical value
    public boolean numericalScale(String data, int colID) {
        System.out.println(Integer.valueOf(data.substring(data.lastIndexOf(".") + 1).length()) + " Scale: " + handler.getScale(colID));
        return Integer.valueOf(data.substring(data.lastIndexOf(".") + 1).length()) <= handler.getScale(colID);
    }



    //TABLE METHODS
    //Check for changes to table
    public boolean isModified() {
        if (handler.isModified() || handler.badCounter != 0) {
            return true;
        } else {
            return false;
        }
    }

    public boolean invalidsPresent() {
        if (handler.badCounter != 0) {
            return true;
        } else {
            return false;
        }
    }
}
