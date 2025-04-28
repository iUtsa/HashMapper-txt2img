import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A simple HashMap implementation with a basic hash function.
 * This implementation uses separate chaining for collision resolution.
 */
public class SimpleHashMap<K, V> {

    // Inner class for storing key-value pairs
    private static class Entry<K, V> {
        K key;
        V value;

        Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Entry<?, ?> entry = (Entry<?, ?>) o;
            return Objects.equals(key, entry.key);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(key);
        }
    }

    private ArrayList<Entry<K, V>>[] buckets;
    private final int size;
    private int collisions;
    private int itemCount;
    private static String hashFunctionType = "String Length";

    /**
     * Constructor with default size of 16
     */
    @SuppressWarnings("unchecked")
    public SimpleHashMap() {
        this(16);
    }

    /**
     * Constructor with specified size
     */
    @SuppressWarnings("unchecked")
    public SimpleHashMap(int size) {
        this.size = size;
        this.buckets = new ArrayList[size];
        this.collisions = 0;
        this.itemCount = 0;

        // Initialize buckets
        for (int i = 0; i < size; i++) {
            buckets[i] = new ArrayList<>();
        }
    }

    /**
     * Set the hash function type
     */
    public static void setHashFunctionType(String type) {
        hashFunctionType = type;
    }

    /**
     * Dumb hash function - intentionally simplified and inefficient
     * This function is designed to demonstrate collision behaviors
     */
    private int dumbHash(K key) {
        if (key == null) {
            return 0;
        }

        // For strings
        if (key instanceof String) {
            String str = (String) key;
            if (str.isEmpty()) {
                return 0;
            }

            // Use different hash functions based on selection
            switch (hashFunctionType) {
                case "String Length":
                    return str.length() % size;

                case "First Character":
                    return str.charAt(0) % size;

                case "First + Last Character":
                    if (str.length() > 1) {
                        return (str.charAt(0) + str.charAt(str.length() - 1)) % size;
                    } else {
                        return str.charAt(0) % size;
                    }

                case "Character Sum":
                    int sum = 0;
                    for (char c : str.toCharArray()) {
                        sum += c;
                    }
                    return sum % size;

                case "Random":
                    // Pseudo-random but deterministic based on first and last chars
                    if (str.length() > 1) {
                        return ((str.charAt(0) * 31) ^ str.charAt(str.length() - 1)) % size;
                    } else {
                        return str.charAt(0) % size;
                    }

                default:
                    return str.length() % size;
            }
        }

        // For integers
        if (key instanceof Integer) {
            Integer num = (Integer) key;
            return Math.abs(num) % size;
        }

        // For other types, use a very basic approach
        String keyString = key.toString();
        if (keyString.isEmpty()) {
            return 0;
        }

        // Just use first and last character
        int hash = keyString.charAt(0);
        if (keyString.length() > 1) {
            hash += keyString.charAt(keyString.length() - 1);
        }

        return Math.abs(hash) % size;
    }

    /**
     * Hash function wrapper - uses dumbHash for this implementation
     */
    private int hash(K key) {
        return dumbHash(key);
    }

    /**
     * Insert or update a key-value pair
     */
    public void put(K key, V value) {
        int index = hash(key);
        ArrayList<Entry<K, V>> bucket = buckets[index];

        // Check if key already exists
        for (int i = 0; i < bucket.size(); i++) {
            if (Objects.equals(bucket.get(i).key, key)) {
                bucket.get(i).value = value; // Update existing value
                return;
            }
        }

        // New entry - check for collision
        if (!bucket.isEmpty()) {
            collisions++;
        }

        // Add new entry
        bucket.add(new Entry<>(key, value));
        itemCount++;
    }

    /**
     * Get a value by key
     */
    public V get(K key) {
        int index = hash(key);
        ArrayList<Entry<K, V>> bucket = buckets[index];

        // Search for key in bucket
        for (Entry<K, V> entry : bucket) {
            if (Objects.equals(entry.key, key)) {
                return entry.value;
            }
        }

        return null; // Key not found
    }

    /**
     * Remove a key-value pair
     */
    public boolean remove(K key) {
        int index = hash(key);
        ArrayList<Entry<K, V>> bucket = buckets[index];

        // Find and remove entry
        for (int i = 0; i < bucket.size(); i++) {
            if (Objects.equals(bucket.get(i).key, key)) {
                bucket.remove(i);
                itemCount--;
                return true;
            }
        }

        return false; // Key not found
    }

    /**
     * Check if key exists
     */
    public boolean containsKey(K key) {
        int index = hash(key);
        ArrayList<Entry<K, V>> bucket = buckets[index];

        for (Entry<K, V> entry : bucket) {
            if (Objects.equals(entry.key, key)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Get all keys in the HashMap
     */
    public List<K> keys() {
        List<K> allKeys = new ArrayList<>();

        for (ArrayList<Entry<K, V>> bucket : buckets) {
            for (Entry<K, V> entry : bucket) {
                allKeys.add(entry.key);
            }
        }

        return allKeys;
    }

    /**
     * Get number of items in the HashMap
     */
    public int size() {
        return itemCount;
    }

    /**
     * Get number of collisions that occurred
     */
    public int getCollisionCount() {
        return collisions;
    }

    /**
     * Get current load factor
     */
    public double getLoadFactor() {
        return (double) itemCount / size;
    }

    /**
     * Get distribution of items across buckets
     */
    public int[] getBucketDistribution() {
        int[] distribution = new int[size];

        for (int i = 0; i < size; i++) {
            distribution[i] = buckets[i].size();
        }

        return distribution;
    }
}