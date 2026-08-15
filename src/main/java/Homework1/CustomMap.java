package Homework1;

public interface CustomMap<K, V> {
    public void put(K key, V value);
    public V get(K key);
    public void remove(K key);
    public boolean containsKey(K key);
    public int size();
    public boolean isEmpty();
}
