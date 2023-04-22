#!/bin/bash

# Prompt user to enter sudo password
echo "Please enter your sudo password to continue..."
sudo -v

# Add Bazel distribution URI as package source
echo "Adding Bazel distribution URI..."
sudo apt-get -qq install apt-transport-https curl gnupg -y && \
curl -fsSL https://bazel.build/bazel-release.pub.gpg | gpg --dearmor >bazel-archive-keyring.gpg && \
sudo mv bazel-archive-keyring.gpg /usr/share/keyrings && \
echo "deb [arch=amd64 signed-by=/usr/share/keyrings/bazel-archive-keyring.gpg] https://storage.googleapis.com/bazel-apt stable jdk1.8" | sudo tee /etc/apt/sources.list.d/bazel.list && \
echo "Bazel distribution URI added."

# Update packages and install Bazel 6.1.1
echo "Updating packages and installing Bazel 6.1.1..."
sudo apt-get -qq update && \
sudo apt-get -qq install bazel-6.1.1 && \
echo "Bazel 6.1.1 installed and updated."

# Check Bazel installation
echo "Checking Bazel installation..."
bazel --version && \
echo "Bazel installation check complete."

# Set up environment
echo "Setting up environment..."
export PATH="$PATH:$HOME/bin" && \
echo "Environment set up."
