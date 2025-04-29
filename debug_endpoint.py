from flask import Flask, jsonify

app = Flask(__name__)

@app.route('/')
def index():
    return "API is working"

@app.route('/api/test', methods=['GET'])
def test_api():
    return jsonify({'status': 'ok', 'message': 'API is working'})

@app.route('/api/generate-fingerprint', methods=['POST'])
def generate_fingerprint():
    return jsonify({
        'raw_image': 'base64_data_would_go_here', 
        'enhanced_image': 'base64_data_would_go_here',
        'stats': {
            'text_length': 100,
            'total_words': 20,
            'unique_words': 15,
            'collisions': 5,
            'max_collision_level': 2
        }
    })

@app.route('/api/run-experiment', methods=['POST'])
def run_experiment():
    return jsonify({
        'image': 'base64_data_would_go_here'
    })

if __name__ == '__main__':
    app.run(debug=True)