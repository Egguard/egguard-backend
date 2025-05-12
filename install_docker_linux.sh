#!/bin/bash

set -e  # Exit immediately if a command exits with a non-zero status.

# Update package list
echo "Updating apt..."
sudo apt update

# Install Docker
echo "Installing docker.io..."
sudo apt install -y docker.io

# Enable and start Docker service
echo "Enabling and starting Docker..."
sudo systemctl enable docker
sudo systemctl start docker

# Create CLI plugins directory
echo "Creating Docker CLI plugins directory..."
mkdir -p ~/.docker/cli-plugins/

# Download Docker Compose
DOCKER_COMPOSE_VERSION="v2.36.0"
echo "Downloading Docker Compose version $DOCKER_COMPOSE_VERSION..."
curl -SL "https://github.com/docker/compose/releases/download/${DOCKER_COMPOSE_VERSION}/docker-compose-linux-x86_64" -o ~/.docker/cli-plugins/docker-compose

# Make it executable
echo "Setting executable permissions..."
chmod +x ~/.docker/cli-plugins/docker-compose

# Verify Docker Compose installation
echo "Verifying Docker Compose installation..."
docker compose version
