import java.util.List;

//Stores and does the handling of Table and Column class objects
public class DataHandler {
    private static Table[] table;

    //A database is opened
    public static void newDatabase(int size){
        table = new Table[size];
    }
}
