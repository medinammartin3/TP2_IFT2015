import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;

public class WordMap<K,V> implements Map<K,V>{
    private int n = 0; // Nombre d'elements dans la Map
    private int capacity; // taille de la Map
    private LinkedList<Entry<K,V>>[] table; // On utilise LinkedList pour traiter des eventuelles collisions

    // 2 constructeurs au choix
    public WordMap(int cap){
        this.capacity = cap;
        this.createTable();
    }
    public WordMap() {
        this.capacity = 16;
        this.createTable();
    }

    public int size() { return n; }
    public boolean isEmpty() { return this.size() == 0; }
    public boolean containsKey( K key ) {
        int hashValue = hashValue(key);
        LinkedList<Entry<K,V>> bucket = table[hashValue];
        if (bucket == null)
            return false;
        for (Entry<K,V> entry : bucket) {
            if (entry.getKey().equals(key)) {
                return true;
            }
        }
        return false;
    }

    // Chercher la valeur d'une cle
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

    // Eliminer l'element au complet
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
        if( bucket == null ) // Le bucket où on veut mettre le mot est vide
        {
            bucket = table[hashValue] = new LinkedList<>();
        } else {
            for (Entry<K, V> entry : bucket) {
                if (entry.getKey().equals(key)) // La cle existe deja
                {
                    entry.setValue(value); // On change seulement la valeur
                    return value;
                }
            }
        }
        bucket.add(new Entry<>(key,value)); // Si la cle n'existe pas, on ajoute un nouveau element
        this.n += 1; // Mettre a jour le nombre d'elements
        if (n > capacity * 0.75) // Maintenir le facteur de charge <= 0.75
            resize(2 * capacity + 1);
        return value;
    }

    // ArrayList de toutes les cles de la Map
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

    // ArrayList de tous les elements de la Map
    public Iterable<Entry<K,V>> entrySet() {
        ArrayList<Entry<K,V>> buffer = new ArrayList<>();
        for (LinkedList<Entry<K,V>> bucket : table) {
            if (bucket != null) {
                for (Entry<K,V> entry : bucket) {
                    buffer.add(entry);
                }
            }
        }
        return buffer;
    }

    // ArrayList de toutes les valeur de la Map
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

    // Code de hashage
    private int hashValue( K key ) {
        return Math.abs(key.hashCode()) % capacity;
    }

    private void resize( int newCapacity ){
        List<Entry<K,V>> buffer = new ArrayList<>();
        for( Entry<K,V> entry : this.entrySet() )
            buffer.add( entry );
        this.capacity = newCapacity;
        this.createTable(); // Avec la nouvelle capacite
        this.n = 0; // Va augmenter automatiquement lors de l'insertion des elements
        for( Entry<K,V> entry : buffer )
            put( entry.getKey(), entry.getValue() );
    }

    @SuppressWarnings("unchecked")
    private void createTable(){
        // Creer un nouveau tableau
        table = new LinkedList[this.capacity];
    }
}


