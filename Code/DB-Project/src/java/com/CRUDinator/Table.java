package com.CRUDinator;
import javafx.collections.ObservableList;

import java.util.*;

//Data manager for each TableView. Created as an instance in TableController.java
public class Table<T> {
    private String tableName;
    private String[] columnIDs; //Ordered column IDs of the current view (index = position in table, value = ID for hashmap entry/fetch)
    private ArrayList<Integer> rowIDs = new ArrayList<>(); //Ordered row IDs of the current view (index = position in table, value = ID for hashmap entry/fetch)
    public ArrayList<String> newRows = new ArrayList<String>(); //ID value for new entries.
    public Map<String, Column> columns  = new HashMap<String, Column>(); //list of columns (key = name (from database), value = column object)
    private String PK; //Name of primary key column

    //On creation, set table name
    public Table(String tableName){
        this.tableName = tableName;
    }
    public String getTable() { return this.tableName; }
    public void setColIDs(ObservableList<String> colList) { columnIDs = colList.toArray(new String[0]);
        System.out.println(Arrays.toString(columnIDs)); } //When a view is loaded, set ID list

    //Column stuff
    //Public Key column name getter + setter
    public void setPK(String PK) { this.PK = PK; }
    public String getPK(){ return this.PK; }
    public boolean isPresent(String colName) { return columns.containsKey(Arrays.asList(columnIDs).indexOf(colName)); }

    //Creates a new column
    public void addColumn(String name) { columns.put(name, new Column(name, 12, true)); } //New column (pass from Table)
    public void importColumn(String name) { columns.put(name, new Column(name, DBcontroller.getColumnType(this.tableName, name), false)); } //Imported column (pass from Controller)
    public void setColumnName(String ID, String newName) { columns.get(ID).setName(newName); }
    public int getType(String colName) { return columns.get(colName).getType(); }


    //Add row, by generating it a new ID
    public String addRow() {
        boolean newKey = false;
        String ID = "0";

        //Generate a random key value until it is not one current present in database
        while (!newKey) {
            if (DBcontroller.getColumnType(tableName, PK) == 4){
                ID = String.valueOf(Integer.valueOf((int) (Integer.valueOf((ID)) + DBcontroller.highestID(tableName, PK) + 1))); //+1 in case (unlikely) the retrived new highest value is already present
            } else {
                Random rand = new Random();
                ID = "newRow" + rand.nextInt(1000); //example: newRow55645.
            }
            System.out.println("PK:  " + PK);
            newKey=(DBcontroller.checkID(tableName, ID, PK) && !newRows.contains(ID));
        }
        //New key obtained, add to newRows as new ID.
        newRows.add(ID);
        return String.valueOf(ID);
    }


    //Adding new entry (row + column intersection)
    //Type is only used if the column has not been established
    public void newEntry(String ID, String value, String columnName) {
        //Check column is present. If not, import. (newly created columns already added)
        if (!columns.containsKey(columnName)) {
            importColumn(columnName);
        }

        //Column is now available. Add entry.
        columns.get(columnName).addEntry(ID, value);
    }
    //For invalid entries
    public void badEntry(String ID, String value, String columnName) {
        if (!columns.containsKey(columnName)) {
            importColumn(columnName);
        }

        //Column is now available. Add entry.
        columns.get(columnName).badEntry(ID, value);
    }


    //Check if changes have been made for table (ie, is there any data stored here)
    public boolean isModified() {
        if (newRows.isEmpty() && columns.isEmpty()){ return false; }
        else { return true; }
    }

    //Checks if there are invalid entries present
    public boolean invalidsPresent() {
        for (Column column : columns.values()) {
            if (column.hasBadEntries()) { return true; } //Invalid entries found
        }
        return false;
    }




    //DATABASE QUERY STUFF (returns query strings)--------
    //get ALTER queries (new column) currently string by default.
    public String getAlters() {
        StringBuilder toReturn = new StringBuilder();

        //Iterate each column for new columns
        for (Map.Entry<String, Column> column : columns.entrySet()) {
            if (column.getValue().isNew()) {
                toReturn.append("ALTER TABLE " + tableName + " ADD " + column.getKey() + " varchar(255);");
            }
        }
        return toReturn.toString();
    }


    //Get INSERT queries
    public String getInserts() {
        StringBuilder toReturn = new StringBuilder(); //Key=row, value =columns
        String apo = setApo(DBcontroller.getColumnType(tableName, PK));

        //For each new row, create an INSERT query to add an ID
        for (String row : newRows) {
            toReturn.append("INSERT INTO " + tableName + " (" + PK + ") VALUES (" + apo + row + apo + ");"); //Adding the primary column to insert
        }

        return toReturn.toString();
    }


    //get UPDATE queries
    public String getUpdates() {
        String apo;
        HashMap<String, String> toReturnList = new HashMap<>(); //Key=entryID, value="UPDATE _ SET" (append "WHERE" at end of looping

        //Iterate each column
        for (Map.Entry<String, Column> column : columns.entrySet()) {
            apo = setApo(column.getValue().getType());

            //Iterate column's entries
            Map<String, String> entries  = column.getValue().getEntries();
            for (Map.Entry<String, String> entry : entries.entrySet()) { //Iterate column's data
                String ID = entry.getKey(); //For readability's sake
                //If ID is not already present for UPDATED queries, add opening of query
                if (!toReturnList.containsKey(ID)) { //For pre-existing entries
                    toReturnList.put(ID, "UPDATE " + tableName + " SET ");
                }

                //Now, add the values and columns
                toReturnList.put(ID, toReturnList.get(ID) + column.getKey() + " = " + apo +  entry.getValue() + apo + ","); //Get index, append item
            }
        }

        apo = setApo(DBcontroller.getColumnType(tableName, PK));

        //Adding tail to UPDATE query (WHERE <primaryKeyColumn> = ID;)
        for  (Map.Entry<String, String> entry : toReturnList.entrySet()) {
            toReturnList.put(entry.getKey(),  entry.getValue().substring(0, entry.getValue().length() - 1) + " WHERE " + this.PK + " = " + apo + entry.getKey() + apo + ";");
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


    //Check column type for if appostrophe is needed in query. Return ' or empty string.
    private String setApo(int type) {
        if (type == 1 || type == 12 || type == -15 || type == -16 || type == -19) {
            return "\'";
        } else { return "";}
    }
}

