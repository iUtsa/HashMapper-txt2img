#!/bin/bash

# Script to set up the folder structure for the HashMapper project

# Create main directories
mkdir -p templates
mkdir -p static/css
mkdir -p static/js
mkdir -p static/img
mkdir -p lib

# Copy the template files
echo "Copying template files..."
cp index.html templates/

# Create static files
echo "Creating CSS file..."
cat > static/css/style.css << 'EOL'
/* Basic reset */
* {
    margin: 0;
    padding: 0;
    box-sizing: border-box;
}

body {
    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
    line-height: 1.6;
    color: #333;
    background-color: #f5f7fa;
    padding-bottom: 2rem;
}

/* Header */
header {
    background-color: #2c3e50;
    color: white;
    padding: 1.5rem;
    text-align: center;
}

header h1 {
    font-size: 2.5rem;
    margin-bottom: 0.5rem;
}

header p {
    font-size: 1.2rem;
    opacity: 0.8;
}

/* Main content */
main {
    max-width: 1200px;
    margin: 2rem auto;
    padding: 0 1rem;
}

/* Tabs */
.tabs {
    display: flex;
    border-bottom: 1px solid #ddd;
    margin-bottom: 2rem;
}

.tab-button {
    padding: 0.75rem 1.5rem;
    border: none;
    background: none;
    font-size: 1rem;
    cursor: pointer;
    border-bottom: 3px solid transparent;
    transition: all 0.3s ease;
}

.tab-button:hover {
    background-color: #f0f0f0;
}

.tab-button.active {
    border-bottom-color: #3498db;
    font-weight: 600;
}

.tab-pane {
    display: none;
}

.tab-pane.active {
    display: block;
}

/* Text input panel */
.input-panel {
    background-color: white;
    border-radius: 8px;
    padding: 1.5rem;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
    margin-bottom: 2rem;
}

.input-panel h2 {
    margin-bottom: 1rem;
    color: #2c3e50;
}

#text-input {
    width: 100%;
    min-height: 150px;
    padding: 0.75rem;
    border: 1px solid #ddd;
    border-radius: 4px;
    font-family: inherit;
    font-size: 1rem;
    margin-bottom: 1.5rem;
    resize: vertical;
}

/* Parameters */
.parameters {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
    gap: 1rem;
    margin-bottom: 1.5rem;
}

.parameter {
    margin-bottom: 1rem;
}

.parameter label {
    display: block;
    margin-bottom: 0.5rem;
    font-weight: 500;
}

.parameter select {
    width: 100%;
    padding: 0.5rem;
    border: 1px solid #ddd;
    border-radius: 4px;
    font-size: 0.9rem;
}

.parameter input[type="range"] {
    width: calc(100% - 50px);
    vertical-align: middle;
}

.value-display {
    display: inline-block;
    width: 40px;
    text-align: right;
    color: #666;
    font-size: 0.9rem;
}

/* Buttons */
.primary-button {
    background-color: #3498db;
    color: white;
    border: none;
    padding: 0.75rem 1.5rem;
    font-size: 1rem;
    border-radius: 4px;
    cursor: pointer;
    transition: background-color 0.3s ease;
}

.primary-button:hover {
    background-color: #2980b9;
}

.primary-button:disabled {
    background-color: #a0a0a0;
    cursor: not-allowed;
}

/* Results panel */
.results-panel {
    background-color: white;
    border-radius: 8px;
    padding: 1.5rem;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.hidden {
    display: none;
}

.fingerprints {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
    gap: 1.5rem;
    margin-bottom: 2rem;
}

.fingerprint-card {
    border: 1px solid #eee;
    border-radius: 4px;
    padding: 1rem;
    text-align: center;
}

.fingerprint-card h3 {
    margin-bottom: 1rem;
    color: #2c3e50;
}

.fingerprint-card img {
    max-width: 100%;
    height: auto;
    border-radius: 4px;
}

/* Stats panel */
.stats-panel {
    margin-top: 2rem;
}

.stats-panel h3 {
    margin-bottom: 1rem;
    color: #2c3e50;
}

.stats-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
    gap: 1rem;
}

.stat-item {
    background-color: #f8f9fa;
    border-radius: 4px;
    padding: 1rem;
    text-align: center;
}

