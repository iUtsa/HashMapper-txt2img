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
                { label: 'Total Words', value: stats.totalWords },
                { label: 'Unique Words', value: stats.uniqueWords },
                { label: 'Collisions', value: stats.collisions },
                { label: 'Max Collision Level', value: stats.maxCollisionLevel }
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
                displayExperiment(experimentType, data);
            })
            .catch(error => {
                experimentResults.innerHTML = `<p class="error">Error: ${error.message}</p>`;
            })
            .finally(() => {
                experimentLoading.classList.add('hidden');
                experimentButtons.forEach(btn => btn.disabled = false);
            });
        });
    });
    
    // Display experiment results
    function displayExperiment(type, data) {
        experimentResults.innerHTML = `
            <h3>${getExperimentTitle(type)}</h3>
            <p>${getExperimentDescription(type)}</p>
            <div class="experiment-chart">
                <!-- Chart would be rendered here -->
                <p>Experiment completed successfully. Chart rendering requires additional implementation.</p>
            </div>
            <div class="experiment-data">
                <h4>Raw Data</h4>
                <pre>${JSON.stringify(data, null, 2)}</pre>
            </div>
        `;
    }
    
    // Helper functions for experiment titles and descriptions
    function getExperimentTitle(type) {
        switch (type) {
            case 'collision': return 'Collision Analysis';
            case 'lookup': return 'Lookup Performance';
            case 'distribution': return 'Bucket Distribution';
            case 'hashfunction': return 'Hash Function Comparison';
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
            case 'hashfunction': 
                return 'This experiment compares different hash functions with the same data.';
            default: 
                return 'Experiment completed successfully.';
        }
    }
});