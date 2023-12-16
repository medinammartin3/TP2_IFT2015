import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public abstract class FileMap <K,V> implements Map <ArrayList<String>, ArrayList<ArrayList<int>>>{
    protected int n = 0; // number of entries in the map
    protected int capacity; // size of the table
    private LinkedList<Entry<ArrayList<String>, ArrayList<ArrayList<int>>>>[] table;

    public FileMap(int cap){
        this.capacity = cap;
        this.createTable();
    }

    public int size() { return n; }
    public boolean isEmpty() { return this.size() == 0; }
    public boolean containsKey( ArrayList<String> key ) {
        int hashValue = hashValue(key);
        LinkedList<Entry<ArrayList<String>, ArrayList<ArrayList<int>>>> bucket = table[hashValue];
        for (Entry<ArrayList<String>, ArrayList<ArrayList<int>>> entry : bucket) {
            if (entry.getKey().equals(key)) {
                return true;
            }
        }
        return false;
    }
    public ArrayList<ArrayList<int>> get(ArrayList<String> key) {
        int hashValue = hashValue(key);
        LinkedList<Entry<ArrayList<String>, ArrayList<ArrayList<int>>>> bucket = table[hashValue];
        if (table[hashValue] != null) {
            for (Entry<ArrayList<String>, ArrayList<ArrayList<int>>> entry : bucket) {
                if (entry.getKey().equals(key))
                    return entry.getValue();
            }
        }
        return null;
    }
    public ArrayList<ArrayList<int>> remove( ArrayList<String> key ) {
        int hashValue = hashValue(key);
        LinkedList<Entry<ArrayList<String>, ArrayList<ArrayList<int>>>> bucket = table[hashValue];
        if (bucket != null) {
            for (Entry<ArrayList<String>, ArrayList<ArrayList<int>>> entry : bucket) {
                if (entry.getKey().equals(key)) {
                    ArrayList<ArrayList<int>> value = entry.getValue();
                    bucket.remove(hashValue);
                    n--;
                    return value;
                }
            }
        }
        return null;
    }
    public ArrayList<ArrayList<int>> put( ArrayList<String> key , ArrayList<ArrayList<int>> value ) {
        int hashValue = hashValue(key);
        LinkedList<Entry<ArrayList<String>, ArrayList<ArrayList<int>>>> bucket = table[hashValue];
        if( bucket == null )
            bucket = table[hashValue] = new LinkedList<>();
        int oldSize = bucket.size();
        bucket.add(new Entry<>(key,value));
        this.n += ( bucket.size() - oldSize ); // size may have increased
        if (n > capacity * 0.75) // keep load factor <= 0.75
            resize(2 * capacity + 1);
        return value;
    }

    public Iterable<ArrayList<String>> keySet() {
        ArrayList<ArrayList<String>> buffer = new ArrayList<>();
        for (LinkedList<Entry<ArrayList<String>, ArrayList<ArrayList<int>>>> bucket : table) {
            if (bucket != null) {
                for (Entry<ArrayList<String>, ArrayList<ArrayList<int>>> entry : bucket) {
                    buffer.add(entry.getKey());
                }
            }
        }
        return buffer;
    }

    public Iterable<Entry<ArrayList<String>, ArrayList<ArrayList<int>>>> entrySet() {
        ArrayList<Entry<ArrayList<String>, ArrayList<ArrayList<int>>>> buffer = new ArrayList<>();
        for (LinkedList<Entry<ArrayList<String>, ArrayList<ArrayList<int>>>> bucket : table) {
            if (bucket != null) {
                for (Entry<ArrayList<String>, ArrayList<ArrayList<int>>> entry : bucket) {
                    buffer.add(entry);
                }
            }
        }
        return buffer;
    }

    public Iterable<ArrayList<ArrayList<int>>> values() {
        ArrayList<ArrayList<ArrayList<int>>> buffer = new ArrayList<>();
        for (LinkedList<Entry<ArrayList<String>, ArrayList<ArrayList<int>>>> bucket : table) {
            if (bucket != null) {
                for (Entry<ArrayList<String>, ArrayList<ArrayList<int>>> entry : bucket) {
                    buffer.add(entry.getValue());
                }
            }
        }
        return buffer;
    }


    private int hashValue( String key ) {
        return key.hashCode() % capacity;
    }

    private void resize( int newCapacity ){
        List<Entry<ArrayList<String>, ArrayList<ArrayList<int>>>> buffer = new ArrayList<>();
        for( Entry<ArrayList<String>, ArrayList<ArrayList<int>>> entry : this.entrySet() )
            buffer.add( entry );
        this.capacity = newCapacity;
        this.createTable(); // based on updated capacity
        this.n = 0; // will be recomputed while reinserting entries
        for( Entry<ArrayList<String>, ArrayList<ArrayList<int>>> entry : buffer )
            put( entry.getKey(), entry.getValue() );
    }

    @SuppressWarnings("unchecked")
    public void createTable(){
        // create an empty table of current capacity
        table = new LinkedList[this.capacity];
    }
}
