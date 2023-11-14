package com.CRUDinator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.util.concurrent.CompletableFuture;

//Controller for login screen
public class loginController {
    @FXML
    private TextField URLField;
    @FXML
    private TextField userField;
    @FXML
    private TextField passField;
    @FXML
    private Button loginButt;
    @FXML
    private Label status;

    private Runnable login;


    //Set login action on creation
    public void setLoginAction(Runnable login) {
        this.login = login;
    }


    //Button clicked, attempt login
    @FXML
    private void loginAttempt() {
        //Disable stuff while a login is attempted
        loginButt.setDisable(true);
        URLField.setDisable(true);
        userField.setDisable(true);
        passField.setDisable(true);
        status.setText("Attempting connection... Please wait.");


        //Run check statement sepperately as to not freeze program
        CompletableFuture.supplyAsync(() -> DBcontroller.loginAttempt("jdbc:sqlserver://" + URLField.getText(),
                userField.getText(), passField.getText())).thenAcceptAsync(loginSuccessful -> {
            Platform.runLater(() -> {
                if (loginSuccessful) {
                    login.run();
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Login attempt failed.\nPlease check your connection and ensure you entered the correct details.");
                    alert.show();
                }

                //Allow user to try again
                URLField.setDisable(false);
                userField.setDisable(false);
                passField.setDisable(false);
                loginButt.setDisable(false);
                status.setText("");
            });
        });
    }
}
