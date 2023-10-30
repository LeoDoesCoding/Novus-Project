import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

import static javafx.application.Application.launch;

public class View extends Application {

    TableView<ObservableList<String>> tableView = new TableView<>();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws SQLException, IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("MainView.fxml"));
        Parent root = loader.load();
        DBcontroller.autologin();
        Controller controller = loader.getController();


        //Table stuff
        controller.updateTableView();


        primaryStage.setScene(new Scene(root, 400, 300));
        primaryStage.setTitle("Dynamic Table Example");
        primaryStage.show();
    }
}