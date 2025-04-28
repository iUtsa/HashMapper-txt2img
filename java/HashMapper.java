import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*; // Import java.util.* explicitly
import javax.imageio.ImageIO;
import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.data.xy.*;
import org.jfree.chart.renderer.xy.*;
import java.awt.BasicStroke;
/**
 * HashMapper: Visual Cryptanalysis Through Intentional Collisions
 *
 * This innovative project uses our deliberately weak dumbHash function to create 
 * visual fingerprints of data, enabling pattern recognition in texts, detecting authorship,
 * and identifying hidden structures in data through collision analysis.
 */
public class HashMapper {

    // Different hash function types
    private static String selectedHashFunction = "String Length";

    // Set the hash function type
    public static void setHashFunction(String hashFunctionType) {
        selectedHashFunction = hashFunctionType;
    }

    // Inner class for our dumb hash map implementation
    static class DumbHashMap<K, V> {

        private static class Entry<K, V> {
            K key;
            V value;
            int collisionLevel;  // Track how many collisions occurred for this entry

            Entry(K key, V value, int collisionLevel) {
                this.key = key;
                this.value = value;
                this.collisionLevel = collisionLevel;
            }
        }

        private ArrayList<Entry<K, V>>[] buckets;
        private final int size;
        private int collisions;
        private Map<Integer, Integer> collisionDistribution;
        private int maxCollisionLevel = 0;

        @SuppressWarnings("unchecked")
        public DumbHashMap(int size) {
            this.size = size;
            this.buckets = new ArrayList[size];
            this.collisions = 0;
            this.collisionDistribution = new HashMap<>();

            for (int i = 0; i < size; i++) {
                buckets[i] = new ArrayList<>();
            }
        }

        // Our intentionally poor hash function
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
                switch (selectedHashFunction) {
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

            // For integers, just mod with size (very poor distribution)
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

        public void put(K key, V value) {
            int index = dumbHash(key);
            ArrayList<Entry<K, V>> bucket = buckets[index];

            // Check if key already exists
            for (int i = 0; i < bucket.size(); i++) {
                if (Objects.equals(bucket.get(i).key, key)) {
                    bucket.get(i).value = value;
                    return;
                }
            }

            // New entry - check for collision and track collision level
            int collisionLevel = 0;
            if (!bucket.isEmpty()) {
                collisions++;
                collisionLevel = bucket.size();

                // Update collision distribution
                collisionDistribution.put(collisionLevel,
                        collisionDistribution.getOrDefault(collisionLevel, 0) + 1);

                // Track maximum collision level
                maxCollisionLevel = Math.max(maxCollisionLevel, collisionLevel);
            }

            bucket.add(new Entry<>(key, value, collisionLevel));
        }

        public V get(K key) {
            int index = dumbHash(key);
            ArrayList<Entry<K, V>> bucket = buckets[index];

            for (Entry<K, V> entry : bucket) {
                if (Objects.equals(entry.key, key)) {
                    return entry.value;
                }
            }

            return null; // Key not found
        }

        public int getCollisionCount() {
            return collisions;
        }

        public Map<Integer, Integer> getCollisionDistribution() {
            return collisionDistribution;
        }

        public int[] getBucketDistribution() {
            int[] distribution = new int[size];

            for (int i = 0; i < size; i++) {
                distribution[i] = buckets[i].size();
            }

            return distribution;
        }

        public int getMaxCollisionLevel() {
            return maxCollisionLevel;
        }

        public int getBucketSize(int index) {
            if (index < 0 || index >= size) {
                return 0;
            }
            return buckets[index].size();
        }

        // Get bucket entries for advanced analysis
        public java.util.List<Entry<K, V>> getBucketEntries(int index) {
            if (index < 0 || index >= size) {
                return new ArrayList<>();
            }
            return new ArrayList<>(buckets[index]);
        }

        // Get all entries for analysis
        public java.util.List<Entry<K, V>> getAllEntries() {
            java.util.List<Entry<K, V>> allEntries = new ArrayList<>();
            for (ArrayList<Entry<K, V>> bucket : buckets) {
                allEntries.addAll(bucket);
            }
            return allEntries;
        }
    }

