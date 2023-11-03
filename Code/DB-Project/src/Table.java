import javafx.util.Pair;

import java.sql.SQLException;
import java.util.*;

//Manages a table's runtime storage. Created as an instance in Controller.java
public class Table<T> {
    private ArrayList<Integer> columnTypes = new ArrayList<Integer>(); //columns data types
    private ArrayList<Integer> columnIDs = new ArrayList<Integer>(); //The ordered IDs of the current view (as primary key may be altered)
    public ArrayList<String> newRows = new ArrayList<String>(); //Primary key value for new entries Must be iterating for INSERT in save to database.
    public Map<String, Column> columns  = new HashMap<String, Column>(); //list of columns (key = name (from database), value = column object)
    //public Map<Integer, String> IDs = new HashMap<>(1); //key = arbuitary value for operation, value = original database key to map onto database.
    private String PK;

    public Table(){

    }

    //Column stuff
    //Initialise columnTypes from imported columns
    public void colInit(ArrayList<Integer> columnTypes){this.columnTypes = columnTypes; }

    //Public Key column name getter + setter
    public void setPK(String PK) { this.PK = PK; }
    public String getPK(){ return this.PK; }

    //columnTypes setter
    public void typeAdd(int type){ columnTypes.add(type); }

    //Creates a new column
    public void addColumn(String name) { columns.put(name, new Column(name, 12, true));
    for (String key : columns.keySet()){
        System.out.println("Next column:" + key);}
    } //New column (pass from Table)
    public void importColumn(String name) { columns.put(name, new Column(name, DBcontroller.typeOf(name), false)); } //Imported column (pass from Controller)
    public void setColumnName(String ID, String newName) { columns.get(ID).setName(newName); }


    //Add row, by generating it a new ID
    public String addRow() {
        boolean newKey = false;
        String ID = "0";

        //Generate a random key until it is not one current present in database
        while (!newKey) {
            //If PK type is string, generate a UUID. Else, generate a random number.
            //We are assuming of course it is either a String or number. Not strictly true ^^'
            if (DBcontroller.typeOf(PK) == 4){
                ID = String.valueOf(Integer.valueOf((int) (Integer.valueOf((ID)) + DBcontroller.highestID(PK) + 1))); //+1 in case (unlikely) the retrived new highest value is already present
            } else {
                Random rand = new Random();
                ID = "newRow" + rand.nextInt(1000); //example: newRow55645.
            }
            newKey=(DBcontroller.checkID(ID, PK) && !newRows.contains(ID));
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



    //Mmm delicous spaghetti
    //Save to Database preparation
    public String saveToDatabase() throws SQLException {
        String apo;
        HashMap<String, String> toReturnList = new HashMap<>(); //Key=entryID, value="UPDATE _ SET" (append "WHERE" at end of looping
        HashMap<String, String> newRow1 = new HashMap<String, String>(); //Key=row, value =columns
        HashMap<String, String> newRow2 = new HashMap<String, String>(); //Key=row, value = values of column^
        StringBuilder newReturns = new StringBuilder(); //Final return string (all queries)

        //ADD NEW COLUMNS
        for (Map.Entry<String, Column> column : columns.entrySet()) {
            if(column.getValue().isNew()) {
                newReturns.append("ALTER TABLE EXAMPLE ADD " + column.getKey() + " varchar(255);");
                System.out.println(newReturns);
                DBcontroller.saveToDatabase(newReturns.toString());//Send query for new colunns
                newReturns.setLength(0); //Clear
            }
        }

        //UPDATING OLD ENTRIES + INSERTING NEW
        //If new rows, add IDs column (this is outside of column iteration)
        for (String row : newRows) {
            if(!columns.containsKey(PK) || columns.containsKey(PK) && !columns.get(PK).containsKey(row)) { //If ID has not been renamed, add original given ID. Else, it will be added in the columns iteration
                newRow1.put(row, PK); //Adding the primary column to insert
                newRow2.put(row, row); //Adding the primary column's value to insert. Might need to change when adding changing primary key
            }
        }

        //Iterate each column
        for (Map.Entry<String, Column> column : columns.entrySet()) {
            System.out.println(column.getKey() + " is type " + column.getValue().getType());
            if (column.getValue().getType() == 12 || column.getValue().getType() == -15) { apo = "'"; } else { apo = "";}

            //Iterate column's entries
            Map<String, T> entries  = column.getValue().getEntries();
            for (Map.Entry<String, T> entry : entries.entrySet()) { //Iterate column's data
                String ID = entry.getKey(); //For readability's sake
                //If ID is not already present for UPDATED queries, add opening of query
                if (!toReturnList.containsKey(ID) && !newRows.contains(ID)) { //For pre-existing entries
                    toReturnList.put(ID, "UPDATE EXAMPLE SET ");
                }

                //Now, add the values and columns
                if (newRows.contains(ID)){ //It is a new row
                    //System.out.println("newRow contains " + ID + ", so enter " + column.getKey());
                    newRow1.put(ID, newRow1.get(ID) + ", " + column.getKey()); //Columns
                    newRow2.put(ID, newRow2.get(ID) + ", " + apo + entry.getValue() + apo); //Values
                } else { //It is a pre-existing row
                    toReturnList.put(ID, toReturnList.get(ID) + column.getKey() + " = " + apo +  entry.getValue() + apo + ","); //Get index, append item
                }
            }
        }

        //Adding tail to UPDATE query
        for  (Map.Entry<String, String> entry : toReturnList.entrySet()) {
            toReturnList.put(entry.getKey(),  entry.getValue().substring(0, entry.getValue().length() - 1) + " WHERE " + this.PK + " = " + entry.getKey() + ";");
        }

        System.out.println(newRow1);
        //Formatting INSERT queries
        if (!newRows.isEmpty()) {
            //Iterate each row
            for (String ID : newRow1.keySet()) {
                newReturns.append("INSERT INTO EXAMPLE (");
                //Adding columns
                newReturns.append(newRow1.get(ID));

                //Delete final commar off last column
                //newReturns.deleteCharAt(newReturns.length() - 1);
                //Adding values for columns
                newReturns.append(") VALUES (");
                newReturns.append(newRow2.get(ID));
                //Delete final commar off last value
                //newReturns.deleteCharAt(newReturns.length() - 1);
                //Add bracket and semicolon to end query
                newReturns.append(");");
            }
        }
        //INSERTS done

        //Get toReturnList as a single string
        String toReturn = toReturnList.values()
                .stream()
                .map(Object::toString)
                .reduce("", String::concat);
        newReturns.append(toReturn); //Add updated entries

        System.out.println(newReturns.toString());

        //Now view is same as hashmap. Clear hashmaps.
        newRows.clear();
        columns.clear();
        return newReturns.toString();
    }
}

