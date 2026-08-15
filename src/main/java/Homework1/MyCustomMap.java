package Homework1;

import java.util.Objects;

public class MyCustomMap<K, V> implements CustomMap<K, V> {
    private static final int DEFAULT_CAPACITY = 16;
    private static final double LOAD_FACTOR = 0.75;

    private static class Node<K, V> {
        K key;
        V value;
        Node<K, V> next;

        Node(K key, V value, Node<K, V> next) {
            this.key = key;
            this.value = value;
            this.next = next;
        }
    }

    Node<K, V>[] buckets;
    int size;

    public MyCustomMap() {
        buckets = new Node[DEFAULT_CAPACITY];
    }

    public void put(K key, V value) {
        if (size > LOAD_FACTOR * buckets.length) {
            resize();
        }

        int bucketIndex = getBucketIndex(key);
        Node<K, V> currentNode = buckets[bucketIndex];

        if (currentNode == null) {
            buckets[bucketIndex] = new Node<>(key, value, null);
            size++;
            return;
        }

        while (true) {
            if (Objects.equals(currentNode.key, key)) {
                currentNode.value = value;
                return;
            }
            if (currentNode.next == null) {
                currentNode.next = new Node<>(key, value, null);
                size++;
                return;
            }
            currentNode = currentNode.next;
        }
    }

    public V get(K key) {
        int bucketIndex = getBucketIndex(key);
        Node<K, V> currentNode = buckets[bucketIndex];

        while (currentNode != null) {
            if (Objects.equals(currentNode.key, key)) {
                return currentNode.value;
            }
            currentNode = currentNode.next;
        }

        return null;
    }

    public void remove(K key) {
        int bucketIndex = getBucketIndex(key);
        Node<K, V> currentNode = buckets[bucketIndex];

        if (currentNode == null) {
            return;
        }

        if (Objects.equals(currentNode.key, key)) {
            buckets[bucketIndex] = currentNode.next;
            size--;
            return;
        }

        while (currentNode.next != null) {
            if (Objects.equals(currentNode.next.key, key)) {
                currentNode.next = currentNode.next.next;
                size--;
                return;
            }
            currentNode = currentNode.next;
        }
    }

    public void remove(K key, V value) {
        int bucketIndex = getBucketIndex(key);
        Node<K, V> currentNode = buckets[bucketIndex];

        if (currentNode == null) {
            return;
        }

        if (Objects.equals(currentNode.key, key) && Objects.equals(currentNode.value, value)) {
            buckets[bucketIndex] = currentNode.next;
            size--;
            return;
        }

        while (currentNode.next != null) {
            if (Objects.equals(currentNode.next.key, key) && Objects.equals(currentNode.next.value, value)) {
                currentNode.next = currentNode.next.next;
                size--;
                return;
            }
            currentNode = currentNode.next;
        }
    }

    public boolean containsKey(K key) {
        int index = getBucketIndex(key);
        Node<K, V> current = buckets[index];
        while (current != null) {
            if (Objects.equals(current.key, key)) return true;
            current = current.next;
        }
        return false;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    private int getBucketIndex(K key) {
        if (key == null) return 0;
        int hash = key.hashCode();
        int index = hash & (buckets.length - 1);
        return index;
    }

    private void resize() {
        Node<K, V>[] oldBuckets = buckets;
        buckets = new Node[oldBuckets.length * 2];

        for (Node<K, V> head : oldBuckets) {
            Node<K, V> current = head;
            while (current != null) {
                Node<K, V> next = current.next;
                int newIndex = getBucketIndex(current.key);
                current.next = buckets[newIndex];
                buckets[newIndex] = current;
                current = next;
            }
        }
    }
}
