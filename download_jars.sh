#!/bin/bash

# Script to download required JAR files for the HashMapper project

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Create lib directory if it doesn't exist
mkdir -p lib

echo -e "${YELLOW}Downloading required JAR files...${NC}"

# JFreeChart - needed for chart visualization
if [ ! -f "lib/jfreechart.jar" ]; then
    echo "Downloading JFreeChart..."
    if command -v curl &> /dev/null; then
        curl -L "https://repo1.maven.org/maven2/org/jfree/jfreechart/1.5.3/jfreechart-1.5.3.jar" -o "lib/jfreechart.jar"
    elif command -v wget &> /dev/null; then
        wget -O "lib/jfreechart.jar" "https://repo1.maven.org/maven2/org/jfree/jfreechart/1.5.3/jfreechart-1.5.3.jar"
    else
        echo -e "${RED}Error: Neither curl nor wget found. Please install one of them to download JAR files.${NC}"
        exit 1
    fi
    echo -e "${GREEN}✓ JFreeChart downloaded${NC}"
else
    echo -e "${GREEN}✓ JFreeChart already exists${NC}"
fi

# JCommon - dependency for JFreeChart
if [ ! -f "lib/jcommon.jar" ]; then
    echo "Downloading JCommon..."
    if command -v curl &> /dev/null; then
        curl -L "https://repo1.maven.org/maven2/org/jfree/jcommon/1.0.24/jcommon-1.0.24.jar" -o "lib/jcommon.jar"
    elif command -v wget &> /dev/null; then
        wget -O "lib/jcommon.jar" "https://repo1.maven.org/maven2/org/jfree/jcommon/1.0.24/jcommon-1.0.24.jar"
    else
        echo -e "${RED}Error: Neither curl nor wget found. Please install one of them to download JAR files.${NC}"
        exit 1
    fi
    echo -e "${GREEN}✓ JCommon downloaded${NC}"
else
    echo -e "${GREEN}✓ JCommon already exists${NC}"
fi

echo -e "\n${GREEN}All required JAR files have been downloaded to the lib/ directory${NC}"
echo "You can now compile and run the Java code with the following classpath:"
echo "javac -cp .:lib/* *.java"
echo "java -cp .:lib/* HashMapExperimentRunner"