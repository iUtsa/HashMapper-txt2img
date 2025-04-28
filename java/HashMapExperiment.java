import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Class to run experiments with our SimpleHashMap implementation
 */
public class HashMapExperiment {

    /**
     * Generate a dataset of random strings
     */
    private static List<String> generateStringDataset(int count, int minLength, int maxLength) {
        List<String> dataset = new ArrayList<>();
        Random random = new Random();
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

        for (int i = 0; i < count; i++) {
            int length = random.nextInt(maxLength - minLength + 1) + minLength;
            StringBuilder sb = new StringBuilder(length);

            for (int j = 0; j < length; j++) {
                int index = random.nextInt(chars.length());
                sb.append(chars.charAt(index));
            }

            dataset.add(sb.toString());
        }

        return dataset;
    }

    /**
     * Generate a dataset of random integers
     */
    private static List<Integer> generateIntegerDataset(int count, int max) {
        List<Integer> dataset = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < count; i++) {
            dataset.add(random.nextInt(max));
        }

        return dataset;
    }

    /**
     * Run experiments with different hash functions
     */
    public static void runHashFunctionExperiment() throws IOException {
        String[] hashFunctions = {
                "String Length", "First Character", "First + Last Character",
                "Character Sum", "Random"
        };

        int dataSize = 10000;
        int mapSize = 128;

        FileWriter writer = new FileWriter("hash_function_comparison.csv");
        writer.write("HashFunction,Collisions,MaxBucketSize,EmptyBuckets\n");

        // Generate dataset
        List<String> dataset = generateStringDataset(dataSize, 5, 15);

        for (String hashFunction : hashFunctions) {
            // Set hash function
            SimpleHashMap.setHashFunctionType(hashFunction);

            // Create HashMap
            SimpleHashMap<String, Boolean> map = new SimpleHashMap<>(mapSize);

            // Insert all data
            for (String item : dataset) {
                map.put(item, true);
            }

            // Get metrics
            int collisions = map.getCollisionCount();
            int[] distribution = map.getBucketDistribution();

            // Find max bucket size
            int maxBucketSize = 0;
            int emptyBuckets = 0;
            for (int size : distribution) {
                maxBucketSize = Math.max(maxBucketSize, size);
                if (size == 0) emptyBuckets++;
            }

            // Write results
            writer.write(String.format("%s,%d,%d,%d\n",
                    hashFunction, collisions, maxBucketSize, emptyBuckets));
        }

        writer.close();
        System.out.println("Hash function experiment completed.");
    }

    /**
     * Run experiment to measure collisions with different hash map sizes
     */
    public static void runCollisionExperiment() throws IOException {
        int[] dataSizes = {1000, 5000, 10000, 20000};
        int[] mapSizes = {16, 32, 64, 128, 256, 512, 1024};

        // Create CSV file for string data
        FileWriter stringWriter = new FileWriter("string_collisions.csv");
        stringWriter.write("DataSize,MapSize,Collisions,LoadFactor\n");

        // Run experiment with string data
        for (int dataSize : dataSizes) {
            List<String> dataset = generateStringDataset(dataSize, 5, 15);

            for (int mapSize : mapSizes) {
                SimpleHashMap<String, Boolean> map = new SimpleHashMap<>(mapSize);

                // Insert all data
                for (String item : dataset) {
                    map.put(item, true);
                }

                // Record results
                stringWriter.write(String.format("%d,%d,%d,%.4f\n",
                        dataSize, mapSize, map.getCollisionCount(), map.getLoadFactor()));
            }
        }
        stringWriter.close();

        // Create CSV file for integer data
        FileWriter intWriter = new FileWriter("integer_collisions.csv");
        intWriter.write("DataSize,MapSize,Collisions,LoadFactor\n");

        // Run experiment with integer data
        for (int dataSize : dataSizes) {
            List<Integer> dataset = generateIntegerDataset(dataSize, 100000);

            for (int mapSize : mapSizes) {
                SimpleHashMap<Integer, Boolean> map = new SimpleHashMap<>(mapSize);

                // Insert all data
                for (Integer item : dataset) {
                    map.put(item, true);
                }

                // Record results
                intWriter.write(String.format("%d,%d,%d,%.4f\n",
                        dataSize, mapSize, map.getCollisionCount(), map.getLoadFactor()));
            }
        }
        intWriter.close();

        System.out.println("Collision experiment completed.");
    }

    /**
     * Run experiment to measure lookup performance
     */
    public static void runLookupExperiment() throws IOException {
        int[] dataSizes = {10000, 50000, 100000};
        int[] mapSizes = {16, 64, 256, 1024, 4096};
        int lookupCount = 10000;

        FileWriter writer = new FileWriter("lookup_performance.csv");
        writer.write("DataSize,MapSize,LoadFactor,LookupTimeMs\n");

        for (int dataSize : dataSizes) {
            List<String> dataset = generateStringDataset(dataSize, 5, 15);
            List<String> lookupKeys = dataset.subList(0, Math.min(lookupCount, dataSize));

            for (int mapSize : mapSizes) {
                SimpleHashMap<String, Boolean> map = new SimpleHashMap<>(mapSize);

                // Insert all data
                for (String item : dataset) {
                    map.put(item, true);
                }

                // Measure lookup time
                long startTime = System.nanoTime();
                for (String key : lookupKeys) {
                    map.get(key);
                }
                long endTime = System.nanoTime();

                double elapsedMs = (endTime - startTime) / 1_000_000.0;

                // Record results
                writer.write(String.format("%d,%d,%.4f,%.4f\n",
                        dataSize, mapSize, map.getLoadFactor(), elapsedMs));
            }
        }
        writer.close();

        System.out.println("Lookup experiment completed.");
    }

    /**
     * Run experiment to analyze bucket distribution
     */
    public static void runDistributionExperiment() throws IOException {
        int dataSize = 10000;
        int mapSize = 128;

        List<String> dataset = generateStringDataset(dataSize, 5, 15);
        SimpleHashMap<String, Boolean> map = new SimpleHashMap<>(mapSize);

        // Insert all data
        for (String item : dataset) {
            map.put(item, true);
        }

        // Get bucket distribution
        int[] distribution = map.getBucketDistribution();

        // Write distribution to CSV
        FileWriter writer = new FileWriter("bucket_distribution.csv");
        writer.write("BucketIndex,ItemCount\n");

        for (int i = 0; i < distribution.length; i++) {
            writer.write(String.format("%d,%d\n", i, distribution[i]));
        }
        writer.close();

        System.out.println("Distribution experiment completed.");
    }

    /**
     * Compare with Java's HashMap
     */
    public static void compareWithJavaHashMap() throws IOException {
        int[] dataSizes = {10000, 50000, 100000};
        int lookupCount = 10000;

        FileWriter writer = new FileWriter("hashmap_comparison.csv");
        writer.write("DataSize,SimpleHashMapTimeMs,JavaHashMapTimeMs\n");

        for (int dataSize : dataSizes) {
            List<String> dataset = generateStringDataset(dataSize, 5, 15);
            List<String> lookupKeys = dataset.subList(0, Math.min(lookupCount, dataSize));

            // Test with SimpleHashMap
            SimpleHashMap<String, Boolean> simpleMap = new SimpleHashMap<>(1024);
            for (String item : dataset) {
                simpleMap.put(item, true);
            }

            long simpleStartTime = System.nanoTime();
            for (String key : lookupKeys) {
                simpleMap.get(key);
            }
            long simpleEndTime = System.nanoTime();
            double simpleElapsedMs = (simpleEndTime - simpleStartTime) / 1_000_000.0;

            // Test with Java HashMap
            java.util.HashMap<String, Boolean> javaMap = new java.util.HashMap<>(1024);
            for (String item : dataset) {
                javaMap.put(item, true);
            }

            long javaStartTime = System.nanoTime();
            for (String key : lookupKeys) {
                javaMap.get(key);
            }
            long javaEndTime = System.nanoTime();
            double javaElapsedMs = (javaEndTime - javaStartTime) / 1_000_000.0;

            // Record results
            writer.write(String.format("%d,%.4f,%.4f\n", dataSize, simpleElapsedMs, javaElapsedMs));
        }
        writer.close();

        System.out.println("HashMap comparison completed.");
    }

    /**
     * Run experiment to analyze text fingerprint collision patterns
     */
    public static void runTextFingerprintExperiment() throws IOException {
        // Sample texts with different characteristics
        String[] texts = {
                // Literature sample
                "It was the best of times, it was the worst of times, it was the age of wisdom, it was the age of foolishness, " +
                        "it was the epoch of belief, it was the epoch of incredulity, it was the season of Light, it was the season of Darkness...",

                // Technical sample
                "A hash function is any function that can be used to map data of arbitrary size to fixed-size values. " +
                        "The values returned by a hash function are called hash values, hash codes, digests, or simply hashes.",

                // Poetry sample
                "Two roads diverged in a yellow wood, And sorry I could not travel both And be one traveler, long I stood " +
                        "And looked down one as far as I could To where it bent in the undergrowth",

                // Code sample
                "public static void main(String[] args) { System.out.println(\"Hello, World!\"); " +
                        "for(int i=0; i<10; i++) { if(i % 2 == 0) { System.out.println(i); } } }"
        };

        String[] textTypes = {"Literature", "Technical", "Poetry", "Code"};
        int mapSize = 64;

        FileWriter writer = new FileWriter("text_fingerprint_analysis.csv");
        writer.write("TextType,Collisions,MaxCollisionLevel,UniqueWords,TotalWords\n");

        for (int i = 0; i < texts.length; i++) {
            // Create HashMap for this text
            SimpleHashMap<String, Integer> map = new SimpleHashMap<>(mapSize);

            // Process words
            String[] words = texts[i].split("\\s+");
            int uniqueWords = 0;
            java.util.HashSet<String> uniqueWordSet = new java.util.HashSet<>();

            for (String word : words) {
                word = word.toLowerCase().replaceAll("[^a-z]", "");
                if (!word.isEmpty()) {
                    if (!uniqueWordSet.contains(word)) {
                        uniqueWordSet.add(word);
                        uniqueWords++;
                    }
                    map.put(word, 1);
                }
            }

            // Get metrics
            int collisions = map.getCollisionCount();

            // Find max collision level (by analyzing bucket sizes)
            int[] distribution = map.getBucketDistribution();
            int maxBucketSize = 0;
            for (int size : distribution) {
                maxBucketSize = Math.max(maxBucketSize, size);
            }

            // Write results
            writer.write(String.format("%s,%d,%d,%d,%d\n",
                    textTypes[i], collisions, maxBucketSize, uniqueWords, words.length));
        }

        writer.close();
        System.out.println("Text fingerprint experiment completed.");
    }

    public static void main(String[] args) {
        try {
            System.out.println("Starting HashMap experiments...");

            runHashFunctionExperiment();
            runCollisionExperiment();
            runLookupExperiment();
            runDistributionExperiment();
            compareWithJavaHashMap();
            runTextFingerprintExperiment();

            System.out.println("All experiments completed successfully.");
        } catch (IOException e) {
            System.err.println("Error writing experiment results: " + e.getMessage());
        }
    }
}