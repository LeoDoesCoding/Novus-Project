package com.CRUDinator;
import javafx.collections.ObservableList;

import java.io.Console;
import java.util.*;

//Data manager for each TableView. Created as an instance in TableController.java
public class Table {
    private String tableName;
    public int badCounter = 0; //Number of invalid cells in table.
    private List<String> columnIDs; //Ordered column IDs of the current view (index = position in table, value = ID for hashmap entry/fetch)
    private Map<Integer, String> rowIDs = new HashMap<>(); //Ordered row IDs of the current view (index = position in table, value = ID for hashmap entry/fetch)
    public List<String> newRows = new ArrayList<>(); //ID value for new entries.
    public Map<String, Column> columns  = new HashMap<>(); //list of columns (key = name (from database), value = column object)
    //Note: Column name changes are stored as a string in a Column object. Key will ALWAYS be DB or generated name.
    //To get column from columns at a given index, use "columns.get(columnIDs.get(colID))"
    private String PK; //Name of primary key column


    //On creation, set table name
    public Table(String tableName){
        this.tableName = tableName;
    }
    public String getTable() { return this.tableName; }
    public void setColIDs(ObservableList<String> colList) { columnIDs = colList; } //When a view is loaded, set ID list


    //Column stuff
    public void setPK(String PK) { this.PK = PK; } //Primary key column (set)
    public String getPK() { return this.PK; } //Primary key column (get)
    public boolean isPresent(int colID) { return columns.containsKey(columnIDs.get(colID)); } //Is column stored in program

    public int getScale(int colID) {
        if (isPresent(colID)) { //If column is saved, get scale from here
            return columns.get(columnIDs.get(colID)).getScale();
        } else { //If not, get scale from DB
            return DBcontroller.getScale(this.tableName, columnIDs.get(colID));
        }
    }

    //Check if ID is already stored. If not, add it.
    public void storeRowID(int rowInd, String ID) {
        //If the index is not already stored, add the new index and ID.
        if (!rowIDs.containsKey(rowInd)) {
            System.out.println("Row ID new to handler. Let's store.");
            rowIDs.put(rowInd, ID);
        }
    }

    public int getPrecision(int colID) { //Column by index
        if (isPresent(colID)) { //If column is saved, get precision from here
            return columns.get(columnIDs.get(colID)).getPrecision();
        } else { //If not, get precision from DB
            return DBcontroller.getPrecision(this.tableName, columnIDs.get(colID));
        }
    }
    public int getPrecision(String colName) { //Column by name (used only in generating new row ID)
        if (columns.containsKey(colName)) { //If column is saved, get precision from here
            return columns.get(colName).getPrecision();
        } else { //If not, get precision from DB
            return DBcontroller.getPrecision(this.tableName, colName);
        }
    }

    public int getType(int colID) {
        if (columns.containsKey(columnIDs.get(colID))) { return columns.get(columnIDs.get(colID)).getType(); //If column is saved, get type from here
        } else { return DBcontroller.getColumnType(this.tableName, columnIDs.get(colID)); } //If not, get type from DB
    }

    public void setColumnName(int colID, String newName) { columns.get(columnIDs.get(colID)).setName(newName); }

    //Creates column (new and imported)
    public void addColumn(String name) {
        columnIDs.add(name);
        columns.put(name, new Column(12, true, 50, 50)); } //New blank column (string scale50 by default)
    public void importColumn(String columnName) { columns.put(columnName, new Column(DBcontroller.getColumnType(this.tableName, columnName), false, DBcontroller.getScale(this.tableName, columnName), DBcontroller.getPrecision(this.tableName, columnName))); } //Imported column



    //Add row, by generating it a new ID
    public String newRow(int rowInd) {
        String ID;
        int inc = 1;
        //Generate ID (ensures it is unique, but not neccessarily that follows the column constraints)
        //It does not need to follow column constraints, only really needed for program reference
        do {
            switch (DBcontroller.getColumnType(tableName, PK)) {
                //NUMERICS
                case 4:
                case -5:
                case 3:
                case 6:
                case 8:
                case 7:
                case 5:
                case -6:
                case -7:
                    ID = String.valueOf((int)DBcontroller.highestID(tableName, PK) + inc);
                    inc +=1;
                    break;
                default:
                    Random rand = new Random();
                    ID = "newRow" + rand.nextInt(10000); //example: newRow55645.
                break;
            }
        //Loop if the ID already exists.
        //TODO: Take into consideration renamed IDs.
        } while (DBcontroller.isIDPresent(tableName, ID, PK) || rowIDs.containsValue(ID));

        System.out.println("Row ID added.");
        System.out.println(rowIDs);

        //New key obtained, add to newRows as new ID.
        newRows.add(ID);
        rowIDs.put(rowInd, ID);
        return ID;
    }


    //Adding new entry (row + column intersection)
    public void newEntry(int rowID, String value, int colID) {
        //If column has not been imported, do so. (new columns already present)
        if (!columns.containsKey(columnIDs.get(colID))) {
            importColumn(columnIDs.get(colID));
        }

        //Column is now available. Add entry.
        columns.get(columnIDs.get(colID)).addEntry(rowIDs.get(rowID), value);
    }

    //Check if changes have been made for table (ie, is there any data stored here)
    public boolean isModified() {
        if (rowIDs.isEmpty() && columns.isEmpty()){ return false; }
        else { return true; }
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
        if (!newRows.isEmpty()) {
            StringBuilder toReturn = new StringBuilder(); //Key=row, value =columns
            String apo = setApo(DBcontroller.getColumnType(tableName, PK));

            //For each new row, create an INSERT query to add an ID
            for (String row : newRows) {
                toReturn.append("INSERT INTO " + tableName + " (" + PK + ") VALUES (" + apo + row + apo + ");"); //Adding the primary column to insert
            }
            return toReturn.toString();
        }
        return "";
    }


    //get UPDATE queries
    public String getUpdates() {
        System.out.println(rowIDs);
        System.out.println(columns);
        String apo;
        HashMap<String, String> toReturnList = new HashMap<>(); //Key=entryID, value="UPDATE _ SET" (append "WHERE" at end of looping

        //Iterate each column
        for (Map.Entry<String, Column> column : columns.entrySet()) {
            apo = setApo(column.getValue().getType());

            //Iterate column's entries
            for (Map.Entry<String, String> entry : column.getValue().getEntries().entrySet()) { //Iterate column's data
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


        //Here saving should be successful, now view is same as database. Clear the columns hashmap.
        //TODO: Do not clear in instance of error (ie, connection error etc).
        columns.clear();
        return toReturn;
    }


    //Check column type for if appostrophe is needed in query. Return "" or empty string.
    private String setApo(int type) {
        if (type == 1 || type == -9 || type == 12 || type == -15 || type == -16 || type == -19) {
            return "\'";
        } else { return "";}
    }
}

