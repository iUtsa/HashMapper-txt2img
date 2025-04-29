from flask import Flask, render_template, request, jsonify, send_file
import base64
import os
import subprocess
import time
import tempfile
import shutil
import logging
import traceback
import json
import sys

# Set up logging
logging.basicConfig(
    level=logging.DEBUG,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    handlers=[
        logging.StreamHandler(sys.stdout)
    ]
)
logger = logging.getLogger(__name__)

app = Flask(__name__)

# Create Java bridge class for generating fingerprints
class JavaBridge:
    def generate_fingerprint(self, text, size, hash_function, salt_level, smooth_radius):
        """
        Generate fingerprint using Java code
        Returns: (raw_image_bytes, enhanced_image_bytes, stats_dict)
        """
        text_path = None
        raw_output = None
        enhanced_output = None
        stats_output = None
        temp_dir = None

        try:
            # Create a temporary directory to store all files
            temp_dir = tempfile.mkdtemp()
            logger.debug(f"Created temporary directory: {temp_dir}")
            
            # Write text to a temporary file
            text_path = os.path.join(temp_dir, 'input.txt')
            with open(text_path, 'w', encoding='utf-8') as text_file:
                text_file.write(text)
            logger.debug(f"Wrote input text to: {text_path} (length: {len(text)})")
            
            # Define output paths
            raw_output = os.path.join(temp_dir, "raw_output.png")
            enhanced_output = os.path.join(temp_dir, "enhanced_output.png")
            stats_output = os.path.join(temp_dir, "stats_output.json")
            
            logger.debug(f"Working directory: {os.getcwd()}")
            logger.debug(f"Output files: {raw_output}, {enhanced_output}, {stats_output}")
            
            # Get the path to the java directory
            java_dir = os.path.join(os.getcwd(), 'java')
            logger.debug(f"Java directory: {java_dir}")
            
            # Build the command - USING CORRECT MAIN CLASS
            cmd = [
                "java",
                "-Djava.awt.headless=true",  # Enable headless mode for server environments
                "-cp", f"{java_dir}:lib/*",  # Include java directory and all JARs in lib
                "HashMapExperimentRunner",  # The main class - FIXED
                "--text-file", text_path,
                "--size", str(size),
                "--hash-function", hash_function,
                "--salt-level", str(salt_level),
                "--smooth-radius", str(smooth_radius),
                "--raw-output", raw_output,
                "--enhanced-output", enhanced_output,
                "--stats-output", stats_output
            ]
            
            # For Windows, use semicolons instead of colons in classpath
            if os.name == 'nt':
                cmd[3] = f"{java_dir};lib/*"
            
            logger.debug(f"Executing command: {' '.join(cmd)}")
            
            # Run the Java process
            result = subprocess.run(
                cmd,
                capture_output=True,
                text=True,
                timeout=60  # Increase timeout for large inputs
            )
            
            logger.debug(f"Java process completed with return code: {result.returncode}")
            logger.debug(f"stdout: {result.stdout}")
            logger.debug(f"stderr: {result.stderr}")
            
            if result.returncode != 0:
                raise Exception(f"Java process failed: {result.stderr}")
            
            # List all files in the temp directory for debugging
            logger.debug(f"Files in temp directory: {os.listdir(temp_dir)}")
            
            # Check if output files exist
            for file_path in [raw_output, enhanced_output, stats_output]:
                if not os.path.exists(file_path):
                    raise FileNotFoundError(f"Output file not found: {file_path}")
                logger.debug(f"File exists: {file_path}, size: {os.path.getsize(file_path)} bytes")
            
            # Read the output files
            with open(raw_output, 'rb') as f:
                raw_bytes = f.read()
                logger.debug(f"Read raw_output file: {len(raw_bytes)} bytes")
                
            with open(enhanced_output, 'rb') as f:
                enhanced_bytes = f.read()
                logger.debug(f"Read enhanced_output file: {len(enhanced_bytes)} bytes")
                
            # Read and parse the JSON stats file with careful error handling
            try:
                with open(stats_output, 'r') as f:
                    stats_content = f.read()
                    logger.debug(f"Stats file content: {stats_content}")
                    
                    # Validate JSON before parsing
                    stats = json.loads(stats_content)
                    logger.debug(f"Parsed stats: {stats}")
            except json.JSONDecodeError as e:
                logger.error(f"JSON parsing error: {e}")
                logger.error(f"Content that failed to parse: {stats_content}")
                # Provide fallback stats
                stats = {
                    "text_length": len(text),
                    "hash_function": hash_function,
                    "salt_level": salt_level,
                    "smooth_radius": smooth_radius,
                    "error": "Failed to parse stats JSON"
                }
                
            return raw_bytes, enhanced_bytes, stats
        
        except Exception as e:
            logger.error(f"Error in generate_fingerprint: {str(e)}")
            logger.error(traceback.format_exc())
            raise
        
        finally:
            # Clean up temporary files
            for file_path in [text_path, raw_output, enhanced_output, stats_output]:
                if file_path and os.path.exists(file_path):
                    try:
                        os.remove(file_path)
                        logger.debug(f"Deleted file: {file_path}")
                    except Exception as e:
                        logger.error(f"Failed to delete {file_path}: {str(e)}")
            
            # Remove the temporary directory
            if temp_dir and os.path.exists(temp_dir):
                try:
                    shutil.rmtree(temp_dir)
                    logger.debug(f"Deleted temporary directory: {temp_dir}")
                except Exception as e:
                    logger.error(f"Failed to delete temporary directory {temp_dir}: {str(e)}")

    def run_experiment(self, experiment_type):
        """
        Run HashMap experiment and return the visualization
        Returns: base64 encoded image
        """
        output_file = None
        temp_dir = None
        
        try:
            # Create a temporary directory
            temp_dir = tempfile.mkdtemp()
            logger.debug(f"Created temporary directory: {temp_dir}")
            
            output_file = os.path.join(temp_dir, f"{experiment_type}_output.png")
            logger.debug(f"Output file will be: {output_file}")
            
            # Get the path to the java directory
            java_dir = os.path.join(os.getcwd(), 'java')
            logger.debug(f"Java directory: {java_dir}")
            
            # First, run HashMapExperiment to generate the data
            experiment_cmd = [
                "java",
                "-Djava.awt.headless=true",
                "-cp", f"{java_dir}:lib/*",
                "HashMapExperiment",  # Use HashMapExperiment to generate data
            ]
            
            # Method name depends on experiment type
            method_name = f"run{experiment_type.capitalize()}Experiment"
            experiment_cmd.append(method_name)
            
            # For Windows, use semicolons instead of colons in classpath
            if os.name == 'nt':
                experiment_cmd[3] = f"{java_dir};lib/*"
            
            logger.debug(f"Running experiment: {' '.join(experiment_cmd)}")
            exp_result = subprocess.run(
                experiment_cmd,
                capture_output=True,
                text=True,
                timeout=60
            )
            
            logger.debug(f"Experiment process completed with return code: {exp_result.returncode}")
            logger.debug(f"stdout: {exp_result.stdout}")
            logger.debug(f"stderr: {exp_result.stderr}")
            
            if exp_result.returncode != 0:
                raise Exception(f"Experiment failed: {exp_result.stderr}")
            
            # Look for the generated CSV file
            csv_file = None
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
                # List all files in current directory to help debugging
                current_dir_files = os.listdir('.')
                logger.debug(f"Files in current directory: {current_dir_files}")
                csv_files = [f for f in current_dir_files if f.endswith('.csv')]
                logger.debug(f"CSV files found: {csv_files}")
                
                if not csv_files:
                    raise FileNotFoundError(f"No CSV files generated by the experiment")
                
                # Use the first CSV file found
                csv_file = csv_files[0]
                logger.debug(f"Using first available CSV file: {csv_file}")
                
            logger.debug(f"Using CSV file: {csv_file} (exists: {os.path.exists(csv_file)}, size: {os.path.getsize(csv_file)})")
            
            # Now run HashMapVisualizer to create the visualization
            viz_cmd = [
                "java",
                "-Djava.awt.headless=true",
                "-cp", f"{java_dir}:lib/*",
                "HashMapVisualizer",  # Use HashMapVisualizer for visualization
            ]
            
            # Method name depends on experiment type
            viz_method = f"visualize{experiment_type.capitalize()}"
            viz_cmd.extend([
                viz_method,
                csv_file,
                "Experiment Results",
                output_file
            ])
            
            # For Windows, use semicolons instead of colons in classpath
            if os.name == 'nt':
                viz_cmd[3] = f"{java_dir};lib/*"
            
            logger.debug(f"Running visualization: {' '.join(viz_cmd)}")
            viz_result = subprocess.run(
                viz_cmd,
                capture_output=True,
                text=True,
                timeout=60
            )
            
            logger.debug(f"Visualization process completed with return code: {viz_result.returncode}")
            logger.debug(f"stdout: {viz_result.stdout}")
            logger.debug(f"stderr: {viz_result.stderr}")
            
            if viz_result.returncode != 0:
                raise Exception(f"Visualization failed: {viz_result.stderr}")
            
            # Check if output file exists
            if not os.path.exists(output_file):
                raise FileNotFoundError(f"Output image not found: {output_file}")
                
            logger.debug(f"Output file exists: {output_file}, size: {os.path.getsize(output_file)} bytes")
            
            # Read the image file
            with open(output_file, 'rb') as f:
                image_bytes = f.read()
                logger.debug(f"Read image file: {len(image_bytes)} bytes")
                
            # Convert to base64
            base64_image = base64.b64encode(image_bytes).decode('utf-8')
            logger.debug(f"Converted image to base64: {len(base64_image)} characters")
            
            return base64_image
            
        except Exception as e:
            logger.error(f"Error in run_experiment: {str(e)}")
            logger.error(traceback.format_exc())
            raise
            
        finally:
            # Clean up
            if output_file and os.path.exists(output_file):
                try:
                    os.remove(output_file)
                    logger.debug(f"Deleted file: {output_file}")
                except Exception as e:
                    logger.error(f"Failed to delete {output_file}: {str(e)}")
                    
            # Remove the temporary directory
            if temp_dir and os.path.exists(temp_dir):
                try:
                    shutil.rmtree(temp_dir)
                    logger.debug(f"Deleted temporary directory: {temp_dir}")
                except Exception as e:
                    logger.error(f"Failed to delete temporary directory {temp_dir}: {str(e)}")

