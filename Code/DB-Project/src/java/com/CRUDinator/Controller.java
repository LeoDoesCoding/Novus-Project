package com.CRUDinator;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

//Controller for main screen outside of table
public class Controller {
    @FXML private ComboBox<String> DBcomboBox;
    @FXML private TabPane tabPane;

    //To access the tab's controllers (to save everything to database)
    private HashMap<Tab, TableController> tabControllers = new HashMap<>();

    //Populate combobox with available tables
    public void init() throws SQLException {
        DBcomboBox.getItems().setAll(DBcontroller.getDatabases());
    }


    //When "Select database" is clicked, get database name and run loadTables
    //This is seperated from loadTables in order to pass the database automatically when taking a debug route (see: View.java).
    @FXML private void selectDatabase() throws SQLException {
        //If a valid table is selected, load it and load tab names
        if (!DBcomboBox.getValue().equals("")) {
            DBcontroller.chooseDatabase(DBcomboBox.getValue()); //Set new connection
            loadTables(DBcomboBox.getValue());
        }
    }

    //Load table from provided database name as string
    public void loadTables (String DB) throws SQLException{
        ArrayList<String> tabList = DBcontroller.getTables(DB);

        //For each table, create a tab and a TableView
        for(String tabName : tabList) {
            //Create empty tab
            Tab tab = new Tab(tabName);

            // Load content into the tab dynamically
            FXMLLoader loader = new FXMLLoader(getClass().getResource("tableContent.fxml"));
            try {
                AnchorPane tabContent = loader.load();

                //Generate controllers for the tabs
                TableController tableController = loader.getController();
                tableController.loadTable(tabName);
                tabContent.setUserData(tableController);
                tabControllers.put(tab, tableController);

                //Put content in tab
                tab.setContent(tabContent);
            } catch (SQLException | IOException e) {
                throw new RuntimeException(e);
            }

            tabPane.getTabs().add(tab);
        }
    }


    //When "save to database" is clicked, save all table data to database
    @FXML private void saveToDatabase() {
        ArrayList<TableController> listoTab = new ArrayList<TableController>();

        //Iterate each table for changes. If changes are present, ask user if they are sure they want to save
        StringBuilder alertString = new StringBuilder();
        for(TableController tab : tabControllers.values()) {
            if (tab.isModified()) {
                //If there is invalid values, show warning popup and cancel action.
                if(tab.invalidsPresent()) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Unable to save because there are invalid entries present. \nPlease check your tables and amend issues.");
                    alert.show();
                    return;
                }
                listoTab.add(tab);
                alertString.append(tab.handler.getTable() + "\n");
            }
        }

        //Check if there are any changes. If yes, ask user if they want to save them.
        if (!alertString.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "The following tables have been changed:\n" + alertString + "Are you sure you want to save?");
            alert.getButtonTypes().addAll(ButtonType.CANCEL);

            //If user selects YES, save all changes to database.
            alert.showAndWait().ifPresent(buttonType -> {
                if (buttonType == buttonType.OK) {
                    System.out.println("They said OKK!");
                    StringBuilder alters = new StringBuilder();
                    StringBuilder inserts = new StringBuilder();
                    StringBuilder updates = new StringBuilder();
                    //For each query type, get all from each table.
                    for (TableController tab : listoTab){
                        alters.append(tab.handler.getAlters());
                        inserts.append(tab.handler.getInserts());
                        updates.append(tab.handler.getUpdates());
                    }
                    //If alters isn't empty, send query
                    if (!alters.isEmpty()){
                        System.out.println(alters);
                        try {
                            DBcontroller.saveToDatabase(alters.toString());
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    //If inserts isn't empty, send query
                    if (!inserts.isEmpty()){
                        System.out.println(inserts);
                        try {
                            DBcontroller.saveToDatabase(inserts.toString());
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    //If updates isn't empty, send query
                    if (!updates.isEmpty()){
                        System.out.println(updates);
                        try {
                            DBcontroller.saveToDatabase(updates.toString());
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            });
        }
    }
}