.stat-value {
    font-size: 1.5rem;
    font-weight: bold;
    color: #3498db;
    margin-bottom: 0.5rem;
}

.stat-label {
    font-size: 0.9rem;
    color: #666;
}

/* Loading panel */
.loading-panel {
    text-align: center;
    padding: 2rem;
}

.spinner {
    border: 4px solid #f3f3f3;
    border-top: 4px solid #3498db;
    border-radius: 50%;
    width: 40px;
    height: 40px;
    animation: spin 1s linear infinite;
    margin: 0 auto 1rem;
}

@keyframes spin {
    0% { transform: rotate(0deg); }
    100% { transform: rotate(360deg); }
}

/* Experiment section */
.experiment-options {
    display: flex;
    flex-wrap: wrap;
    gap: 1rem;
    margin: 1.5rem 0;
}

.experiment-button {
    background-color: #ecf0f1;
    color: #2c3e50;
    border: none;
    padding: 0.75rem 1.5rem;
    font-size: 1rem;
    border-radius: 4px;
    cursor: pointer;
    transition: all 0.3s ease;
}

.experiment-button:hover {
    background-color: #d5dbdb;
}

.experiment-results {
    background-color: white;
    border-radius: 8px;
    padding: 1.5rem;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
    margin-top: 1.5rem;
    min-height: 400px;
}

/* Footer */
footer {
    text-align: center;
    padding: 1rem;
    margin-top: 3rem;
    background-color: #2c3e50;
    color: white;
}
EOL

