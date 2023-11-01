import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

//Manages a table's runtime storage. Created as an instance in Controller.java
public class Table<T> {
    private ArrayList<Integer> columnTypes = new ArrayList<Integer>(); //columns data types
    private ArrayList<Integer> columnIDs = new ArrayList<Integer>(); //The ordered IDs of the current view
    public Map<String, Column> columns  = new HashMap<String, Column>(); //list of columns (key = name (from database), value = column object)
    public Map<String, Integer> IDs = new HashMap<>(1); //key = arbuitary value for operation, value = original database key to map onto database.

    public Table(){

    }

    //Column stuff
    //Initialise columnTypes from imported columns
    public void colInit(ArrayList<Integer> columnTypes){this.columnTypes = columnTypes; }

    //columnTypes setter
    public void typeAdd(int type){ columnTypes.add(type); }

    //Creates a new column
    public void addColumn(String name) { columns.put(name, new Column(name, 4, true));
        System.out.println(columns.toString());} //New column
    public void addColumn(String name, int type, boolean isNew) { columns.put(name, new Column(name, type, isNew)); } //Imported column
    public void setColumnName(String ID, String newName) { columns.get(ID).setName(newName); }



    //Creates a new index for a new row (database's row's current ID)
    public void newRow(String originID) {
        IDs.put(originID, IDs.size()+1);
    }


    //Adding new entry (row + column intersection)
    //May recieve either origin ID(string) or registered ID (int)
    //Type is only used if the column has not been established
    public void newEntry(String ID, String value, int type, String columnName) {
        //Check row is stored. If not, create.
        if (!IDs.containsKey(ID)) {
            newRow(String.valueOf(ID));
        }
        //Check column is present. If not, create.
        if (!columns.containsKey(columnName)) {
            addColumn(columnName, type, false);
        }

        //Both row and column are now available. Add entry.
        columns.get(columnName).addEntry(Integer.valueOf((String) ID), value);
    }
}

