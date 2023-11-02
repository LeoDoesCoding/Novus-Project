import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

//Manages a table's runtime storage. Created as an instance in Controller.java
public class Table<T> {
    private ArrayList<Integer> columnTypes = new ArrayList<Integer>(); //columns data types
    private ArrayList<Integer> columnIDs = new ArrayList<Integer>(); //The ordered IDs of the current view
    public Map<String, Column> columns  = new HashMap<String, Column>(); //list of columns (key = name (from database), value = column object)
    //public Map<Integer, String> IDs = new HashMap<>(1); //key = arbuitary value for operation, value = original database key to map onto database.
    private String PK;

    public Table(){

    }

    //Column stuff
    //Initialise columnTypes from imported columns
    public void colInit(ArrayList<Integer> columnTypes){this.columnTypes = columnTypes; }

    public void setPK(String PK) { this.PK = PK; }

    //columnTypes setter
    public void typeAdd(int type){ columnTypes.add(type); }

    //Creates a new column
    public void addColumn(String name) { columns.put(name, new Column(name, 4, true));
        System.out.println(columns.toString());} //New column
    public void addColumn(String name, int type, boolean isNew) { columns.put(name, new Column(name, type, isNew)); } //Imported column
    public void setColumnName(String ID, String newName) { columns.get(ID).setName(newName); }



    //Adding new entry (row + column intersection)
    //May recieve either origin ID(string) or registered ID (int)
    //Type is only used if the column has not been established
    public void newEntry(String ID, String value, int type, String columnName) {
        //Check row is stored. If not, create.
        //if (!IDs.containsKey(ID)) {
            //newRow(String.valueOf(ID));
        //}
        //Check column is present. If not, create.
        if (!columns.containsKey(columnName)) {
            addColumn(columnName, type, false);
        }

        //Both row and column are now available. Add entry.
        columns.get(columnName).addEntry(ID, value);
    }



    //Mmm delicous spaghetti
    //Save to Database preparation
    public String saveToDatabase() {
        String apo;

        HashMap<String, String> toReturnList = new HashMap<>(); //Key=entryID, value="UPDATE _ SET" (append "WHERE" at end of looping
        //Iterate each column
        System.out.println("Start iterating columns");
        for (Map.Entry<String, Column> column : columns.entrySet()) {
            if (column.getValue().getType() == 12) { apo = "'"; } else { apo = "";}

            //Iterate column's entries
            Map<String, T> entries  = column.getValue().getEntries();
            for (Map.Entry<String, T> entry : entries.entrySet()) { //Iterate column's data
                //If ID is not already present, add opening of query
                if (!toReturnList.containsKey(entry.getKey())){
                    toReturnList.put(entry.getKey(), "UPDATE Movies SET ");
                    System.out.println("Added ID " + entry.getKey() + " to returnList");
                    System.out.println("toReturnList now: " + toReturnList); //This is correct
                }

                //System.out.println("Start iterating entries.");
                System.out.println("At " + entry.getKey() + " in toReturnList is " + toReturnList.get(entry.getKey()));
                toReturnList.put(entry.getKey(), toReturnList.get(entry.getKey()) + column.getKey() + " = " + apo +  entry.getValue() + apo + ","); //Get index, append item
            }
            //System.out.println("Stop iterating entries.");
        }
        for  (Map.Entry<String, String> entry : toReturnList.entrySet()) {
            toReturnList.put(entry.getKey(),  entry.getValue().substring(0, entry.getValue().length() - 1) + " WHERE " + this.PK + " = " + entry.getKey() + ";");
        }


        //Get toReturnList as a single string
        String toReturn = toReturnList.values()
                .stream()
                .map(Object::toString)
                .reduce("", String::concat);


        System.out.println(toReturn);

        //Wipe column hashmaps here
        return toReturn.toString();
    }
}

