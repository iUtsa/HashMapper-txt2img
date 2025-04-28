import subprocess
import os
import json
import tempfile
import time
from PIL import Image
import io

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
        with tempfile.NamedTemporaryFile(delete=False, suffix='.txt') as text_file:
            text_file.write(text.encode('utf-8'))
            text_path = text_file.name
        
        timestamp = int(time.time())
        raw_output = f"temp_raw_{timestamp}.png"
        enhanced_output = f"temp_enhanced_{timestamp}.png"
        stats_output = f"temp_stats_{timestamp}.json"
        
        print(f"Running Java command from directory: {os.getcwd()}")
        print(f"Text file: {text_path}")
        print(f"Expected outputs: {raw_output}, {enhanced_output}, {stats_output}")
        
        cmd = [
            "java",
            "-Djava.awt.headless=true",  # Enable headless mode
            "-cp", "lib/*:.:java",
            "HashMapExperimentRunner",
            "--text-file", text_path,
            "--size", str(size),
            "--hash-function", hash_function,
            "--salt-level", str(salt_level),
            "--smooth-radius", str(smooth_radius),
            "--raw-output", raw_output,
            "--enhanced-output", enhanced_output,
            "--stats-output", stats_output
        ]
        
        print(f"Executing command: {' '.join(cmd)}")
        
        result = subprocess.run(
            cmd,
            capture_output=True,
            text=True,
            timeout=30
        )
        
        if result.returncode != 0:
            raise Exception(f"Java process failed: {result.stderr}\nstdout: {result.stdout}")
        
        print(f"Java stdout: {result.stdout}")
        print(f"Java stderr: {result.stderr}")
        
        for file in [raw_output, enhanced_output, stats_output]:
            if not os.path.exists(file):
                raise FileNotFoundError(f"Output file not found: {file}")
        
        with open(raw_output, 'rb') as f:
            raw_bytes = f.read()
            
        with open(enhanced_output, 'rb') as f:
            enhanced_bytes = f.read()
            
        with open(stats_output, 'r') as f:
            stats = json.load(f)
            
        return raw_bytes, enhanced_bytes, stats
    
    except subprocess.TimeoutExpired:
        raise Exception("Java process timed out")
    except FileNotFoundError as e:
        raise Exception(f"Output file missing: {str(e)}")
    except Exception as e:
        raise Exception(f"Error generating fingerprint: {str(e)}")
    finally:
        for file in [text_path, raw_output, enhanced_output, stats_output]:
            if file and os.path.exists(file):
                try:
                    os.remove(file)
                except Exception as e:
                    print(f"Failed to delete {file}: {str(e)}")

def run_experiment(experiment_type):
    """
    Run experiment using Java code
    Returns: dict with experiment results
    """
    output_file = None
    try:
        timestamp = int(time.time())
        output_file = f"temp_experiment_{timestamp}.json"
        
        print(f"Running experiment from directory: {os.getcwd()}")
        print(f"Expected output: {output_file}")
        
        cmd = [
            "java",
            "-Djava.awt.headless=true",  # Enable headless mode
            "-cp", "lib/*:.:java",
            "HashMapExperimentRunner",
            "--type", experiment_type,
            "--output", output_file
        ]
        
        print(f"Executing command: {' '.join(cmd)}")
        
        result = subprocess.run(
            cmd,
            capture_output=True,
            text=True,
            timeout=30
        )
        
        if result.returncode != 0:
            raise Exception(f"Java process failed: {result.stderr}\nstdout: {result.stdout}")
        
        print(f"Java stdout: {result.stdout}")
        print(f"Java stderr: {result.stderr}")
        
        if not os.path.exists(output_file):
            raise FileNotFoundError(f"Output file not found: {output_file}")
        
        with open(output_file, 'r') as f:
            experiment_data = json.load(f)
            
        return experiment_data
    
    except subprocess.TimeoutExpired:
        raise Exception("Java process timed out")
    except FileNotFoundError as e:
        raise Exception(f"Output file missing: {str(e)}")
    except Exception as e:
        raise Exception(f"Error running experiment: {str(e)}")
    finally:
        if output_file and os.path.exists(output_file):
            try:
                os.remove(output_file)
            except Exception as e:
                print(f"Failed to delete {output_file}: {str(e)}")