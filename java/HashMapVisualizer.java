import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.nio.charset.MalformedInputException;

/**
 * Class to visualize the results of our HashMap experiments
 */
public class HashMapVisualizer {

    /**
     * Create a visualization of collision data and save to a file
     */
    public static void visualizeCollisions(String csvFile, String title, String outputFile) {
        // Read data from CSV file
        java.util.Map<Integer, java.util.Map<Integer, Integer>> collisionData = new java.util.HashMap<>();
        java.util.List<Integer> mapSizes = new java.util.ArrayList<>();
        java.util.List<Integer> dataSizes = new java.util.ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line = br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                int dataSize = Integer.parseInt(values[0]);
                int mapSize = Integer.parseInt(values[1]);
                int collisions = Integer.parseInt(values[2]);

                if (!dataSizes.contains(dataSize)) {
                    dataSizes.add(dataSize);
                }
                if (!mapSizes.contains(mapSize)) {
                    mapSizes.add(mapSize);
                }

                collisionData.computeIfAbsent(dataSize, k -> new java.util.HashMap<>()).put(mapSize, collisions);
            }
        } catch (IOException e) {
            System.err.println("Error reading CSV file: " + e.getMessage());
            System.exit(1);
        }

        // Create BufferedImage
        int width = 800;
        int height = 600;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);

        int padding = 50;

        // Draw axes
        g2d.setColor(Color.BLACK);
        g2d.drawLine(padding, height - padding, width - padding, height - padding); // x-axis
        g2d.drawLine(padding, height - padding, padding, padding); // y-axis

        // Draw x-axis labels
        FontMetrics fm = g2d.getFontMetrics();
        for (int i = 0; i < mapSizes.size(); i++) {
            int mapSize = mapSizes.get(i);
            String label = String.valueOf(mapSize);
            int labelWidth = fm.stringWidth(label);
            float x = padding + i * (width - 2 * padding) / (mapSizes.size() - 1);
            g2d.drawString(label, x - labelWidth / 2, height - padding / 2);
        }

        // Find max collision value for scaling
        int maxCollisions = 0;
        for (java.util.Map<Integer, Integer> dataMap : collisionData.values()) {
            for (int collisions : dataMap.values()) {
                maxCollisions = Math.max(maxCollisions, collisions);
            }
        }

        // Draw lines for each data size
        Color[] colors = {Color.RED, Color.BLUE, Color.GREEN, Color.ORANGE, Color.MAGENTA};
        for (int i = 0; i < dataSizes.size(); i++) {
            int dataSize = dataSizes.get(i);
            g2d.setColor(colors[i % colors.length]);

            int prevX = 0;
            int prevY = 0;
            boolean first = true;

            for (int j = 0; j < mapSizes.size(); j++) {
                int mapSize = mapSizes.get(j);
                int collisions = collisionData.get(dataSize).get(mapSize);

                float x = padding + j * (width - 2 * padding) / (mapSizes.size() - 1);
                float y = height - padding - (collisions * (height - 2 * padding) / maxCollisions);

                g2d.fillOval((int) x - 3, (int) y - 3, 6, 6);
                if (!first) {
                    g2d.drawLine(prevX, prevY, (int) x, (int) y);
                }

                prevX = (int) x;
                prevY = (int) y;
                first = false;
            }
        }

        // Draw legend
        int legendX = width - 200;
        int legendY = 50;
        for (int i = 0; i < dataSizes.size(); i++) {
            g2d.setColor(colors[i % colors.length]);
            g2d.fillRect(legendX, legendY + i * 20, 10, 10);
            g2d.setColor(Color.BLACK);
            g2d.drawString("Data Size: " + dataSizes.get(i), legendX + 20, legendY + i * 20 + 10);
        }

        // Draw titles
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        String xAxisTitle = "HashMap Size";
        String yAxisTitle = "Number of Collisions";

        g2d.drawString(xAxisTitle, width / 2 - fm.stringWidth(xAxisTitle) / 2, height - 10);
        g2d.translate(15, height / 2 + fm.stringWidth(yAxisTitle) / 2);
        g2d.rotate(-Math.PI / 2);
        g2d.drawString(yAxisTitle, 0, 0);
        g2d.rotate(Math.PI / 2);
        g2d.translate(-15, -(height / 2 + fm.stringWidth(yAxisTitle) / 2));

        g2d.dispose();

        // Save image
        try {
            ImageIO.write(image, "png", new File(outputFile));
        } catch (IOException e) {
            System.err.println("Error saving image: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Create a visualization of lookup performance and save to a file
     */
    public static void visualizeLookupPerformance(String csvFile, String outputFile) {
        // Read data from CSV file
        java.util.Map<Integer, java.util.Map<Integer, Double>> lookupData = new java.util.HashMap<>();
        java.util.List<Integer> mapSizes = new java.util.ArrayList<>();
        java.util.List<Integer> dataSizes = new java.util.ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                int dataSize = Integer.parseInt(values[0]);
                int mapSize = Integer.parseInt(values[1]);
                double lookupTime = Double.parseDouble(values[3]);

                if (!dataSizes.contains(dataSize)) {
                    dataSizes.add(dataSize);
                }
                if (!mapSizes.contains(mapSize)) {
                    mapSizes.add(mapSize);
                }

                lookupData.computeIfAbsent(dataSize, k -> new java.util.HashMap<>()).put(mapSize, lookupTime);
            }
        } catch (IOException e) {
            System.err.println("Error reading CSV file: " + e.getMessage());
            System.exit(1);
        }

        // Create BufferedImage
        int width = 800;
        int height = 600;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);

        int padding = 50;

        // Draw axes
        g2d.setColor(Color.BLACK);
        g2d.drawLine(padding, height - padding, width - padding, height - padding);
        g2d.drawLine(padding, height - padding, padding, padding);

        // Draw x-axis labels
        FontMetrics fm = g2d.getFontMetrics();
        for (int i = 0; i < mapSizes.size(); i++) {
            int mapSize = mapSizes.get(i);
            String label = String.valueOf(mapSize);
            int labelWidth = fm.stringWidth(label);
            float x = padding + i * (width - 2 * padding) / (mapSizes.size() - 1);
            g2d.drawString(label, x - labelWidth / 2, height - padding / 2);
        }

        // Find max lookup time for scaling
        double maxLookupTime = 0;
        for (java.util.Map<Integer, Double> dataMap : lookupData.values()) {
            for (double lookupTime : dataMap.values()) {
                maxLookupTime = Math.max(maxLookupTime, lookupTime);
            }
        }

        // Draw lines for each data size
        Color[] colors = {Color.RED, Color.BLUE, Color.GREEN, Color.ORANGE, Color.MAGENTA};
        for (int i = 0; i < dataSizes.size(); i++) {
            int dataSize = dataSizes.get(i);
            g2d.setColor(colors[i % colors.length]);

            int prevX = 0;
            int prevY = 0;
            boolean first = true;

            for (int j = 0; j < mapSizes.size(); j++) {
                int mapSize = mapSizes.get(j);
                double lookupTime = lookupData.get(dataSize).get(mapSize);

                float x = padding + j * (width - 2 * padding) / (mapSizes.size() - 1);
                float y = height - padding - (float) (lookupTime * (height - 2 * padding) / maxLookupTime);

                g2d.fillOval((int) x - 3, (int) y - 3, 6, 6);
                if (!first) {
                    g2d.drawLine(prevX, prevY, (int) x, (int) y);
                }

                prevX = (int) x;
                prevY = (int) y;
                first = false;
            }
        }

        // Draw legend
        int legendX = width - 200;
        int legendY = 50;
        for (int i = 0; i < dataSizes.size(); i++) {
            g2d.setColor(colors[i % colors.length]);
            g2d.fillRect(legendX, legendY + i * 20, 10, 10);
            g2d.setColor(Color.BLACK);
            g2d.drawString("Data Size: " + dataSizes.get(i), legendX + 20, legendY + i * 20 + 10);
        }

        // Draw titles
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        String xAxisTitle = "HashMap Size";
        String yAxisTitle = "Lookup Time (ms)";

        g2d.drawString(xAxisTitle, width / 2 - fm.stringWidth(xAxisTitle) / 2, height - 10);
        g2d.translate(15, height / 2 + fm.stringWidth(yAxisTitle) / 2);
        g2d.rotate(-Math.PI / 2);
        g2d.drawString(yAxisTitle, 0, 0);
        g2d.rotate(Math.PI / 2);
        g2d.translate(-15, -(height / 2 + fm.stringWidth(yAxisTitle) / 2));

        g2d.dispose();

        // Save image
        try {
            ImageIO.write(image, "png", new File(outputFile));
        } catch (IOException e) {
            System.err.println("Error saving image: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Create a visualization of bucket distribution and save to a file
     */
    public static void visualizeBucketDistribution(String csvFile, String outputFile) {
        // Read data from CSV file
        java.util.List<Integer> bucketCounts = new java.util.ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                int count = Integer.parseInt(values[1]);
                bucketCounts.add(count);
            }
        } catch (IOException e) {
            System.err.println("Error reading CSV file: " + e.getMessage());
            System.exit(1);
        }

        // Create BufferedImage
        int width = 800;
        int height = 600;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);

        int padding = 50;

        // Draw axes
        g2d.setColor(Color.BLACK);
        g2d.drawLine(padding, height - padding, width - padding, height - padding);
        g2d.drawLine(padding, height - padding, padding, padding);

        // Find max bucket count for scaling
        int maxCount = Math.max(Collections.max(bucketCounts), 0);

        // Calculate bar width
        int barWidth = (width - 2 * padding) / bucketCounts.size();

        // Draw bars
        g2d.setColor(Color.BLUE);
        for (int i = 0; i < bucketCounts.size(); i++) {
            int count = bucketCounts.get(i);
            int barHeight = (int) ((double) count / maxCount * (height - 2 * padding));
            int x = padding + i * barWidth;
            g2d.fillRect(x, height - padding - barHeight, barWidth - 2, barHeight);
        }

        // Draw axes labels
        for (int i = 0; i < bucketCounts.size(); i += bucketCounts.size() / 10) {
            int x = padding + i * barWidth;
            g2d.drawString(String.valueOf(i), x, height - padding + 15);
        }

        // Y-axis scale
        for (int i = 0; i <= 10; i++) {
            int y = height - padding - i * (height - 2 * padding) / 10;
            int value = i * maxCount / 10;
            g2d.drawString(String.valueOf(value), padding - 30, y + 5);
            g2d.drawLine(padding - 5, y, padding, y);
        }

        // Draw titles
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        String xAxisTitle = "Bucket Index";
        String yAxisTitle = "Number of Items";

        g2d.drawString(xAxisTitle, width / 2 - g2d.getFontMetrics().stringWidth(xAxisTitle) / 2, height - 10);
        g2d.translate(15, height / 2 + g2d.getFontMetrics().stringWidth(yAxisTitle) / 2);
        g2d.rotate(-Math.PI / 2);
        g2d.drawString(yAxisTitle, 0, 0);
        g2d.rotate(Math.PI / 2);
        g2d.translate(-15, -(height / 2 + g2d.getFontMetrics().stringWidth(yAxisTitle) / 2));

        g2d.dispose();

        // Save image
        try {
            ImageIO.write(image, "png", new File(outputFile));
        } catch (IOException e) {
            System.err.println("Error saving image: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Create a visualization comparing our HashMap with Java's HashMap and save to a file
     */
    public static void visualizeHashMapComparison(String csvFile, String outputFile) {
        // Read data from CSV file
        java.util.List<Integer> dataSizes = new java.util.ArrayList<>();
        java.util.List<Double> simpleHashMapTimes = new java.util.ArrayList<>();
        java.util.List<Double> javaHashMapTimes = new java.util.ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                int dataSize = Integer.parseInt(values[0]);
                double simpleTime = Double.parseDouble(values[1]);
                double javaTime = Double.parseDouble(values[2]);

                dataSizes.add(dataSize);
                simpleHashMapTimes.add(simpleTime);
                javaHashMapTimes.add(javaTime);
            }
        } catch (IOException e) {
            System.err.println("Error reading CSV file: " + e.getMessage());
            System.exit(1);
        }

        // Create BufferedImage
        int width = 800;
        int height = 600;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);

        int padding = 50;

        // Draw axes
        g2d.setColor(Color.BLACK);
        g2d.drawLine(padding, height - padding, width - padding, height - padding);
        g2d.drawLine(padding, height - padding, padding, padding);

        // Find max lookup time value for scaling
        double maxTime = Math.max(
            simpleHashMapTimes.stream().mapToDouble(Double::doubleValue).max().orElse(0),
            javaHashMapTimes.stream().mapToDouble(Double::doubleValue).max().orElse(0)
        );

        // Draw data points and lines
        g2d.setColor(Color.RED);
        drawLine(g2d, dataSizes, simpleHashMapTimes, maxTime, width, height, padding);

        g2d.setColor(Color.BLUE);
        drawLine(g2d, dataSizes, javaHashMapTimes, maxTime, width, height, padding);

        // Draw x-axis labels
        for (int i = 0; i < dataSizes.size(); i++) {
            int dataSize = dataSizes.get(i);
            String label = String.valueOf(dataSize);
            int labelWidth = g2d.getFontMetrics().stringWidth(label);
            float x = padding + i * (width - 2 * padding) / (dataSizes.size() - 1);
            g2d.setColor(Color.BLACK);
            g2d.drawString(label, x - labelWidth / 2, height - padding + 15);
        }

        // Draw legend
        g2d.setColor(Color.RED);
        g2d.fillRect(width - 200, 50, 10, 10);
        g2d.setColor(Color.BLACK);
        g2d.drawString("SimpleHashMap", width - 180, 60);

        g2d.setColor(Color.BLUE);
        g2d.fillRect(width - 200, 70, 10, 10);
        g2d.setColor(Color.BLACK);
        g2d.drawString("Java HashMap", width - 180, 80);

        // Draw titles
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        String xAxisTitle = "Data Size";
        String yAxisTitle = "Lookup Time (ms)";

        g2d.drawString(xAxisTitle, width / 2 - g2d.getFontMetrics().stringWidth(xAxisTitle) / 2, height - 10);
        g2d.translate(15, height / 2 + g2d.getFontMetrics().stringWidth(yAxisTitle) / 2);
        g2d.rotate(-Math.PI / 2);
        g2d.drawString(yAxisTitle, 0, 0);
        g2d.rotate(Math.PI / 2);
        g2d.translate(-15, -(height / 2 + g2d.getFontMetrics().stringWidth(yAxisTitle) / 2));

        g2d.dispose();

        // Save image
        try {
            ImageIO.write(image, "png", new File(outputFile));
        } catch (IOException e) {
            System.err.println("Error saving image: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void drawLine(Graphics2D g2d, java.util.List<Integer> xValues, java.util.List<Double> yValues,
                                 double maxY, int width, int height, int padding) {
        int prevX = 0;
        int prevY = 0;
        boolean first = true;

        for (int i = 0; i < xValues.size(); i++) {
            float x = padding + i * (width - 2 * padding) / (xValues.size() - 1);
            float y = height - padding - (float) (yValues.get(i) * (height - 2 * padding) / maxY);

            g2d.fillOval((int) x - 3, (int) y - 3, 6, 6);
            if (!first) {
                g2d.drawLine(prevX, prevY, (int) x, (int) y);
            }

            prevX = (int) x;
            prevY = (int) y;
            first = false;
        }
    }

    /**
     * Visualize hash function comparison data and save to a file
     */
    public static void visualizeHashFunctionComparison(String csvFile, String outputFile) {
        // Read data from CSV file
        java.util.List<String> hashFunctions = new java.util.ArrayList<>();
        java.util.List<Integer> collisions = new java.util.ArrayList<>();
        java.util.List<Integer> maxBucketSizes = new java.util.ArrayList<>();
        java.util.List<Integer> emptyBuckets = new java.util.ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                hashFunctions.add(values[0]);
                collisions.add(Integer.parseInt(values[1]));
                maxBucketSizes.add(Integer.parseInt(values[2]));
                emptyBuckets.add(Integer.parseInt(values[3]));
            }
        } catch (IOException e) {
            System.err.println("Error reading CSV file: " + e.getMessage());
            System.exit(1);
        }

        // Create BufferedImage
        int width = 800;
        int height = 600;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);

        int padding = 70;

        // Draw axes
        g2d.setColor(Color.BLACK);
        g2d.drawLine(padding, height - padding, width - padding, height - padding);
        g2d.drawLine(padding, height - padding, padding, padding);

        // Find max values for scaling
        int maxCollisions = Math.max(Collections.max(collisions), 0);
        int maxBucketSize = Math.max(Collections.max(maxBucketSizes), 0);
        int maxEmptyBuckets = Math.max(Collections.max(emptyBuckets), 0);

        // Calculate bar width and spacing
        int totalFunctions = hashFunctions.size();
        int groupWidth = (width - 2 * padding) / totalFunctions;
        int barWidth = groupWidth / 3 - 4;

        // Draw bars for each hash function
        for (int i = 0; i < totalFunctions; i++) {
            int x = padding + i * groupWidth;

            int collisionHeight = (int) ((double) collisions.get(i) / maxCollisions * (height - 2 * padding));
            g2d.setColor(Color.RED);
            g2d.fillRect(x, height - padding - collisionHeight, barWidth, collisionHeight);

            int bucketSizeHeight = (int) ((double) maxBucketSizes.get(i) / maxBucketSize * (height - 2 * padding));
            g2d.setColor(Color.BLUE);
            g2d.fillRect(x + barWidth + 2, height - padding - bucketSizeHeight, barWidth, bucketSizeHeight);

            int emptyBucketHeight = (int) ((double) emptyBuckets.get(i) / maxEmptyBuckets * (height - 2 * padding));
            g2d.setColor(Color.GREEN);
            g2d.fillRect(x + 2 * barWidth + 4, height - padding - emptyBucketHeight, barWidth, emptyBucketHeight);

            g2d.setColor(Color.BLACK);
            String label = hashFunctions.get(i);
            int labelWidth = g2d.getFontMetrics().stringWidth(label);
            g2d.translate(x + groupWidth / 2, height - padding + 15);
            g2d.rotate(Math.PI / 4);
            g2d.drawString(label, -labelWidth / 2, 0);
            g2d.rotate(-Math.PI / 4);
            g2d.translate(-(x + groupWidth / 2), -(height - padding + 15));
        }

        // Draw legend
        g2d.setColor(Color.RED);
        g2d.fillRect(width - 180, 40, 10, 10);
        g2d.setColor(Color.BLACK);
        g2d.drawString("Collisions", width - 160, 50);

        g2d.setColor(Color.BLUE);
        g2d.fillRect(width - 180, 60, 10, 10);
        g2d.setColor(Color.BLACK);
        g2d.drawString("Max Bucket Size", width - 160, 70);

        g2d.setColor(Color.GREEN);
        g2d.fillRect(width - 180, 80, 10, 10);
        g2d.setColor(Color.BLACK);
        g2d.drawString("Empty Buckets", width - 160, 90);

        // Draw titles
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        String xAxisTitle = "Hash Functions";
        String yAxisTitle = "Values (normalized)";

        g2d.drawString(xAxisTitle, width / 2 - g2d.getFontMetrics().stringWidth(xAxisTitle) / 2, height - 10);
        g2d.translate(15, height / 2 + g2d.getFontMetrics().stringWidth(yAxisTitle) / 2);
        g2d.rotate(-Math.PI / 2);
        g2d.drawString(yAxisTitle, 0, 0);
        g2d.rotate(Math.PI / 2);
        g2d.translate(-15, -(height / 2 + g2d.getFontMetrics().stringWidth(yAxisTitle) / 2));

        g2d.dispose();

        // Save image
        try {
            ImageIO.write(image, "png", new File(outputFile));
        } catch (IOException e) {
            System.err.println("Error saving image: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Visualize text fingerprint analysis and save to a file
     */
    public static void visualizeTextFingerprintAnalysis(String csvFile, String outputFile) {
        // Read data from CSV file
        java.util.List<String> textTypes = new java.util.ArrayList<>();
        java.util.List<Integer> collisions = new java.util.ArrayList<>();
        java.util.List<Integer> maxLevels = new java.util.ArrayList<>();
        java.util.List<Integer> uniqueWords = new java.util.ArrayList<>();
        java.util.List<Integer> totalWords = new java.util.ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                textTypes.add(values[0]);
                collisions.add(Integer.parseInt(values[1]));
                maxLevels.add(Integer.parseInt(values[2]));
                uniqueWords.add(Integer.parseInt(values[3]));
                totalWords.add(Integer.parseInt(values[4]));
            }
        } catch (IOException e) {
            System.err.println("Error reading CSV file: " + e.getMessage());
            System.exit(1);
        }

        // Create BufferedImage
        int width = 800;
        int height = 600;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);

        int padding = 70;

        // Draw axes
        g2d.setColor(Color.BLACK);
        g2d.drawLine(padding, height - padding, width - padding, height - padding);
        g2d.drawLine(padding, height - padding, padding, padding);

        // Find max values for scaling
        int maxCollision = Math.max(Collections.max(collisions), 0);
        int maxLevel = Math.max(Collections.max(maxLevels), 0);
        int maxUniqueWord = Math.max(Collections.max(uniqueWords), 0);

        // Calculate bar width and spacing
        int totalTypes = textTypes.size();
        int groupWidth = (width - 2 * padding) / totalTypes;
        int barWidth = groupWidth / 3 - 4;

        // Draw bars for each text type
        for (int i = 0; i < totalTypes; i++) {
            int x = padding + i * groupWidth;

            int collisionHeight = (int) ((double) collisions.get(i) / maxCollision * (height - 2 * padding));
            g2d.setColor(Color.RED);
            g2d.fillRect(x, height - padding - collisionHeight, barWidth, collisionHeight);

            int levelHeight = (int) ((double) maxLevels.get(i) / maxLevel * (height - 2 * padding));
            g2d.setColor(Color.BLUE);
            g2d.fillRect(x + barWidth + 2, height - padding - levelHeight, barWidth, levelHeight);

            double ratio = (double) uniqueWords.get(i) / totalWords.get(i);
            int ratioHeight = (int) (ratio * (height - 2 * padding));
            g2d.setColor(Color.GREEN);
            g2d.fillRect(x + 2 * barWidth + 4, height - padding - ratioHeight, barWidth, ratioHeight);

            g2d.setColor(Color.BLACK);
            String label = textTypes.get(i);
            g2d.drawString(label, x + groupWidth / 2 - g2d.getFontMetrics().stringWidth(label) / 2, height - padding + 15);
        }

        // Draw legend
        g2d.setColor(Color.RED);
        g2d.fillRect(width - 180, 40, 10, 10);
        g2d.setColor(Color.BLACK);
        g2d.drawString("Collisions", width - 160, 50);

        g2d.setColor(Color.BLUE);
        g2d.fillRect(width - 180, 60, 10, 10);
        g2d.setColor(Color.BLACK);
        g2d.drawString("Max Collision Level", width - 160, 70);

        g2d.setColor(Color.GREEN);
        g2d.fillRect(width - 180, 80, 10, 10);
        g2d.setColor(Color.BLACK);
        g2d.drawString("Unique/Total Ratio", width - 160, 90);

        // Draw titles
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        String xAxisTitle = "Text Types";
        String yAxisTitle = "Values (normalized)";

        g2d.drawString(xAxisTitle, width / 2 - g2d.getFontMetrics().stringWidth(xAxisTitle) / 2, height - 10);
        g2d.translate(15, height / 2 + g2d.getFontMetrics().stringWidth(yAxisTitle) / 2);
        g2d.rotate(-Math.PI / 2);
        g2d.drawString(yAxisTitle, 0, 0);
        g2d.rotate(Math.PI / 2);
        g2d.translate(-15, -(height / 2 + g2d.getFontMetrics().stringWidth(yAxisTitle) / 2));

        g2d.dispose();

        // Save image
        try {
            ImageIO.write(image, "png", new File(outputFile));
        } catch (IOException e) {
            System.err.println("Error saving image: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Generate text fingerprint and enhanced fingerprint
     */
    public static void generateTextFingerprint(String textFile, int size, String hashFunction,
                                               double saltLevel, int smoothRadius,
                                               String rawOutput, String enhancedOutput, String statsOutput) {
        try {
            // Validate input file
            File file = new File(textFile);
            if (!file.exists() || !file.isFile()) {
                throw new IOException("Input file does not exist or is not a regular file: " + textFile);
            }
            if (file.length() == 0) {
                throw new IOException("Input file is empty: " + textFile);
            }

            // Check if the file is likely a binary file (e.g., PNG)
            byte[] firstBytes = Files.readAllBytes(file.toPath());
            if (firstBytes.length >= 4 && firstBytes[0] == (byte) 0x89 && firstBytes[1] == (byte) 0x50 &&
                firstBytes[2] == (byte) 0x4E && firstBytes[3] == (byte) 0x47) {
                throw new IOException("Input file appears to be a PNG image, not a text file: " + textFile);
            }

            // Read input text with explicit UTF-8 encoding
            String text;
            try {
                text = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            } catch (MalformedInputException e) {
                throw new IOException("Input file is not valid UTF-8 encoded text: " + textFile, e);
            }

            // Set hash function
            HashMapper.setHashFunction(hashFunction);

            // Generate raw fingerprint
            BufferedImage rawImage = HashMapper.TextVisualizer.createVisualFingerprint(text, size);
            ImageIO.write(rawImage, "png", new File(rawOutput));

            // Generate enhanced fingerprint
            BufferedImage enhancedImage = HashMapper.TextVisualizer.saltAndSmooth(rawImage, saltLevel, smoothRadius);
            ImageIO.write(enhancedImage, "png", new File(enhancedOutput));

            // Generate stats using TextAnalyzer
            Map<String, Object> analysis = HashMapper.TextAnalyzer.analyzeText(text, size);
            String statsJson = String.format(
                "{\"text_length\":%d,\"hash_function\":\"%s\",\"salt_level\":%.2f,\"smooth_radius\":%d," +
                "\"total_words\":%d,\"unique_words\":%d,\"collisions\":%d,\"max_collision_level\":%d}",
                text.length(), hashFunction, saltLevel, smoothRadius,
                (Integer) analysis.get("totalWords"), (Integer) analysis.get("uniqueWords"),
                (Integer) analysis.get("collisions"), (Integer) analysis.get("maxCollisionLevel")
            );
            try (FileWriter writer = new FileWriter(statsOutput)) {
                writer.write(statsJson);
            }
        } catch (IOException e) {
            System.err.println("Error generating fingerprint: " + e.getMessage());
            System.exit(1);
        }
    }
}