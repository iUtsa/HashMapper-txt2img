import java.io.*;

/**
 * Main class to run HashMap experiments and visualization
 */
public class HashMapExperimentRunner {
    public static void main(String[] args) {
        System.out.println("Starting HashMap Experiment...");

        try {
            // Parse command-line arguments
            String textFile = null;
            int size = 128;
            String hashFunction = "String Length";
            double saltLevel = 0.05;
            int smoothRadius = 2;
            String rawOutput = null;
            String enhancedOutput = null;
            String statsOutput = null;
            String experimentType = null;
            String output = null;

            for (int i = 0; i < args.length; i++) {
                switch (args[i]) {
                    case "--text-file":
                        textFile = args[++i];
                        break;
                    case "--size":
                        size = Integer.parseInt(args[++i]);
                        break;
                    case "--hash-function":
                        hashFunction = args[++i];
                        break;
                    case "--salt-level":
                        saltLevel = Double.parseDouble(args[++i]);
                        break;
                    case "--smooth-radius":
                        smoothRadius = Integer.parseInt(args[++i]);
                        break;
                    case "--raw-output":
                        rawOutput = args[++i];
                        break;
                    case "--enhanced-output":
                        enhancedOutput = args[++i];
                        break;
                    case "--stats-output":
                        statsOutput = args[++i];
                        break;
                    case "--type":
                        experimentType = args[++i];
                        break;
                    case "--output":
                        output = args[++i];
                        break;
                }
            }

            if (textFile != null && rawOutput != null && enhancedOutput != null && statsOutput != null) {
                // Generate text fingerprint
                System.out.println("Generating text fingerprint...");
                HashMapVisualizer.generateTextFingerprint(
                    textFile, size, hashFunction, saltLevel, smoothRadius,
                    rawOutput, enhancedOutput, statsOutput
                );
                System.out.println("Text fingerprint generation completed.");
            } else if (experimentType != null && output != null) {
                // Run experiments
                System.out.println("Running experiments...");
                HashMapExperiment.main(args);

                // Generate visualizations
                System.out.println("Generating visualizations...");
                switch (experimentType) {
                    case "hash_function":
                        HashMapVisualizer.visualizeHashFunctionComparison("hash_function_comparison.csv", output);
                        break;
                    case "collision":
                        HashMapVisualizer.visualizeCollisions("string_collisions.csv", "String Key Collisions", output.replace(".png", "_string.png"));
                        HashMapVisualizer.visualizeCollisions("integer_collisions.csv", "Integer Key Collisions", output.replace(".png", "_integer.png"));
                        break;
                    case "lookup":
                        HashMapVisualizer.visualizeLookupPerformance("lookup_performance.csv", output);
                        break;
                    case "distribution":
                        HashMapVisualizer.visualizeBucketDistribution("bucket_distribution.csv", output);
                        break;
                    case "comparison":
                        HashMapVisualizer.visualizeHashMapComparison("hashmap_comparison.csv", output);
                        break;
                    case "text_fingerprint":
                        HashMapVisualizer.visualizeTextFingerprintAnalysis("text_fingerprint_analysis.csv", output);
                        break;
                    default:
                        System.err.println("Unknown experiment type: " + experimentType);
                        System.exit(1);
                }
                System.out.println("Visualizations completed.");
            } else {
                System.err.println("Invalid arguments");
                System.exit(1);
            }

            System.out.println("Experiment completed successfully!");
        } catch (Exception e) {
            System.err.println("Error during experiment: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}