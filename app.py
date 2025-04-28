from flask import Flask, render_template, request, jsonify
import base64
import io
import os
import subprocess
import time
import java_bridge

app = Flask(__name__)

# Flag to ensure Java compilation runs only once
_java_compiled = False

def compile_java():
    global _java_compiled
    if not _java_compiled:
        # Compile Java files with JFreeChart in classpath
        subprocess.run([
            "javac",
            "-cp", "lib/*:.",
            "java/SimpleHashMap.java",
            "java/HashMapper.java",
            "java/HashMapExperiment.java",
            "java/HashMapVisualizer.java",
            "java/HashMapExperimentRunner.java"
        ])
        print("Java files compiled successfully")
        _java_compiled = True

# Run compilation at startup
compile_java()

@app.route('/')
def index():
    return render_template('index.html')

@app.route('/api/generate-fingerprint', methods=['POST'])
def generate_fingerprint():
    # Get form data
    text = request.form.get('text', '')
    size = int(request.form.get('size', 128))
    hash_function = request.form.get('hashFunction', 'String Length')
    salt_level = float(request.form.get('saltLevel', 0.05))
    smooth_radius = int(request.form.get('smoothRadius', 2))
    
    if not text:
        return jsonify({'error': 'No text provided'}), 400
    
    try:
        # Generate fingerprint using Java bridge
        raw_image, enhanced_image, stats = java_bridge.generate_fingerprint(
            text, size, hash_function, salt_level, smooth_radius
        )
        
        # Convert images to base64 for display in browser
        raw_base64 = base64.b64encode(raw_image).decode('utf-8')
        enhanced_base64 = base64.b64encode(enhanced_image).decode('utf-8')
        
        return jsonify({
            'raw_image': raw_base64,
            'enhanced_image': enhanced_base64,
            'stats': stats
        })
        
    except Exception as e:
        return jsonify({'error': str(e)}), 500

@app.route('/api/run-experiment', methods=['POST'])
def run_experiment():
    experiment_type = request.form.get('type', 'collision')
    
    try:
        # Run experiment using Java bridge
        result = java_bridge.run_experiment(experiment_type)
        return jsonify(result)
    except Exception as e:
        return jsonify({'error': str(e)}), 500

if __name__ == '__main__':
    app.run(debug=True)