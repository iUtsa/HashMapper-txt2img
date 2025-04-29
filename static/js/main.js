document.addEventListener('DOMContentLoaded', function() {
    // Set up debug logging
    const DEBUG = true;
    
    function debugLog(...args) {
        if (DEBUG) {
            console.log(...args);
        }
    }
    
    debugLog('HashMapper application initialized');
    
    // Tab switching
    const tabButtons = document.querySelectorAll('.tab-button');
    const tabPanes = document.querySelectorAll('.tab-pane');
    
    tabButtons.forEach(button => {
        button.addEventListener('click', () => {
            debugLog('Tab clicked:', button.dataset.tab);
            
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
            debugLog(`Parameter ${input.id} changed to:`, value);
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
        debugLog('Generate button clicked');
        
        const text = textInput.value.trim();
        
        if (!text) {
            alert('Please enter some text first.');
            return;
        }
        
        debugLog('Text length:', text.length);
        debugLog('Hash function:', hashFunction.value);
        debugLog('Map size:', mapSize.value);
        debugLog('Salt level:', saltLevel.value);
        debugLog('Smooth radius:', smoothRadius.value);
        
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
        
        debugLog('Sending request to /api/generate-fingerprint');
        
        // Send request with improved error handling
        fetch('/api/generate-fingerprint', {
            method: 'POST',
            body: formData
        })
        .then(response => {
            debugLog('Response status:', response.status);
            debugLog('Response headers:', response.headers);
            
            if (!response.ok) {
                throw new Error(`Server responded with status: ${response.status}`);
            }
            
            // Check if response is JSON
            const contentType = response.headers.get('content-type');
            debugLog('Content type:', contentType);
            
            if (!contentType || !contentType.includes('application/json')) {
                debugLog('Response is not JSON');
                return response.text().then(text => {
                    debugLog('Response text:', text);
                    throw new Error('Server did not return JSON');
                });
            }
            
            return response.json().catch(error => {
                debugLog('JSON parse error:', error);
                return response.text().then(text => {
                    debugLog('Raw response text:', text);
                    throw new Error('Error parsing JSON response');
                });
            });
        })
        .then(data => {
            debugLog('Response data received:', data);
            
            if (data.error) {
                throw new Error(data.error);
            }
            
            // Validate data
            if (!data.raw_image || !data.enhanced_image || !data.stats) {
                throw new Error('Incomplete data received from server');
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
            debugLog('Results displayed successfully');
        })
        .catch(error => {
            console.error('Error:', error);
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
            debugLog('Experiment button clicked:', experimentType);
            
            // Show loading state
            experimentResults.classList.add('hidden');
            experimentLoading.classList.remove('hidden');
            experimentButtons.forEach(btn => btn.disabled = true);
            
            // Create form data
            const formData = new FormData();
            formData.append('type', experimentType);
            
            debugLog('Sending request to /api/run-experiment');
            
            // Send request with improved error handling
            fetch('/api/run-experiment', {
                method: 'POST',
                body: formData
            })
            .then(response => {
                debugLog('Response status:', response.status);
                debugLog('Response headers:', response.headers);
                
                if (!response.ok) {
                    throw new Error(`Server responded with status: ${response.status}`);
                }
                
                // Check if response is JSON
                const contentType = response.headers.get('content-type');
                debugLog('Content type:', contentType);
                
                if (!contentType || !contentType.includes('application/json')) {
                    debugLog('Response is not JSON');
                    return response.text().then(text => {
                        debugLog('Response text:', text);
                        throw new Error('Server did not return JSON');
                    });
                }
                
                return response.json().catch(error => {
                    debugLog('JSON parse error:', error);
                    return response.text().then(text => {
                        debugLog('Raw response text:', text);
                        throw new Error('Error parsing JSON response');
                    });
                });
            })
            .then(data => {
                debugLog('Response data received:', data);
                
                if (data.error) {
                    throw new Error(data.error);
                }
                
                // Validate data
                if (!data.image) {
                    throw new Error('Incomplete data received from server');
                }
                
                // Set experiment title
                document.getElementById('experiment-title').textContent = getExperimentTitle(experimentType);
                
                // Display experiment image
                document.getElementById('experiment-image').src = 'data:image/png;base64,' + data.image;
                
                // Show results
                experimentLoading.classList.add('hidden');
                experimentResults.classList.remove('hidden');
                debugLog('Experiment results displayed successfully');
            })
            .catch(error => {
                console.error('Error:', error);
                alert('Error: ' + error.message);
                experimentLoading.classList.add('hidden');
            })
            .finally(() => {
                experimentButtons.forEach(btn => btn.disabled = false);
            });
        });
    });
    
    // Helper functions for experiment titles
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
});