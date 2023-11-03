import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.util.converter.DefaultStringConverter;

import javax.swing.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

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


    public void init() throws SQLException {
        //Populate combobox
        DBcomboBox.getItems().setAll(DBcontroller.getDatabases());
    }

    @FXML
    private void selectDatabase() throws SQLException {
        //If a valid table is selected, load it and load tab names
        if (!DBcomboBox.getValue().equals("")) {
            DBcontroller.chooseDatabase(DBcomboBox.getValue()); //Set new connection
            ArrayList<String> tabList = DBcontroller.getTables(DBcomboBox.getValue());

            //For each table, create a tab and a TableView
            for(String tabName : tabList) {
                // Create the tab
                Tab tab = new Tab(tabName);

                // Load content into the tab dynamically
                FXMLLoader loader = new FXMLLoader(getClass().getResource("tableContent.fxml"));
                try {
                    AnchorPane tabContent = loader.load();
                    TableController tableController = loader.getController();
                    tableController.updateTableView(DBcomboBox.getValue(), tabName);
                    tabContent.setUserData(tableController);
                    tab.setContent(tabContent);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                System.out.println("makin a tab");
                tabPane.getTabs().add(tab);
            }
        }
    }
}
