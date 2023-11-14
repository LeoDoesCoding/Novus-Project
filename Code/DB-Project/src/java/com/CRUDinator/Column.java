package com.CRUDinator;

import java.util.HashMap;
import java.util.Map;

public class Column {
    private String name; //Collumns's display name
    private int type; //Data type
    private Map<String, String> entries  = new HashMap<>(); //Key = ID from Table
    private Map<String, String> badEntries = new HashMap<>(); //Entries for INVALIDS. Key = ID from table
    private boolean isNew; //Dictates whether it is added to the database

    public Column(String name, int type, boolean isNew) {
        this.name = name;
        this.type = type;
        this.isNew = isNew;
    }

    //Add/modify entry
    public void addEntry(String index, String value) {
        entries.put(index, value);
        //If index was previously and invalid value, remove from badEntries.
        if (badEntries.containsKey(index)) {
            badEntries.remove(index);
        }
    }
    public void badEntry(String index, String value) { badEntries.put(index, value);}

    //Getters and setters
    public int getType(){ return this.type; }
    public String getEntry(int ID) {
        if (this.entries.get(ID) != null) {
            return this.entries.get(ID);
        } else {
        return null;
        }
    }

    public int size(){ return entries.size(); }
    public boolean containsKey (String ID) { return entries.containsKey(ID); }
    public boolean hasBadEntries() { return !badEntries.isEmpty(); }
    public void setName(String name){ this.name = name; }
    public boolean isNew(){ return this.isNew; }
    public Map<String, String> getEntries() { return this.entries; }
}
