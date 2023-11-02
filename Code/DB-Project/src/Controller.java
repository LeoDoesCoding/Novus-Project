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

public class Controller {
    @FXML
    private TableView<ObservableList<String>> tableView;

    @FXML
    private VBox sidebar;

    @FXML
    private ToggleButton toggleButton;

    @FXML
    private Button addColumnButt;

    private Table handler = new Table(); //Table object, handles once instance of a table.


    //Adds new (blank) column
    @FXML
    private void addColumn() {
        int finalIdx = tableView.getColumns().size(); //Get current size of table
        TableColumn<ObservableList<String>, String> column = new TableColumn<>("Column " + (finalIdx + 1));
        handler.typeAdd(12);

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
            handler.newEntry(selectedRow.get(0), event.getNewValue(), event.getTablePosition().getColumn(), column.getText());
        });

        tableView.getColumns().add(column);
        handler.addColumn("col" + (tableView.getColumns().size()));
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
                handler.newEntry(selectedRow.get(0), event.getNewValue(), event.getTablePosition().getColumn(), column.getText());
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
    private void saveToDatabase() {
        DBcontroller.saveToDatabase(handler.saveToDatabase());
    }
}
