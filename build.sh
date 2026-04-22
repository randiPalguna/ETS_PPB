#!/bin/bash

# MyMoney Notes - Build Script

echo "Building MyMoney Notes APK..."

# Clean previous builds
./gradlew clean

# Build Debug APK
./gradlew assembleDebug

# Copy APK to docs folder
mkdir -p docs
cp app/build/outputs/apk/debug/app-debug.apk docs/MyMoneyNotes-v1.0.apk

echo "Build complete! APK available at: docs/MyMoneyNotes-v1.0.apk"
