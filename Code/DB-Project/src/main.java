import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.Scanner;

public class main {
    static protected Connection myConnection;

    public static void main(String[] args) throws SQLException {

        Scanner myScanner = new Scanner(System.in);
        String choice;
        String url = "jdbc:sqlserver://localhost;";
        String user = "SA";
        String pass = "abc";

        if(loginAttempt(url, user, pass)){
            myConnection = DriverManager.getConnection(url, user, pass);
        }
        getDatabases();
        System.out.println("Which one do you want to use?");
        choice = myScanner.nextLine();
        getTables(choice);
    }

    //Attempts a database login, returns true (successful) or false (unsuccessful)
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


    //Display available databases
    static void getDatabases() throws SQLException {
        try {
            DatabaseMetaData md = myConnection.getMetaData();
            ResultSet retrival = md.getCatalogs();
            while (retrival.next()){
                System.out.println(retrival.getString("TABLE_CAT"));
            }
        } catch (SQLException e) { throw new RuntimeException(e);}
    }


    //Display available tables from selected databases
    static void getTables(String myDB) throws SQLException {
        try {
            DatabaseMetaData md = myConnection.getMetaData();
            ResultSet retrival = md.getTables(myDB, "dbo", "%", null);
            while (retrival.next()){
                System.out.println(retrival.getString(3));
            }
        } catch (SQLException e) { throw new RuntimeException(e);}
    }
}
