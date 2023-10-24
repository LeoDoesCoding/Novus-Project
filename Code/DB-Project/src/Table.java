import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//Manages a table
public class Table<T> {
    //Old approaches vv
    //private List<Map<String, String>> columns = new ArrayList<>();
    //private List<HashMap<String, List<?>>> Columns = new ArrayList<>();
    //New approach vv
    private List<Column> columns = new ArrayList<>(); //list of column hashmaps (hashmaps of column type <column name, list(entry values)>)
    private Map<String, Integer> indexes  = new HashMap<String, Integer>(); //<Entry ID, arb index>

    public Table() {
        //Define starting columns here
    }

    //Creates a new column
    //Then when retriving a table to save, you can go forEach and add a column.
    public void addColumn(String name, String type) {
        for (Column column : columns){
            //If a column of that data type is available use it.
            if (column.getType().equals(type)) {
                column.addColumn(name);
                break;
            }
        }
        //If no column of that data type is found, make one.
        columns.add(new Column(type));
    }
}

