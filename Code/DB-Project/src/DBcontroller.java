import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.util.Pair;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

//Manages the SQL connection and ferries queries
public class DBcontroller {
    static protected Connection SQLcon;
    static protected String DB = "testDB";

    //DEBUGGING logs in with my details
    public static void autologin() throws SQLException {
        String url = "jdbc:sqlserver://localhost;databaseName=testDB";
        String user = "SA";
        String pass = "abc";

        if(loginAttempt(url, user, pass)){
            SQLcon = DriverManager.getConnection(url, user, pass);
        }
    }

    //If true, a connection should be established to static, and the next scene loaded.
    static boolean loginAttempt(String url, String user, String pass){
        try (Connection conAttempt = DriverManager.getConnection(url, user, pass)) {
            System.out.println(conAttempt);
            return (true);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return (false);
        }
    }

    //Gets table columns
    public static ObservableList<String> getColumns() throws SQLException {
        ObservableList<String> data = FXCollections.observableArrayList();

        try (ResultSet result = SQLcon.prepareStatement("SELECT * FROM Movies WHERE 1 = 0").executeQuery()) {
            DataHandler.newDatabase(result.getMetaData().getColumnCount()); //Set empty table list
            for (int i = 1; i <= result.getMetaData().getColumnCount(); i++) {
                data.add(result.getMetaData().getColumnName(i));
            }
        }
        return data;
    }

    public static ObservableList getEntries() throws SQLException {
        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();

        try (ResultSet result = SQLcon.prepareStatement("SELECT * FROM Movies").executeQuery()) {
            while (result.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                for (int i = 1; i <= result.getMetaData().getColumnCount(); i++) {
                    row.add(result.getString(i));
                }
                data.add(row);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return data;
    }


    //Gets table for opened database
    static void getTables(String myDB) throws SQLException {
        try {
            DatabaseMetaData md = SQLcon.getMetaData();
            ResultSet retrival = md.getTables(myDB, "dbo", "%", null);
            while (retrival.next()){
                System.out.println(retrival.getString(3));
            }
        } catch (SQLException e) { throw new RuntimeException(e);}
    }

    static void getColumns(String table) {
        try {
            Statement stmt = SQLcon.createStatement();
            ResultSet result = stmt.executeQuery("select * from " + table);
            ResultSetMetaData resMeta = result.getMetaData();
            int count = resMeta.getColumnCount();
            Table myTable = new Table();


        } catch (SQLException e) { throw new RuntimeException(e);}
    }

}


