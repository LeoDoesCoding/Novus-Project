import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;


import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class Controller {
    @FXML
    private VBox sidebar;

    @FXML
    private ToggleButton toggleButton;

    @FXML
    private Button addColumnButt;

    @FXML
    private ComboBox<String> DBcomboBox;

    @FXML
    private TabPane tabPane;

    //To access the tab's controllers (to save everything to database)
    private HashMap<Tab, TableController> tabControllers = new HashMap<>();

    //Populate combobox with available tables
    public void init() throws SQLException {
        //Populate combobox
        DBcomboBox.getItems().setAll(DBcontroller.getDatabases());
    }

    //When "Select database" is clicked, load up the tabs and tables.
    @FXML
    private void selectDatabase() throws SQLException {
        //If a valid table is selected, load it and load tab names
        if (!DBcomboBox.getValue().equals("")) {
            DBcontroller.chooseDatabase(DBcomboBox.getValue()); //Set new connection
            ArrayList<String> tabList = DBcontroller.getTables(DBcomboBox.getValue());

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
                    tableController.updateTableView(DBcomboBox.getValue(), tabName);
                    tabContent.setUserData(tableController);
                    tabControllers.put(tab, tableController);

                    //Put content in tab
                    tab.setContent(tabContent);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                tabPane.getTabs().add(tab);
            }
        }
    }


    //When "save to database" is clicked, save all table data to database
    @FXML
    private void saveToDatabase() {
        ArrayList<TableController> listoTab = new ArrayList<TableController>();

        //Iterate each table for changes. If changes are present, ask user if they are sure they want to save
        for(TableController tab : tabControllers.values()) {
            if (tab.isModified()) {
                listoTab.add(tab);
            }
        }

        //Check if there are any changes. If yes, ask user if they want to save them.
        if (!listoTab.isEmpty()) {
            StringBuilder alertString = new StringBuilder();
            for (TableController tab : listoTab){
                alertString.append(tab.handler.getTable() + "\n");
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION, "The following tables have been changed:\n" + alertString + "Are you sure you want to save?");
            alert.getButtonTypes().addAll(ButtonType.CANCEL);

            //If user selects YES, save all changes to database.
            alert.showAndWait().ifPresent(buttonType -> {
                if (buttonType == buttonType.OK) {
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
