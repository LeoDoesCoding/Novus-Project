module com.CRUDinator {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens com.CRUDinator to javafx.fxml;
    exports com.CRUDinator;
}