# Initialize JavaBridge
java_bridge = JavaBridge()

@app.route('/')
def index():
    # Define template variables
    template_data = {
        'title': 'HashMapper - Text to Image Fingerprints',
        'version': '1.0.0',
        'default_text': 'It was the best of times, it was the worst of times, it was the age of wisdom, it was the age of foolishness, it was the epoch of belief, it was the epoch of incredulity, it was the season of Light, it was the season of Darkness...',
        'default_map_size': 128,
        'default_salt_level': 5,
        'default_smooth_radius': 2,
        'hash_functions': [
            'String Length',
            'First Character',
            'First + Last Character',
            'Character Sum',
            'Random'
        ],
        'hash_function_descriptions': [
            {'name': 'String Length', 'description': 'Uses only the length of words'},
            {'name': 'First Character', 'description': 'Uses only the first character of words'},
            {'name': 'First + Last Character', 'description': 'Combines first and last characters'},
            {'name': 'Character Sum', 'description': 'Sums all character values'},
            {'name': 'Random', 'description': 'Creates pseudo-random but deterministic patterns'}
        ],
        'experiments': [
            {'type': 'collision', 'title': 'Collision Analysis'},
            {'type': 'lookup', 'title': 'Lookup Performance'},
            {'type': 'distribution', 'title': 'Bucket Distribution'},
            {'type': 'hashFunction', 'title': 'Hash Function Comparison'},
            {'type': 'comparison', 'title': 'HashMap Comparison'},
            {'type': 'textFingerprint', 'title': 'Text Fingerprint Analysis'}
        ]
    }
    
    logger.debug("Rendering index template")
    return render_template('index.html', **template_data)

