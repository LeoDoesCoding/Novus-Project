package com.CRUDinator;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

import static javafx.application.Application.launch;

public class View extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override public void start(Stage primaryStage) throws SQLException, IOException {
        debugger(primaryStage); //Debbuger autologin. Bypasses login and database load (custom set). NOTE: change the controller in MainView.fxml and comment out the "select database" button.
        //real(primaryStage); //Login NOTE: change the controller in MainView.fxml and remove comment marking from "select database" button.
    }


    //DEBUG Automatically logs in and loads DB
    public void debugger(Stage primaryStage) throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("MainView.fxml"));
            Parent root = loader.load();
            Controller mainController = loader.getController();
            DBcontroller.autoLogin("DemoDB");
            mainController.loadTables("DemoDB");

            //Load MainView
            Scene mainScene = new Scene(root, 800, 500);
            primaryStage.setResizable(true);
            primaryStage.setScene(mainScene);
            primaryStage.setTitle("CRUDinator");
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //Put back in main for real use
    public void real(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("LoginView.fxml"));
        Parent root = loader.load();
        loginController controller = loader.getController();


        //Successful login, load main page
        controller.setLoginAction(() -> {
            try {
                FXMLLoader mainLoader = new FXMLLoader(getClass().getResource("MainView.fxml"));
                Parent mainRoot = mainLoader.load();
                Controller mainController = mainLoader.getController();
                mainController.init();

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