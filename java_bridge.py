import subprocess
import os
import json
import tempfile
import time
from PIL import Image
import io
import base64

def generate_fingerprint(text, size, hash_function, salt_level, smooth_radius):
    """
    Generate fingerprint using Java code
    Returns: (raw_image_bytes, enhanced_image_bytes, stats_dict)
    """
    text_path = None
    raw_output = None
    enhanced_output = None
    stats_output = None

    try:
        # Create a temporary directory to store all files
        temp_dir = tempfile.mkdtemp()
        
        # Write text to a temporary file
        text_path = os.path.join(temp_dir, 'input.txt')
        with open(text_path, 'w', encoding='utf-8') as text_file:
            text_file.write(text)
        
        # Define output paths
        raw_output = os.path.join(temp_dir, "raw_output.png")
        enhanced_output = os.path.join(temp_dir, "enhanced_output.png")
        stats_output = os.path.join(temp_dir, "stats_output.json")
        
        print(f"Working directory: {os.getcwd()}")
        print(f"Text file: {text_path}")
        print(f"Output files: {raw_output}, {enhanced_output}, {stats_output}")
        
        # Get the path to the java directory
        java_dir = os.path.join(os.getcwd(), 'java')
        
        # Build the command
        cmd = [
            "java",
            "-Djava.awt.headless=true",  # Enable headless mode for server environments
            "-cp", f"{java_dir}:lib/*",  # Include java directory and all JARs in lib
            "HashMapVisualizer",  # The main class
            "generateTextFingerprint",  # The method to call
            text_path,
            str(size),
            hash_function,
            str(salt_level),
            str(smooth_radius),
            raw_output,
            enhanced_output,
            stats_output
        ]
        
        # For Windows, use semicolons instead of colons in classpath
        if os.name == 'nt':
            cmd[3] = f"{java_dir};lib/*"
        
        print(f"Executing command: {' '.join(cmd)}")
        
        # Run the Java process
        result = subprocess.run(
            cmd,
            capture_output=True,
            text=True,
            timeout=30
        )
        
        print(f"Java process completed with return code: {result.returncode}")
        print(f"stdout: {result.stdout}")
        print(f"stderr: {result.stderr}")
        
        if result.returncode != 0:
            raise Exception(f"Java process failed: {result.stderr}")
        
        # Check if output files exist
        for file_path in [raw_output, enhanced_output, stats_output]:
            if not os.path.exists(file_path):
                raise FileNotFoundError(f"Output file not found: {file_path}")
            print(f"File exists: {file_path}, size: {os.path.getsize(file_path)} bytes")
        
        # Read the output files
        with open(raw_output, 'rb') as f:
            raw_bytes = f.read()
            
        with open(enhanced_output, 'rb') as f:
            enhanced_bytes = f.read()
            
        with open(stats_output, 'r') as f:
            stats = json.load(f)
            
        return raw_bytes, enhanced_bytes, stats
    
    except Exception as e:
        print(f"Error in generate_fingerprint: {str(e)}")
        raise
    
    finally:
        # Clean up temporary files
        for file_path in [text_path, raw_output, enhanced_output, stats_output]:
            if file_path and os.path.exists(file_path):
                try:
                    os.remove(file_path)
                    print(f"Deleted file: {file_path}")
                except Exception as e:
                    print(f"Failed to delete {file_path}: {str(e)}")
        
        # Remove the temporary directory
        if temp_dir and os.path.exists(temp_dir):
            try:
                import shutil
                shutil.rmtree(temp_dir)
                print(f"Deleted temporary directory: {temp_dir}")
            except Exception as e:
                print(f"Failed to delete temporary directory {temp_dir}: {str(e)}")