@app.route('/api/test', methods=['GET'])
def test_api():
    """Simple API endpoint for testing JSON responses"""
    logger.debug("Test API endpoint called")
    return jsonify({'status': 'ok', 'message': 'API is working'})

@app.route('/api/generate-fingerprint', methods=['POST'])
def generate_fingerprint():
    """API endpoint to generate text fingerprints"""
    logger.debug("Generate fingerprint API endpoint called")
    
    try:
        # Get form data
        text = request.form.get('text', '')
        size = int(request.form.get('size', 128))
        hash_function = request.form.get('hashFunction', 'String Length')
        salt_level = float(request.form.get('saltLevel', 0.05))
        smooth_radius = int(request.form.get('smoothRadius', 2))
        
        logger.debug(f"Request parameters: text_length={len(text)}, size={size}, hash_function={hash_function}, salt_level={salt_level}, smooth_radius={smooth_radius}")
        
        if not text:
            logger.warning("No text provided in request")
            return jsonify({'error': 'No text provided'}), 400
        
        # Generate fingerprint using Java bridge
        logger.debug("Calling java_bridge.generate_fingerprint()")
        raw_image, enhanced_image, stats = java_bridge.generate_fingerprint(
            text, size, hash_function, salt_level, smooth_radius
        )
        
        logger.debug(f"Generate fingerprint returned: raw_image={len(raw_image) if raw_image else 'None'} bytes, enhanced_image={len(enhanced_image) if enhanced_image else 'None'} bytes, stats={stats}")
        
        # Convert images to base64 for display in browser
        raw_base64 = base64.b64encode(raw_image).decode('utf-8')
        enhanced_base64 = base64.b64encode(enhanced_image).decode('utf-8')
        
        # Create response JSON
        response_data = {
            'raw_image': raw_base64,
            'enhanced_image': enhanced_base64,
            'stats': stats
        }
        
        # Validate JSON response before returning
        try:
            # Test serialize to validate
            json_response = json.dumps(response_data)
            logger.debug(f"Response JSON created: {len(json_response)} characters")
        except Exception as e:
            logger.error(f"JSON serialization error: {str(e)}")
            # Create a safe response
            response_data = {
                'error': 'Error creating JSON response',
                'message': str(e)
            }
        
        logger.debug("Returning successful response")
        return jsonify(response_data)
    
    except Exception as e:
        logger.error(f"Error generating fingerprint: {str(e)}")
        logger.error(traceback.format_exc())
        return jsonify({'error': str(e)}), 500

