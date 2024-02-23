package com.CRUDinator;

import java.util.HashMap;
import java.util.Map;

public class Column {
    private String name; //Blank, unless name is changed
    private int type; //Data type
    private int scale;
    private int precision;
    private Map<String, String> entries  = new HashMap<>(); //Key = ID from Table
    private boolean isNew; //Dictates whether it is added to the database

    public Column(int type, boolean isNew, int scale, int precision) {
        this.type = type;
        this.isNew = isNew;
        this.scale = scale;
        this.precision = precision;
    }

    //Add/modify entry
    public void addEntry(String index, String value) {
        entries.put(index, value);
    }

    //Getters and setters
    public int getType() { return this.type; }
    public int getScale() { return this.scale; }
    public int getPrecision() { return this.precision; }
    public boolean getIsNew() { return this.isNew; }
    public String getEntry(int ID) {
        if (this.entries.get(ID) != null) {
            return this.entries.get(ID);
        } else {
        return null;
        }
    }

    public int size(){ return entries.size(); }
    public boolean containsKey (String ID) { return entries.containsKey(ID); }
    public void setName(String name) { this.name = name; }
    public boolean isNew() { return this.isNew; }
    public Map<String, String> getEntries() { return this.entries; }
}
