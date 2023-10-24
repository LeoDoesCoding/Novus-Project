import java.util.HashMap;
import java.util.Map;

//Manages a table
public class Table<T> {
    private Map<String, Column> columns  = new HashMap<String, Column>(); //list of columns (key = name, value = column object)

    //ID hashmap: key = arbuitary value for operation, value = original database key to map onto database.
    //I guess display key can be a whole other column.
    public Table() {
        addColumn("IDs", "int", true);
    }

    //Creates a new column
    public void addColumn(String name, String type, boolean isNew) {
        columns.put(name, new Column(type, isNew));
    }

    //Creates a new index for a new row (database's row's current ID)
    public void newRow(int originID) {
        columns.get("IDs").addEntry(columns.get("IDs").size()+1, originID);
    }

    //New row without an originID (row not formally present in database)
    public void newRow(){
        columns.get("IDs").addEntry(columns.get("IDs").size()+1, null);
    }


    //Adding new entry (row + column intersection)
    public void newEntry(int ID, T value, String type, String columnName) {
        //Check row is stored. If not, create.
        if (!columns.get("IDs").containsKey(ID)) {
            newRow(ID);
        }
        //Check column is present. If not, create.
        if (!columns.containsKey(columnName)) {
            addColumn(columnName, type, false);
        }

        //Both row and column are now available. Add entry.
        columns.get(columnName).addEntry(ID, value);
    }
}