    /**
     * Class for creating a visual fingerprint from text
     */
    static class TextVisualizer {

        // Process a text and generate a visual fingerprint
        public static BufferedImage createVisualFingerprint(String text, int size) {
            DumbHashMap<String, Integer> wordMap = new DumbHashMap<>(size);
            DumbHashMap<Character, Integer> charMap = new DumbHashMap<>(size);

            // Process words
            String[] words = text.split("\\s+");
            for (String word : words) {
                word = word.toLowerCase().replaceAll("[^a-z]", "");
                if (!word.isEmpty()) {
                    wordMap.put(word, 1);
                }
            }

            // Process characters
            for (char c : text.toCharArray()) {
                if (Character.isLetterOrDigit(c)) {
                    charMap.put(c, 1);
                }
            }

            // Create image
            BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();

            // Fill background
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, size, size);

            // Map bucket distribution to colors
            int[] wordDist = wordMap.getBucketDistribution();
            int[] charDist = charMap.getBucketDistribution();

            // Find max values for scaling
            int maxWord = 0;
            int maxChar = 0;
            for (int i = 0; i < size; i++) {
                maxWord = Math.max(maxWord, wordDist[i]);
                maxChar = Math.max(maxChar, charDist[i]);
            }

            // Draw fingerprint
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    // Calculate color intensity based on bucket sizes
                    int wordIntensity = (int) (255.0 * wordDist[i] / (maxWord > 0 ? maxWord : 1));
                    int charIntensity = (int) (255.0 * charDist[j] / (maxChar > 0 ? maxChar : 1));

                    // Create color: red channel from word distribution, blue from char distribution
                    Color color = new Color(
                            wordIntensity,
                            (wordIntensity + charIntensity) / 4,
                            charIntensity
                    );

                    image.setRGB(i, j, color.getRGB());
                }
            }

