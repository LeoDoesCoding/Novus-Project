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
        FXMLLoader loader = new FXMLLoader(getClass().getResource("LoginView.fxml"));
        Parent root = loader.load();
        loginController controller = loader.getController();
        //DBcontroller.autologin();

        //Successful login, load main page
        controller.setLoginAction(() -> {
                try {
                    FXMLLoader mainLoader = new FXMLLoader(getClass().getResource("MainView.fxml"));
                    Parent mainRoot = mainLoader.load();
                    Controller mainController = mainLoader.getController();

                    mainController.init();
                    //ONLY DO THIS WHEN NOT CHOSING DATABASE
                    mainController.updateTableView();

                    //Load MainView
                    Scene mainScene = new Scene(mainRoot, 800, 500);
                    primaryStage.setResizable(true);
                    primaryStage.setScene(mainScene);
                    primaryStage.setTitle("CRUDinator");
                } catch (Exception e) {
                    e.printStackTrace();
                }
        });

        primaryStage.setScene(new Scene(root, 300, 350));
        primaryStage.setTitle("CRUDinator Login");
        primaryStage.setResizable(false);
        primaryStage.show();
    }
}