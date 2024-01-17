//Auteurs:
//Étienne Mitchell-Bouchard (20243430)
//Martin Medina (20235219)

//Implémentation de l'interface Entry des notes de cours sur les Maps (5.1)
public class Entry<K,V>{
    private final K key;
    private V value;

    public Entry(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) { this.value = value; }
}

