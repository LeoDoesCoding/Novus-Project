import javafx.util.Pair;

import java.sql.SQLException;
import java.util.*;

//Manages a table's runtime storage. Created as an instance in Controller.java
public class Table<T> {
    private String DBname;
    private String tableName;
    private ArrayList<Integer> columnTypes = new ArrayList<Integer>(); //columns data types
    private ArrayList<Integer> columnIDs = new ArrayList<Integer>(); //The ordered IDs of the current view (as primary key may be altered)
    public ArrayList<String> newRows = new ArrayList<String>(); //Primary key value for new entries Must be iterating for INSERT in save to database.
    public Map<String, Column> columns  = new HashMap<String, Column>(); //list of columns (key = name (from database), value = column object)
    //public Map<Integer, String> IDs = new HashMap<>(1); //key = arbuitary value for operation, value = original database key to map onto database.
    private String PK;

    public Table(String DBname, String tableName){
        this.DBname = DBname;
        this.tableName = tableName;
    }
    public String getTable() { return this.tableName; }
    public String getDB() { return this.DBname; }

    //Column stuff
    //Initialise columnTypes from imported columns
    public void colInit(ArrayList<Integer> columnTypes){this.columnTypes = columnTypes; }

    //Public Key column name getter + setter
    public void setPK(String PK) { this.PK = PK; }
    public String getPK(){ return this.PK; }

    //columnTypes setter
    public void typeAdd(int type){ columnTypes.add(type); }

    //Creates a new column
    public void addColumn(String name) { columns.put(name, new Column(name, 12, true)); } //New column (pass from Table)
    public void importColumn(String name) { columns.put(name, new Column(name, DBcontroller.typeOf(this.tableName, name), false)); } //Imported column (pass from Controller)
    public void setColumnName(String ID, String newName) { columns.get(ID).setName(newName); }


    //Add row, by generating it a new ID
    public String addRow() {
        boolean newKey = false;
        String ID = "0";

        //Generate a random key until it is not one current present in database
        while (!newKey) {
            //If PK type is string, generate a UUID. Else, generate a random number.
            //We are assuming of course it is either a String or number. Not strictly true ^^'
            if (DBcontroller.typeOf(tableName, PK) == 4){
                ID = String.valueOf(Integer.valueOf((int) (Integer.valueOf((ID)) + DBcontroller.highestID(tableName, PK) + 1))); //+1 in case (unlikely) the retrived new highest value is already present
            } else {
                Random rand = new Random();
                ID = "newRow" + rand.nextInt(1000); //example: newRow55645.
            }
            newKey=(DBcontroller.checkID(tableName, ID, PK) && !newRows.contains(ID));
        }
        //New key obtained, add to newRows as new ID.
        newRows.add(ID);
        return String.valueOf(ID);
    }


    //Adding new entry (row + column intersection)
    //May recieve either origin ID(string) or registered ID (int)
    //Type is only used if the column has not been established
    public void newEntry(String ID, String value, String columnName) {
        //Check column is present. If not, import. (newly created columns already added)
        if (!columns.containsKey(columnName)) {
            importColumn(columnName);
        }

        //Both row and column are now available. Add entry.
        columns.get(columnName).addEntry(ID, value);
    }


    //Check if changes have been made for table (ie, is there any data stored here)
    public boolean isModified() {
        if (newRows.isEmpty() && columns.isEmpty()){ return false; }
        else { return true;}
    }




    //DATABASE QUERY STUFF (returns query strings)--------
    //get ALTER queries (new column) currently string by default.
    public String getAlters() {
        StringBuilder toReturn = new StringBuilder();

        //Iterate each column for new columns
        for (Map.Entry<String, Column> column : columns.entrySet()) {
            if (column.getValue().isNew()) {
                toReturn.append("ALTER TABLE EXAMPLE ADD " + column.getKey() + " varchar(255);");
            }
        }
        return toReturn.toString();
    }


    //Get INSERT queries
    public String getInserts() {
        StringBuilder toReturn = new StringBuilder(); //Key=row, value =columns

        //For each new row, create an INSERT query to add an ID
        for (String row : newRows) {
            toReturn.append("INSERT INTO " + tableName + " (" + PK + ") VALUES (" + row + ");"); //Adding the primary column to insert
        }

        return toReturn.toString();
    }


    //get UPDATE queries
    public String getUpdates() {
        String apo;
        HashMap<String, String> toReturnList = new HashMap<>(); //Key=entryID, value="UPDATE _ SET" (append "WHERE" at end of looping

        //Iterate each column
        for (Map.Entry<String, Column> column : columns.entrySet()) {
            //If it is a string-type columnm, put appostrophies either side of values
            if (column.getValue().getType() == 12 || column.getValue().getType() == -15) { apo = "'"; } else { apo = "";}

            //Iterate column's entries
            Map<String, T> entries  = column.getValue().getEntries();
            for (Map.Entry<String, T> entry : entries.entrySet()) { //Iterate column's data
                String ID = entry.getKey(); //For readability's sake
                //If ID is not already present for UPDATED queries, add opening of query
                if (!toReturnList.containsKey(ID)) { //For pre-existing entries
                    toReturnList.put(ID, "UPDATE " + tableName + " SET ");
                }

                //Now, add the values and columns
                toReturnList.put(ID, toReturnList.get(ID) + column.getKey() + " = " + apo +  entry.getValue() + apo + ","); //Get index, append item
            }
        }

        //Adding tail to UPDATE query (WHERE <primaryKeyColumn> = ID;)
        for  (Map.Entry<String, String> entry : toReturnList.entrySet()) {
            toReturnList.put(entry.getKey(),  entry.getValue().substring(0, entry.getValue().length() - 1) + " WHERE " + this.PK + " = " + entry.getKey() + ";");
        }


        //Get toReturnList as a single string
        String toReturn = toReturnList.values()
                .stream()
                .map(Object::toString)
                .reduce("", String::concat);


        //Now view is same as hashmap. Clear hashmaps.
        newRows.clear();
        columns.clear();
        return toReturn;
    }
}

