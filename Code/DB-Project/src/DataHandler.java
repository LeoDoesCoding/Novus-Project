import java.util.List;

//Stores and does the handling of Table and Column class objects
public class DataHandler {
    private static Table[] table;

    //A database is opened
    public static void newDatabase(int size){
        table = new Table[size];
        for (int i = 0; i < size; i++) {
            table[i] = new Table();
        }
    }

    //New column created (ID=col<number> by default. Name to database deturmined by column String name)
    public static void newColumn(String colName) {
        table[0].addColumn(colName, 4,true);
        System.out.println(table[0].toString());
    }

    //Change column name
    public static void changeColname(String ID, String colName) {
        table[0].setColumnName(ID, colName);
    }

}