@app.route('/api/run-experiment', methods=['POST'])
def run_experiment():
    """API endpoint to run HashMap experiments"""
    logger.debug("Run experiment API endpoint called")
    
    try:
        # Get form data
        experiment_type = request.form.get('type', 'collision')
        logger.debug(f"Request parameters: experiment_type={experiment_type}")
        
        # Run experiment using Java bridge
        logger.debug("Calling java_bridge.run_experiment()")
        base64_image = java_bridge.run_experiment(experiment_type)
        logger.debug(f"Run experiment returned: base64_image={len(base64_image) if base64_image else 'None'} characters")
        
        # Create response JSON
        response_data = {
            'image': base64_image
        }
        
        # Validate JSON response before returning
        try:
            # Test serialize to validate
            json_response = json.dumps(response_data)
            logger.debug(f"Response JSON created: {len(json_response)} characters")
        except Exception as e:
            logger.error(f"JSON serialization error: {str(e)}")
            # Create a safe response
            response_data = {
                'error': 'Error creating JSON response',
                'message': str(e)
            }
        
        logger.debug("Returning successful response")
        return jsonify(response_data)
        
    except Exception as e:
        logger.error(f"Error running experiment: {str(e)}")
        logger.error(traceback.format_exc())
        return jsonify({'error': str(e)}), 500

if __name__ == '__main__':
    logger.info("Starting Flask application")
    app.run(debug=True)