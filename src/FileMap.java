import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;

public class FileMap<K,V> implements Map<K,V>{
    private int n = 0; // number of entries in the map
    private int capacity; // size of the table
    private LinkedList<Entry<K,V>>[] table;

    public FileMap(int cap){
        this.capacity = cap;
        this.createTable();
    }
    public FileMap() {
        this.capacity = 16;
        this.createTable();
    }

    public int size() { return n; }
    public boolean isEmpty() { return this.size() == 0; }
    public boolean containsKey( K key ) {
        int hashValue = hashValue(key);
        LinkedList<Entry<K,V>> bucket = table[hashValue];
        for (Entry<K,V> entry : bucket) {
            if (entry.getKey().equals(key)) {
                return true;
            }
        }
        return false;
    }
    public V get(K key) {
        int hashValue = hashValue(key);
        LinkedList<Entry<K,V>> bucket = table[hashValue];
        if (table[hashValue] != null) {
            for (Entry<K,V> entry : bucket) {
                if (entry.getKey().equals(key))
                    return entry.getValue();
            }
        }
        return null;
    }
    public V remove( K key ) {
        int hashValue = hashValue(key);
        LinkedList<Entry<K,V>> bucket = table[hashValue];
        if (bucket != null) {
            for (Entry<K,V> entry : bucket) {
                if (entry.getKey().equals(key)) {
                    V value = entry.getValue();
                    bucket.remove(entry);
                    n--;
                    return value;
                }
            }
        }
        return null;
    }
    public V put( K key, V value ) {
        int hashValue = hashValue(key);
        LinkedList<Entry<K,V>> bucket = table[hashValue];
        if( bucket == null )
            bucket = table[hashValue] = new LinkedList<>();
        int oldSize = bucket.size();
        bucket.add(new Entry<>(key,value));
        this.n += ( bucket.size() - oldSize ); // size may have increased
        if (n > capacity * 0.75) // keep load factor <= 0.75
            resize(2 * capacity + 1);
        return value;
    }

    public Iterable<K> keySet() {
        ArrayList<K> buffer = new ArrayList<>();
        for (LinkedList<Entry<K,V>> bucket : table) {
            if (bucket != null) {
                for (Entry<K,V> entry : bucket) {
                    buffer.add(entry.getKey());
                }
            }
        }
        return buffer;
    }

    public Iterable<Entry<K,V>> entrySet() {
        ArrayList<Entry<K,V>> buffer = new ArrayList<>();
        for (LinkedList<Entry<K,V>> bucket : table) {
            if (bucket != null) {
                buffer.addAll(bucket);
            }
        }
        return buffer;
    }

    public Iterable<V> values() {
        ArrayList<V> buffer = new ArrayList<>();
        for (LinkedList<Entry<K,V>> bucket : table) {
            if (bucket != null) {
                for (Entry<K,V> entry : bucket) {
                    buffer.add(entry.getValue());
                }
            }
        }
        return buffer;
    }


    private int hashValue( K key ) {
        return Math.abs(key.hashCode()) % capacity;
    }

    private void resize( int newCapacity ){
        List<Entry<K,V>> buffer = new ArrayList<>();
        for( Entry<K,V> entry : this.entrySet() )
            buffer.add( entry );
        this.capacity = newCapacity;
        this.createTable(); // based on updated capacity
        this.n = 0; // will be recomputed while reinserting entries
        for( Entry<K,V> entry : buffer )
            put( entry.getKey(), entry.getValue() );
    }

    @SuppressWarnings("unchecked")
    private void createTable(){
        // create an empty table of current capacity
        table = new LinkedList[this.capacity];
    }
}


