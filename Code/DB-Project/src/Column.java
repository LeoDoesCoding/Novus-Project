import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//Manages a hashmap of columns for one specific data type.
public class Column<T> {
    private Map<String, ArrayList<T>> columns  = new HashMap<String, ArrayList<T>>(); //Key=column name, value = list of entry's values
    private String type; //Type it stores (int, float, string etc)

    public Column(String type){
        this.columns.put(type, new ArrayList<T>());
    }

    //Edits singular entry value
    public void editValue(String column, int index, T newValue){
        columns.computeIfPresent(column, (key, value) -> {
            value.set(index, newValue);
            return value;
        });
    }

    //Returns a string of the variable type
    public String getType(){
        return this.type;
    }

    //Adds new column
    public void addColumn(String name){
        columns.put(name, new ArrayList<T>());
    }

}
