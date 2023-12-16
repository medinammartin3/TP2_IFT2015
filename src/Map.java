import java.lang.Iterable;

public interface Map<K,V> {
    int size();
    boolean isEmpty();
    boolean containsKey( K key );
    V get( K key );
    V put( K key, V value );
    V remove( K key );

    Iterable<K> keySet();
    Iterable<V> values();
    Iterable<Entry<K,V>> entrySet();

}
