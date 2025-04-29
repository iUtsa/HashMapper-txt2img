#!/bin/bash

# Script to set up and run the HashMap Visualizer project

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Function to print section headers
print_header() {
    echo -e "\n${YELLOW}=== $1 ===${NC}\n"
}

# Check prerequisites
print_header "Checking prerequisites"

# Check Java
if ! command -v java &> /dev/null || ! command -v javac &> /dev/null; then
    echo -e "${RED}Error: Java JDK is required but not found.${NC}"
    echo "Please install Java JDK 8 or later."
    exit 1
else
    java_version=$(java -version 2>&1 | head -1 | cut -d'"' -f2)
    echo -e "${GREEN}✓ Java found:${NC} $java_version"
fi

# Check Python
if ! command -v python3 &> /dev/null; then
    echo -e "${RED}Error: Python 3 is required but not found.${NC}"
    echo "Please install Python 3.7 or later."
    exit 1
else
    python_version=$(python3 --version)
    echo -e "${GREEN}✓ Python found:${NC} $python_version"
fi

# Create directories
print_header "Setting up project structure"

# Create directories if they don't exist
mkdir -p lib templates static
echo -e "${GREEN}✓ Directories created${NC}"

# Check if the required Java libraries exist
if [ ! -f "lib/jfreechart.jar" ]; then
    print_header "Downloading required Java libraries"
    
    if command -v curl &> /dev/null; then
        echo "Downloading JFreeChart..."
        curl -L "https://repo1.maven.org/maven2/org/jfree/jfreechart/1.5.3/jfreechart-1.5.3.jar" -o "lib/jfreechart.jar"
    elif command -v wget &> /dev/null; then
        echo "Downloading JFreeChart..."
        wget -O "lib/jfreechart.jar" "https://repo1.maven.org/maven2/org/jfree/jfreechart/1.5.3/jfreechart-1.5.3.jar"
    else
        echo -e "${RED}Error: Neither curl nor wget found.${NC}"
        echo "Please install curl or wget, or manually download the following JAR files:"
        echo "- https://repo1.maven.org/maven2/org/jfree/jfreechart/1.5.3/jfreechart-1.5.3.jar (save to lib/jfreechart.jar)"
        exit 1
    fi
    
    echo -e "${GREEN}✓ Java libraries downloaded${NC}"
else
    echo -e "${GREEN}✓ Java libraries already exist${NC}"
fi

# Install Python packages
print_header "Installing Python packages"

# Use pip to install required packages
if ! python3 -m pip install flask pillow; then
    echo -e "${RED}Error: Failed to install Python packages.${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Python packages installed${NC}"

# Compile Java files
print_header "Compiling Java files"

if javac -cp ".:lib/*" *.java; then
    echo -e "${GREEN}✓ Java files compiled successfully${NC}"
else
    echo -e "${RED}Error: Failed to compile Java files.${NC}"
    exit 1
fi

# Copy template files if they don't already exist
if [ ! -f "templates/index.html" ]; then
    echo "Copying template files..."
    if [ -f "index.html" ]; then
        cp index.html templates/
    else
        echo -e "${YELLOW}Warning: index.html not found. Please manually copy it to the templates directory.${NC}"
    fi
fi

# Run the Flask application
print_header "Starting Flask server"
export FLASK_APP=app.py
export FLASK_ENV=development

echo "Starting server at http://localhost:5000"
echo "Press Ctrl+C to stop the server"
flask run