            g.dispose();
            return image;
        }

        // Apply salt and smooth algorithms to the fingerprint
        public static BufferedImage saltAndSmooth(BufferedImage original, double saltLevel, int smoothRadius) {
            int width = original.getWidth();
            int height = original.getHeight();

            // Create a copy of the original image
            BufferedImage result = new BufferedImage(width, height, original.getType());

            // Apply salt
            Random random = new Random();
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    if (random.nextDouble() < saltLevel) {
                        // Apply salt (random noise)
                        result.setRGB(x, y, new Color(
                                random.nextInt(256),
                                random.nextInt(256),
                                random.nextInt(256)
                        ).getRGB());
                    } else {
                        // Copy original pixel
                        result.setRGB(x, y, original.getRGB(x, y));
                    }
                }
            }

            // Smooth using a Gaussian-like blur
            BufferedImage smoothed = new BufferedImage(width, height, original.getType());
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    // Get average of nearby pixels
                    int totalR = 0, totalG = 0, totalB = 0;
                    int count = 0;

                    for (int dx = -smoothRadius; dx <= smoothRadius; dx++) {
                        for (int dy = -smoothRadius; dy <= smoothRadius; dy++) {
                            int nx = x + dx;
                            int ny = y + dy;

                            if (nx >= 0 && nx < width && ny >= 0 && ny < height) {
                                Color pixel = new Color(result.getRGB(nx, ny));
                                totalR += pixel.getRed();
                                totalG += pixel.getGreen();
                                totalB += pixel.getBlue();
                                count++;
                            }
                        }
                    }

                    // Set pixel to average color
                    Color avgColor = new Color(
                            totalR / count,
                            totalG / count,
                            totalB / count
                    );
                    smoothed.setRGB(x, y, avgColor.getRGB());
                }
            }

            return smoothed;
        }
    }

    /**
     * Class for analyzing text patterns using collision data
     */
    static class TextAnalyzer {

        // Analyze a text and return statistics
        public static Map<String, Object> analyzeText(String text, int mapSize) {
            DumbHashMap<String, Integer> wordFreq = new DumbHashMap<>(mapSize);

            // Process words and count frequency
            String[] words = text.split("\\s+");
            Map<String, Integer> actualFreq = new HashMap<>();

            for (String word : words) {
                word = word.toLowerCase().replaceAll("[^a-z]", "");
                if (!word.isEmpty()) {
                    actualFreq.put(word, actualFreq.getOrDefault(word, 0) + 1);
                    wordFreq.put(word, actualFreq.get(word));
                }
            }

            // Calculate statistics
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalWords", words.length);
            stats.put("uniqueWords", actualFreq.size());
            stats.put("collisions", wordFreq.getCollisionCount());
            stats.put("collisionDistribution", wordFreq.getCollisionDistribution());
            stats.put("bucketDistribution", wordFreq.getBucketDistribution());
            stats.put("maxCollisionLevel", wordFreq.getMaxCollisionLevel());

            return stats;
        }

        // Generate a collision spectrum chart and save to a file
        public static void generateCollisionSpectrum(Map<String, Object> stats, String outputFile) {
            // Extract collision distribution
            @SuppressWarnings("unchecked")
            Map<Integer, Integer> collisionDist = (Map<Integer, Integer>) stats.get("collisionDistribution");

            // Create dataset
            XYSeries series = new XYSeries("Collision Spectrum");
            for (Map.Entry<Integer, Integer> entry : collisionDist.entrySet()) {
                series.add(entry.getKey(), entry.getValue());
            }

            XYSeriesCollection dataset = new XYSeriesCollection();
            dataset.addSeries(series);

            // Create chart
            JFreeChart chart = ChartFactory.createXYLineChart(
                    "Text Collision Spectrum",
                    "Collision Level",
                    "Frequency",
                    dataset,
                    PlotOrientation.VERTICAL,
                    true,
                    true,
                    false
            );

            // Customize appearance
            XYPlot plot = chart.getXYPlot();
            XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
            renderer.setSeriesPaint(0, Color.RED);
            renderer.setSeriesStroke(0, new BasicStroke(2.0f));
            plot.setRenderer(renderer);

            // Save chart to file
            try {
                ChartUtils.saveChartAsPNG(new File(outputFile), chart, 800, 600);
            } catch (IOException e) {
                System.err.println("Error saving chart: " + e.getMessage());
                System.exit(1);
            }
        }

        // Compare two texts and calculate similarity based on collision patterns
        public static double calculateSimilarity(String text1, String text2, int mapSize) {
            Map<String, Object> stats1 = analyzeText(text1, mapSize);
            Map<String, Object> stats2 = analyzeText(text2, mapSize);

            int[] bucketDist1 = (int[]) stats1.get("bucketDistribution");
            int[] bucketDist2 = (int[]) stats2.get("bucketDistribution");

            // Calculate Euclidean distance between bucket distributions
            double sumSquaredDiff = 0;
            for (int i = 0; i < mapSize; i++) {
                sumSquaredDiff += Math.pow(bucketDist1[i] - bucketDist2[i], 2);
            }

            double distance = Math.sqrt(sumSquaredDiff);

            // Convert distance to similarity (inverse and normalized)
            Object uniqueWords1 = stats1.get("uniqueWords");
            Object uniqueWords2 = stats2.get("uniqueWords");
            double maxUniqueWords = Math.max(
                    (uniqueWords1 instanceof Number) ? ((Number) uniqueWords1).doubleValue() : 0,
                    (uniqueWords2 instanceof Number) ? ((Number) uniqueWords2).doubleValue() : 0
            );

            double maxPossibleDistance = Math.sqrt(mapSize * Math.pow(maxUniqueWords, 2));

            // Similarity between 0 and 1
            return 1.0 - (distance / maxPossibleDistance);
        }
    }

    /**
     * Plotter class that creates advanced visualizations using Octave-like styling
     */
    static class Plotter {

        // Create a 3D visualization of text fingerprint
        public static BufferedImage create3DPlot(int[] bucketDistribution, int size) {
            // Create a 3D representation of the bucket distribution
            double[][] data = new double[size][size];

            // Map the 1D bucket distribution to a 2D grid
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    // Create an interesting pattern using the bucket data
                    double value1 = bucketDistribution[i];
                    double value2 = bucketDistribution[j];

                    // Create a surface by combining values, avoid NaN
                    double product = value1 * value2;
                    data[i][j] = (product > 0) ? Math.sqrt(product) * Math.sin(i * j / (double) (size * size) * Math.PI) : 0;
                }
            }

            // Normalize data for better visualization
            double maxVal = Double.MIN_VALUE;
            double minVal = Double.MAX_VALUE;
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    maxVal = Math.max(maxVal, data[i][j]);
                    minVal = Math.min(minVal, data[i][j]);
                }
            }

            // Handle case where maxVal equals minVal to avoid division by zero
            if (maxVal == minVal) {
                maxVal = minVal + 1.0;
            }

            // Create a BufferedImage to visualize the 3D surface with color mapping
            BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = image.createGraphics();

            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    // Normalize value between 0 and 1
                    double normValue = (data[i][j] - minVal) / (maxVal - minVal);

                    // Use a color gradient (blue to red, through green)
                    Color color;
                    if (normValue < 0.5) {
                        // Blue to green
                        int green = (int) (normValue * 2 * 255);
                        color = new Color(0, green, 255 - green);
                    } else {
                        // Green to red
                        int red = (int) ((normValue - 0.5) * 2 * 255);
                        color = new Color(red, 255 - red, 0);
                    }

                    image.setRGB(i, j, color.getRGB());
                }
            }

            // Add a grid to make it look like an octave plot
            g2d.setColor(new Color(255, 255, 255, 50)); // Translucent white
            int gridSize = size / 20; // Scale grid to image size
            if (gridSize > 0) {
                for (int i = 0; i <= size; i += gridSize) {
                    g2d.drawLine(i, 0, i, size);
                    g2d.drawLine(0, i, size, i);
                }
            }

            g2d.dispose();
            return image;
        }

        // Create a color contour map of collision patterns
        public static BufferedImage createContourMap(int[] bucketDistribution, int size) {
            // Create a 2D grid for the contour map
            double[][] data = new double[size][size];

            // Map the 1D bucket distribution to a 2D grid using a wave equation
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    double x = i / (double) size;
                    double y = j / (double) size;

                    // Value from bucket distribution normalized
                    double bucketVal1 = bucketDistribution[i] / 10.0;
                    double bucketVal2 = bucketDistribution[j] / 10.0;

                    // Create wave pattern based on bucket values
                    data[i][j] = Math.sin(bucketVal1 * 2 * Math.PI * x) *
                            Math.cos(bucketVal2 * 2 * Math.PI * y) *
                            (bucketVal1 + bucketVal2) / 2.0;
                }
            }

            // Create image for the contour map
            BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);

            // Find min and max for normalization
            double min = Double.MAX_VALUE;
            double max = Double.MIN_VALUE;
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    min = Math.min(min, data[i][j]);
                    max = Math.max(max, data[i][j]);
                }
            }

            // Handle case where max equals min to avoid division by zero
            if (max == min) {
                max = min + 1.0;
            }

            // Create a contour map
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    // Normalize data
                    double value = (data[i][j] - min) / (max - min);

                    // Create contour lines by checking if value is close to a threshold
                    boolean isContour = false;
                    for (double threshold = 0.1; threshold < 1.0; threshold += 0.1) {
                        if (Math.abs(value - threshold) < 0.01) {
                            isContour = true;
                            break;
                        }
                    }

                    // Set pixel color based on value and contour lines
                    if (isContour) {
                        image.setRGB(i, j, Color.BLACK.getRGB());
                    } else {
                        // Use a gradient from blue to red
                        int red = (int) (value * 255);
                        int blue = (int) ((1 - value) * 255);
                        int green = (int) (Math.sin(value * Math.PI) * 255);

                        Color color = new Color(red, green, blue);
                        image.setRGB(i, j, color.getRGB());
                    }
                }
            }

            return image;
        }
    }
}