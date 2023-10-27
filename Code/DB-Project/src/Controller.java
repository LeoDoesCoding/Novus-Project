import javafx.animation.TranslateTransition;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.sql.SQLException;

public class Controller {
    @FXML
    TableView tableView;

    @FXML
    private VBox sidebar;

    @FXML
    private ToggleButton toggleButton;


    //Set column names to table
    public void getColumnNames() {
        try {
            ObservableList<String> data = DBcontroller.getTable();
            //For each column, add to table
            for (int i = 0; i < data.size(); i++) {
                int finalIdx = i;
                TableColumn<ObservableList<String>, String> column = new TableColumn<>(data.get(i).toString());

                //Factory (gets column data)
                column.setCellValueFactory(param -> {
                    String cellValue = param.getValue().get(finalIdx);
                    return new SimpleStringProperty(cellValue);
                });
                tableView.getColumns().add(column);
            }
        } catch (
        SQLException e) {
            e.printStackTrace();
        }
    }
}