echo "Creating JavaScript file..."
cat > static/js/main.js << 'EOL'
document.addEventListener('DOMContentLoaded', function() {
    // Tab switching
    const tabButtons = document.querySelectorAll('.tab-button');
    const tabPanes = document.querySelectorAll('.tab-pane');
    
    tabButtons.forEach(button => {
        button.addEventListener('click', () => {
            // Remove active class from all buttons
            tabButtons.forEach(btn => btn.classList.remove('active'));
            // Add active class to clicked button
            button.classList.add('active');
            
            // Hide all tab panes
            tabPanes.forEach(pane => pane.classList.remove('active'));
            // Show the corresponding tab pane
            const tabId = button.dataset.tab;
            document.getElementById(tabId).classList.add('active');
        });
    });
    
    // Parameter value display
    const rangeInputs = document.querySelectorAll('input[type="range"]');
    rangeInputs.forEach(input => {
        input.addEventListener('input', () => {
            const display = input.nextElementSibling;
            let value = input.value;
            
            // Add % for salt level
            if (input.id === 'salt-level') {
                value += '%';
            }
            
            display.textContent = value;
        });
    });
    
    // Generate fingerprint
    const generateButton = document.getElementById('generate-button');
    const textInput = document.getElementById('text-input');
    const hashFunction = document.getElementById('hash-function');
    const mapSize = document.getElementById('map-size');
    const saltLevel = document.getElementById('salt-level');
    const smoothRadius = document.getElementById('smooth-radius');
    const results = document.getElementById('results');
    const loading = document.getElementById('loading');
    
    generateButton.addEventListener('click', () => {
        const text = textInput.value.trim();
        
        if (!text) {
            alert('Please enter some text first.');
            return;
        }
        
        // Show loading state
        results.classList.add('hidden');
        loading.classList.remove('hidden');
        generateButton.disabled = true;
        
        // Create form data
        const formData = new FormData();
        formData.append('text', text);
        formData.append('size', mapSize.value);
        formData.append('hashFunction', hashFunction.value);
        formData.append('saltLevel', saltLevel.value / 100); // Convert to decimal
        formData.append('smoothRadius', smoothRadius.value);
        
        // Send request
        fetch('/api/generate-fingerprint', {
            method: 'POST',
            body: formData
        })
        .then(response => response.json())
        .then(data => {
            if (data.error) {
                throw new Error(data.error);
            }
            
            // Update images
            document.getElementById('raw-fingerprint').src = 'data:image/png;base64,' + data.raw_image;
            document.getElementById('enhanced-fingerprint').src = 'data:image/png;base64,' + data.enhanced_image;
            
            // Update stats
            const statsDisplay = document.getElementById('stats-display');
            statsDisplay.innerHTML = '';
            
            // Add stats items
            const stats = data.stats;
            const statItems = [
                { label: 'Text Length', value: stats.text_length || 0 },
                { label: 'Total Words', value: stats.total_words || 0 },
                { label: 'Unique Words', value: stats.unique_words || 0 },
                { label: 'Collisions', value: stats.collisions || 0 },
                { label: 'Max Collision Level', value: stats.max_collision_level || 0 }
            ];
            
            statItems.forEach(item => {
                const statElement = document.createElement('div');
                statElement.className = 'stat-item';
                statElement.innerHTML = `
                    <div class="stat-value">${item.value}</div>
                    <div class="stat-label">${item.label}</div>
                `;
                statsDisplay.appendChild(statElement);
            });
            
            // Show results
            loading.classList.add('hidden');
            results.classList.remove('hidden');
        })
        .catch(error => {
            alert('Error: ' + error.message);
            loading.classList.add('hidden');
        })
        .finally(() => {
            generateButton.disabled = false;
        });
    });
    
    // Experiment buttons
    const experimentButtons = document.querySelectorAll('.experiment-button');
    const experimentResults = document.getElementById('experiment-results');
    const experimentLoading = document.getElementById('experiment-loading');
    
    experimentButtons.forEach(button => {
        button.addEventListener('click', () => {
            const experimentType = button.dataset.type;
            
            // Show loading state
            experimentResults.innerHTML = '';
            experimentLoading.classList.remove('hidden');
            experimentButtons.forEach(btn => btn.disabled = true);
            
            // Create form data
            const formData = new FormData();
            formData.append('type', experimentType);
            
            // Send request
            fetch('/api/run-experiment', {
                method: 'POST',
                body: formData
            })
            .then(response => response.json())
            .then(data => {
                if (data.error) {
                    throw new Error(data.error);
                }
                
                // Display experiment results
                const experimentImageSrc = 'data:image/png;base64,' + data.image;
                experimentResults.innerHTML = `
                    <h3>${getExperimentTitle(experimentType)}</h3>
                    <p>${getExperimentDescription(experimentType)}</p>
                    <div class="experiment-chart">
                        <img src="${experimentImageSrc}" alt="${getExperimentTitle(experimentType)}">
                    </div>
                `;
                
                // Show results
                experimentLoading.classList.add('hidden');
                experimentResults.classList.remove('hidden');
            })
            .catch(error => {
                experimentResults.innerHTML = `<p class="error">Error: ${error.message}</p>`;
                experimentLoading.classList.add('hidden');
            })
            .finally(() => {
                experimentButtons.forEach(btn => btn.disabled = false);
            });
        });
    });
    
    // Helper functions for experiment titles and descriptions
    function getExperimentTitle(type) {
        switch (type) {
            case 'collision': return 'Collision Analysis';
            case 'lookup': return 'Lookup Performance';
            case 'distribution': return 'Bucket Distribution';
            case 'hashFunction': return 'Hash Function Comparison';
            case 'comparison': return 'HashMap Comparison';
            case 'textFingerprint': return 'Text Fingerprint Analysis';
            default: return 'Experiment Results';
        }
    }
    
    function getExperimentDescription(type) {
        switch (type) {
            case 'collision': 
                return 'This experiment measures collision rates with different HashMap sizes and data sizes.';
            case 'lookup': 
                return 'This experiment measures lookup performance with different HashMap configurations.';
            case 'distribution': 
                return 'This experiment analyzes how items are distributed across buckets.';
            case 'hashFunction': 
                return 'This experiment compares different hash functions with the same data.';
            case 'comparison':
                return 'This experiment compares our SimpleHashMap with Java\'s built-in HashMap implementation.';
            case 'textFingerprint':
                return 'This experiment analyzes fingerprints of different text types.';
            default: 
                return 'Experiment completed successfully.';
        }
    }
    });
EOL

echo "Folder structure created successfully!"
echo "-----------------------------------"
echo "Directory structure:"
echo "- templates/ - HTML templates"
echo "- static/css/ - CSS stylesheets"
echo "- static/js/ - JavaScript files"
echo "- static/img/ - Image files"
echo "- lib/ - Java libraries"
echo "-----------------------------------"
echo "Next steps:"
echo "1. Download the required JAR files to lib/"
echo "2. Compile the Java files"
echo "3. Run the Flask application with 'python app.py'"