def run_experiment(experiment_type):
    """
    Run HashMap experiment and return the visualization
    Returns: image bytes
    """
    output_file = None
    
    try:
        # Create a temporary directory
        temp_dir = tempfile.mkdtemp()
        output_file = os.path.join(temp_dir, f"{experiment_type}_output.png")
        
        # Get the path to the java directory
        java_dir = os.path.join(os.getcwd(), 'java')
        
        # First run the experiment to generate data
        experiment_cmd = [
            "java",
            "-Djava.awt.headless=true",
            "-cp", f"{java_dir}:lib/*",
            "HashMapExperiment",
            f"run{experiment_type.capitalize()}Experiment"
        ]
        
        # For Windows, use semicolons instead of colons in classpath
        if os.name == 'nt':
            experiment_cmd[3] = f"{java_dir};lib/*"
        
        print(f"Running experiment: {' '.join(experiment_cmd)}")
        exp_result = subprocess.run(
            experiment_cmd,
            capture_output=True,
            text=True,
            timeout=30
        )
        
        if exp_result.returncode != 0:
            raise Exception(f"Experiment failed: {exp_result.stderr}")
        
        print(f"Experiment completed: {exp_result.stdout}")
        
        # Then run the visualization
        csv_file = f"{experiment_type.lower()}_data.csv"
        if not os.path.exists(csv_file):
            # Use the appropriate CSV file based on experiment type
            if experiment_type == "hashFunction":
                csv_file = "hash_function_comparison.csv"
            elif experiment_type == "collision":
                csv_file = "string_collisions.csv"
            elif experiment_type == "lookup":
                csv_file = "lookup_performance.csv"
            elif experiment_type == "distribution":
                csv_file = "bucket_distribution.csv"
            elif experiment_type == "comparison":
                csv_file = "hashmap_comparison.csv"
            elif experiment_type == "textFingerprint":
                csv_file = "text_fingerprint_analysis.csv"
                
        # Check if CSV file exists
        if not os.path.exists(csv_file):
            raise FileNotFoundError(f"CSV file not found: {csv_file}")
            
        print(f"Using CSV file: {csv_file}")
        
        # Run visualization
        viz_cmd = [
            "java",
            "-Djava.awt.headless=true",
            "-cp", f"{java_dir}:lib/*",
            "HashMapVisualizer",
            f"visualize{experiment_type.capitalize()}",
            csv_file,
            "Experiment Results",
            output_file
        ]
        
        # For Windows, use semicolons instead of colons in classpath
        if os.name == 'nt':
            viz_cmd[3] = f"{java_dir};lib/*"
        
        print(f"Running visualization: {' '.join(viz_cmd)}")
        viz_result = subprocess.run(
            viz_cmd,
            capture_output=True,
            text=True,
            timeout=30
        )
        
        if viz_result.returncode != 0:
            raise Exception(f"Visualization failed: {viz_result.stderr}")
        
        print(f"Visualization completed: {viz_result.stdout}")
        
        # Check if output file exists
        if not os.path.exists(output_file):
            raise FileNotFoundError(f"Output image not found: {output_file}")
            
        print(f"Output file exists: {output_file}, size: {os.path.getsize(output_file)} bytes")
        
        # Read the image file
        with open(output_file, 'rb') as f:
            image_bytes = f.read()
            
        # Convert to base64
        base64_image = base64.b64encode(image_bytes).decode('utf-8')
        
        return base64_image
        
    except Exception as e:
        print(f"Error in run_experiment: {str(e)}")
        raise
        
    finally:
        # Clean up
        if output_file and os.path.exists(output_file):
            try:
                os.remove(output_file)
                print(f"Deleted file: {output_file}")
            except Exception as e:
                print(f"Failed to delete {output_file}: {str(e)}")
                
        # Remove the temporary directory
        if temp_dir and os.path.exists(temp_dir):
            try:
                import shutil
                shutil.rmtree(temp_dir)
                print(f"Deleted temporary directory: {temp_dir}")
            except Exception as e:
                print(f"Failed to delete temporary directory {temp_dir}: {str(e)}")