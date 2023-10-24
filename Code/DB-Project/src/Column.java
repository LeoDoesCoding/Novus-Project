import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Column<T> {
    private String type; //Data type
    private Map<Integer, T> entries  = new HashMap<Integer, T>(); //Key = ID from Table
    private boolean isNew; //Dictates whether it is added to the database

    public Column(String type, boolean isNew) {
        this.type = type;
        this.isNew = isNew;
    }

    //Also used to edit an entry
    public void addEntry(int index, T value){
        entries.put(index, value);
    }

    //Getters
    public String getType(){
        return this.type;
    }
    public int size(){ return entries.size();}
    public boolean containsKey (int ID) {return entries.containsKey(ID);}
}
