import javafx.util.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Column<T> {
    private String name; //Collumns's display name (if different from
    private int type; //Data type
    private Map<String, T> entries  = new HashMap<String, T>(); //Key = ID from Table
    private boolean isNew; //Dictates whether it is added to the database

    public Column(String name, int type, boolean isNew) {
        this.name = name;
        this.type = type;
        this.isNew = isNew;
    }

    //Also used to edit an entry
    public void addEntry(String index, T value) {
        entries.put(index, value);
    }

    //Getters
    public int getType(){ return this.type; }
    public T getEntry(int ID) {
        if (this.entries.get(ID) != null) {
            return this.entries.get(ID);
        } else {
        return (T) "Null";
        }
    }
    public int size(){ return entries.size();}
    public boolean containsKey (String ID) {return entries.containsKey(ID);}
    public void setName(String name){this.name = name;}
    public boolean isNew(){return this.isNew;}
    public Map<String, T> getEntries() { return this.entries; }

    //Gets one entry, returns and deletes it from map
    public Pair<String, String> getReturnString(String oldName) {
        /*HashMap<Integer, String> toReturn = new HashMap<Integer, String>(entries.size());
        char apo = '\0';

        //If it is a string column, it needs an appostrophe for the value
        if (this.type == 12) {
            apo = '\'';
        }

        //For each entry, put it in the hashmap as ID, "Columnname = value"
        for (Map.Entry<Integer, T> entry : entries.entrySet()) {
            toReturn.put(entry.getKey(), oldName + " = " + apo +  entry.getValue() + apo + ","); //ID, "columnName = value"
        }*/

        Map.Entry<String, T> entry = entries.entrySet().iterator().next();
        Pair<String, String> toReturn = new Pair<>(entry.getKey(), entry.getValue().toString());
        entries.remove(entry);
        return toReturn;
    }
}
