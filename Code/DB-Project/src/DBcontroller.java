import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.*;
import java.util.ArrayList;

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

        try (ResultSet result = SQLcon.prepareStatement("SELECT * FROM EXAMPLE WHERE 1 = 0").executeQuery()) {
            for (int i = 1; i <= result.getMetaData().getColumnCount(); i++) {
                data.add(result.getMetaData().getColumnName(i));
            }
        }
        return data;
    }

    public static ArrayList<Integer> getColumnTypes() {
        try (ResultSet result = SQLcon.prepareStatement("SELECT * FROM EXAMPLE WHERE 1 = 0").executeQuery()) {

            ArrayList<Integer> returnList = new ArrayList<Integer>();
            for (int i = 1; i <= result.getMetaData().getColumnCount(); i++) {
                returnList.add(result.getMetaData().getColumnType(i));
            }
            return returnList;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static ObservableList getEntries() throws SQLException {
        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();

        try (ResultSet result = SQLcon.prepareStatement("SELECT * FROM EXAMPLE").executeQuery()) {
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


    //Find out which column has the primary IDs
    public static String getIDColumn() throws SQLException {
        String tableName = "EXAMPLE";
        String sql = "SELECT KU.column_name as PRIMARYKEYCOLUMN FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS AS TC "
                + "INNER JOIN INFORMATION_SCHEMA.KEY_COLUMN_USAGE AS KU "
                + "ON TC.CONSTRAINT_TYPE = 'PRIMARY KEY' "
                + "AND TC.CONSTRAINT_NAME = KU.CONSTRAINT_NAME "
                + "AND KU.table_name=?";

        try (PreparedStatement preparedStatement = SQLcon.prepareStatement(sql)) {
            preparedStatement.setString(1, tableName);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    String primaryKeyColumn = resultSet.getString("PRIMARYKEYCOLUMN");
                    System.out.println("Primary Key Column for table " + tableName + ": " + primaryKeyColumn);
                    return primaryKeyColumn;
                } else {
                    System.out.println("No primary key found for table " + tableName);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static ArrayList<String> getColumn(String colName) {
        ArrayList<String> toReturn = new ArrayList<>();
        try (ResultSet result = SQLcon.prepareStatement("SELECT " + colName + " FROM EXAMPLE").executeQuery()) {
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
    public static boolean checkID(String ID, String pColumn) {
        try (PreparedStatement preparedStatement = SQLcon.prepareStatement("SELECT * FROM EXAMPLE WHERE " + pColumn + " = " + ID)) {
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
    public static int typeOf(String column) {
        try (ResultSet result = SQLcon.prepareStatement("SELECT " + column + " FROM EXAMPLE WHERE 1 = 0").executeQuery()) {
            return result.getMetaData().getColumnType(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    //Get highest ID
    public static double highestID(String pColumn) {
        try (ResultSet result = SQLcon.prepareStatement("SELECT MAX(" + pColumn + ") AS max_value FROM EXAMPLE").executeQuery()) {
            if (result.next()) {
                return (result.getInt("max_value"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return -1;
    }

}


