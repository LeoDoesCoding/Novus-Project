import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.*;
import java.util.ArrayList;

//Manages the SQL connection and ferries queries
public class DBcontroller {
    static protected Connection SQLcon;
    static protected String url;
    static protected String user;
    static protected String pass;


    //If true, a connection should be established to static, and the next scene loaded.
    static boolean loginAttempt(String newurl, String newuser, String newpass){
        url = newurl;
        user = newuser;
        pass = newpass;
        try (Connection conAttempt = DriverManager.getConnection(url, user, pass)) {
            System.out.println("Login successful. Creating connection to " + url);
            SQLcon = DriverManager.getConnection(url, user, pass); //Set SQLcon to this successful connection
            return (true);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return (false);
        }
    }

    static void chooseDatabase(String db) throws SQLException {
        url= url + ";databaseName=" + db;
        System.out.println("Connecting to " + url);
        try (Connection conAttempt = DriverManager.getConnection(url, user, pass)) {
            SQLcon.close();
            SQLcon = DriverManager.getConnection(url, user, pass); //Set SQLcon to this successful connection
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }


    //Get available databases
    public static ArrayList<String> getDatabases() throws SQLException {
        ArrayList<String> toReturn = new ArrayList<String>();
        DatabaseMetaData meta = SQLcon.getMetaData();
        ResultSet res = meta.getCatalogs();
        while (res.next()) {
            toReturn.add(res.getString("TABLE_CAT"));
        }
        return toReturn;
    }


    //Gets table columns
    public static ObservableList<String> getColumns(String table) throws SQLException {
        ObservableList<String> data = FXCollections.observableArrayList();

        try (ResultSet result = SQLcon.prepareStatement("SELECT * FROM " + table + " WHERE 1 = 0").executeQuery()) {
            for (int i = 1; i <= result.getMetaData().getColumnCount(); i++) {
                data.add(result.getMetaData().getColumnName(i));
            }
        }
        return data;
    }

    public static ArrayList<Integer> getColumnTypes(String table) {
        System.out.println("Getting column types for " + table);
        try (ResultSet result = SQLcon.prepareStatement("SELECT * FROM " + table + " WHERE 1 = 0").executeQuery()) {

            ArrayList<Integer> returnList = new ArrayList<Integer>();
            for (int i = 1; i <= result.getMetaData().getColumnCount(); i++) {
                returnList.add(result.getMetaData().getColumnType(i));
            }
            return returnList;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static ObservableList getEntries(String table) throws SQLException {
        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        System.out.println("SELECT * FROM " + table);

        try (ResultSet result = SQLcon.prepareStatement("SELECT * FROM " + table).executeQuery()) {
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
    static ArrayList<String> getTables(String myDB) throws SQLException {
        ArrayList<String> toReturn = new ArrayList<>();
        try {
            DatabaseMetaData md = SQLcon.getMetaData();
            ResultSet retrival = md.getTables(myDB, "dbo", "%", null);
            while (retrival.next()){
                toReturn.add(retrival.getString(3));
            }
            return toReturn;
        } catch (SQLException e) { throw new RuntimeException(e);}
    }


    //Find out which column has the primary IDs
    public static String getIDColumn(String table) throws SQLException {
        String sql = "SELECT KU.column_name as PRIMARYKEYCOLUMN FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS AS TC "
                + "INNER JOIN INFORMATION_SCHEMA.KEY_COLUMN_USAGE AS KU "
                + "ON TC.CONSTRAINT_TYPE = 'PRIMARY KEY' "
                + "AND TC.CONSTRAINT_NAME = KU.CONSTRAINT_NAME "
                + "AND KU.table_name=?";

        try (PreparedStatement preparedStatement = SQLcon.prepareStatement(sql)) {
            preparedStatement.setString(1, table);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    String primaryKeyColumn = resultSet.getString("PRIMARYKEYCOLUMN");
                    return primaryKeyColumn;
                } else {
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static ArrayList<String> getColumn(String table, String colName) {
        ArrayList<String> toReturn = new ArrayList<>();
        try (ResultSet result = SQLcon.prepareStatement("SELECT " + colName + " FROM " + table).executeQuery()) {
            while(result.next()) {
                toReturn.add(result.getString(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return toReturn;
    }


    public static void saveToDatabase(String toExecute) throws SQLException {
        Statement statement = SQLcon.createStatement();
        statement.executeUpdate(toExecute);
    }

    //Check if passed key is present. If it is not, return true (counter intuitive, I know)
    public static boolean checkID(String table, String ID, String pColumn) {
        try (PreparedStatement preparedStatement = SQLcon.prepareStatement("SELECT * FROM " + table + " WHERE " + pColumn + " = " + ID)) {
            //preparedStatement.setString(1, ID);
            try (ResultSet result = preparedStatement.executeQuery()) {
                if (result.next()) { //Present. Return false.
                    return false;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle the exception according to your application's requirements
        }
        //Not present. Return true.
        return true;
    }

    //Return the data type of column
    public static int typeOf(String table, String column) {
        try (ResultSet result = SQLcon.prepareStatement("SELECT " + column + " FROM " + table + " WHERE 1 = 0").executeQuery()) {
            return result.getMetaData().getColumnType(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    //Get highest ID
    public static double highestID(String table, String pColumn) {
        try (ResultSet result = SQLcon.prepareStatement("SELECT MAX(" + pColumn + ") AS max_value FROM " + table).executeQuery()) {
            if (result.next()) {
                return (result.getInt("max_value"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return -1;
    }

}


