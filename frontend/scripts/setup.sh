#!/bin/bash

# IoT Dashboard Frontend Setup Script
echo "🚀 Setting up IoT Dashboard Frontend..."

# Check if Node.js is installed
if ! command -v node &> /dev/null; then
    echo "❌ Node.js is not installed. Please install Node.js 16+ and try again."
    exit 1
fi

# Check Node.js version
NODE_VERSION=$(node -v | cut -d'v' -f2 | cut -d'.' -f1)
if [ "$NODE_VERSION" -lt 16 ]; then
    echo "❌ Node.js version 16+ is required. Current version: $(node -v)"
    exit 1
fi

echo "✅ Node.js version: $(node -v)"

# Check if npm is installed
if ! command -v npm &> /dev/null; then
    echo "❌ npm is not installed. Please install npm and try again."
    exit 1
fi

echo "✅ npm version: $(npm -v)"

# Install dependencies
echo "📦 Installing dependencies..."
npm install

if [ $? -ne 0 ]; then
    echo "❌ Failed to install dependencies"
    exit 1
fi

echo "✅ Dependencies installed successfully"

# Copy environment file
if [ ! -f .env ]; then
    echo "📝 Creating environment configuration..."
    cp env.example .env
    echo "✅ Environment file created. Please update .env with your configuration."
else
    echo "✅ Environment file already exists"
fi

# Create build directory
echo "🏗️  Creating build directory..."
mkdir -p build

echo "✅ Frontend setup completed successfully!"
echo ""
echo "📋 Next steps:"
echo "1. Update .env file with your API endpoints"
echo "2. Run 'npm start' to start the development server"
echo "3. Run 'npm run build' to create a production build"
echo ""
echo "🌐 The application will be available at http://localhost:3000"
