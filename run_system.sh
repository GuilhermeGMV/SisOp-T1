#!/bin/bash

echo "==================================="
echo "Testing Concurrent Operating System"
echo "==================================="
echo ""
echo "Compiling Java files..."
javac *.java

if [ $? -ne 0 ]; then
    echo "Compilation failed!"
    exit 1
fi

echo ""
echo "Compilation successful!"
echo ""
echo "Starting the system..."
echo "You can now:"
echo "  - Create processes: new fibonacciREAD"
echo "  - Check status: ps"
echo "  - Enable trace: traceon"
echo "  - Exit: exit"
echo ""
echo "==================================="
echo ""

java Sistema
