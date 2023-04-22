#!/bin/bash

# Prompt user to enter sudo password
echo "Please enter your sudo password to continue..."
sudo -v

# Update all outdated packages and dependencies
echo "Updating packages and dependencies..."
sudo apt-get -qq update && echo "Update complete."

# Install git
echo "Installing git..."
sudo apt-get -qq install git && echo "Git installed."

# Install OpenCV and FFmpeg dependencies
echo "Installing OpenCV and FFmpeg dependencies..."
sudo apt-get -qq install -y libopencv-core-dev \
    libopencv-highgui-dev \
    libopencv-calib3d-dev \
    libopencv-features2d-dev \
    libopencv-imgproc-dev \
    libopencv-video-dev && echo "OpenCV and FFmpeg dependencies installed."

# Install Android Debug Bridge
echo "Installing Android Debug Bridge..."
sudo apt-get -qq install adb && echo "Android Debug Bridge installed."

# Install Python pip and the required packages
echo "Installing Python pip and required packages..."
sudo apt-get -qq install -y python3-pip && echo "Python pip installed."
echo "Installing required Python packages..."
pip3 install --user six absl-py attrs flatbuffers numpy opencv-contrib-python protobuf==3.11 sounddevice==0.4.4 && echo "Required Python packages installed."
