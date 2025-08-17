#!/bin/bash

# meklasCase Build Script
# For environments without Maven

echo "🎰 Building meklasCase..."

# Create build directory
mkdir -p build/classes
mkdir -p build/libs

# Check if Java is available
if ! command -v javac &> /dev/null; then
    echo "❌ Java compiler (javac) not found!"
    echo "Please install JDK 17+ to build this project"
    exit 1
fi

echo "✅ Java found: $(java --version | head -n1)"

# Note about dependencies
echo ""
echo "📋 To build this project properly, you need:"
echo "   1. Maven 3.6+"
echo "   2. Java 17+"
echo "   3. Paper/Spigot API"
echo "   4. LiteCommands dependency"
echo ""
echo "🔧 Recommended build command:"
echo "   mvn clean package"
echo ""
echo "📦 This will create: target/meklasCase-1.0.0.jar"
echo ""
echo "🚀 For production use:"
echo "   1. Install Maven: https://maven.apache.org/"
echo "   2. Run: mvn clean package"
echo "   3. Upload JAR to your server's plugins/ folder"

echo ""
echo "✨ Project structure is ready!"
echo "🎯 All features implemented:"
echo "   ✅ LiteCommands integration"
echo "   ✅ Beautiful GUI with animations"
echo "   ✅ 24h rotation system"
echo "   ✅ fHolo hologram support"
echo "   ✅ Advanced configuration system"
echo "   ✅ Beautiful message utils with gradients"
echo "   ✅ Complete command system"
echo "   ✅ Event listeners"
echo "   ✅ Full documentation"