import java.util.HashMap;
import java.util.Map;

//Manages a table
public class Table<T> {
    public Map<String, Column> columns  = new HashMap<String, Column>(); //list of columns (key = name (from database), value = column object)
    public Map<String, Integer> IDs = new HashMap<>(1);

    //ID hashmap: key = arbuitary value for operation, value = original database key to map onto database.
    //I guess display key can be a whole other column.
    public Table() {
    }

    //Creates a new column
    public void addColumn(String name, int type, boolean isNew) {
        columns.put(name, new Column(name, type, isNew));
    }
    public void setColumnName(String ID, String newName) { columns.get(ID).setName(newName);};

    //Creates a new index for a new row (database's row's current ID)
    public void newRow(String originID) {
        IDs.put(originID, IDs.size()+1);
    }
    //For the "not present in database, therefore no ID" issue can be resolves by attempting to randomly generate an ID and checking if it is present in database.
    //If it is not, it can be used as our pseudo originID.

    //New row without an originID (row not formally present in database)
    //public void newRow() {
        //columns.get("IDs").addEntry(columns.get("IDs").size()+1, null);
    //}


    //Adding new entry (row + column intersection)
    //May recieve either origin ID(string) or registered ID (int)
    //Type is only used if the column has not been established
    public void newEntry(T ID, T value, int type, String columnName) {
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

    //Returns the entries for a specified ID
    public String toString(String ID) {
        String returnStr = IDs.get(ID).toString();
        for (String key : columns.keySet()) {
            returnStr = returnStr + "\n" + key + ": " + columns.get(key).getEntry(IDs.get(ID));
        }
        return returnStr;
    }

    public String toString(){
        String returnStr ="";
        for (String key : columns.keySet()) {
            returnStr = returnStr + "\nColumn " + key;
        }
        return returnStr;
    }
}

