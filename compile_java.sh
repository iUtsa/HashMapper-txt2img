#!/bin/bash

# Script to compile Java files in the java directory

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Function to print section headers
print_header() {
    echo -e "\n${YELLOW}=== $1 ===${NC}\n"
}

# Get the current directory
CURRENT_DIR=$(pwd)
JAVA_DIR="${CURRENT_DIR}/java"
LIB_DIR="${CURRENT_DIR}/lib"

print_header "Checking Java directory"

# Check if the java directory exists
if [ ! -d "$JAVA_DIR" ]; then
    echo -e "${RED}Error: Java directory not found: $JAVA_DIR${NC}"
    echo "Please make sure the 'java' directory exists in the current directory."
    exit 1
fi

# List Java files in the directory
JAVA_FILES=$(find "$JAVA_DIR" -name "*.java")
if [ -z "$JAVA_FILES" ]; then
    echo -e "${RED}Error: No Java files found in $JAVA_DIR${NC}"
    exit 1
fi

echo -e "${GREEN}Found Java files:${NC}"
for FILE in $JAVA_FILES; do
    echo "  - $(basename "$FILE")"
done

print_header "Checking library directory"

# Check if the lib directory exists
if [ ! -d "$LIB_DIR" ]; then
    echo -e "${YELLOW}Warning: Library directory not found: $LIB_DIR${NC}"
    echo "Creating library directory..."
    mkdir -p "$LIB_DIR"
fi

# Check if required JAR files exist
if [ ! -f "$LIB_DIR/jfreechart.jar" ]; then
    echo -e "${YELLOW}JFreeChart JAR not found. Would you like to download it? (y/n)${NC}"
    read -r DOWNLOAD
    if [[ "$DOWNLOAD" =~ ^[Yy]$ ]]; then
        echo "Downloading JFreeChart..."
        curl -L "https://repo1.maven.org/maven2/org/jfree/jfreechart/1.5.3/jfreechart-1.5.3.jar" -o "$LIB_DIR/jfreechart.jar"
        echo "Downloading JCommon..."
        curl -L "https://repo1.maven.org/maven2/org/jfree/jcommon/1.0.24/jcommon-1.0.24.jar" -o "$LIB_DIR/jcommon.jar"
    fi
fi

print_header "Compiling Java files"

# Determine the classpath separator based on OS
if [[ "$OSTYPE" == "msys" || "$OSTYPE" == "win32" || "$OSTYPE" == "cygwin" ]]; then
    CP_SEP=";"
else
    CP_SEP=":"
fi

# Compile Java files
CLASSPATH="${JAVA_DIR}${CP_SEP}${LIB_DIR}/*"
javac -cp "$CLASSPATH" -d "$JAVA_DIR" "$JAVA_DIR"/*.java

# Check the compilation result
if [ $? -eq 0 ]; then
    echo -e "${GREEN}Java files compiled successfully!${NC}"
    echo -e "You can now run the Flask application with: python app.py"
else
    echo -e "${RED}Error: Failed to compile Java files.${NC}"
    exit 1
fi