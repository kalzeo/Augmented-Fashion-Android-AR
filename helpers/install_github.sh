#!/bin/bash

echo "Installing Git..."
sudo apt-get install git -qq
echo "Git installed successfully!"

echo "Installing GitHub CLI..."
type -p curl >/dev/null || (sudo apt update -qq && sudo apt install curl -y -qq)
curl -fsSL https://cli.github.com/packages/githubcli-archive-keyring.gpg | sudo dd of=/usr/share/keyrings/githubcli-archive-keyring.gpg >/dev/null \
&& sudo chmod go+r /usr/share/keyrings/githubcli-archive-keyring.gpg >/dev/null \
&& echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/githubcli-archive-keyring.gpg] https://cli.github.com/packages stable main" | sudo tee /etc/apt/sources.list.d/github-cli.list > /dev/null \
&& sudo apt update -qq \
&& sudo apt install gh -y -qq
echo "GitHub CLI installed successfully!"

echo "Logging in to GitHub..."
gh auth login
echo "Logged in successfully!"

echo "Cloning the repository to your desktop..."
gh repo clone kalzeo/Augmented-Fashion-Android-AR ~/Desktop/Augmented-Fashion-Android-AR-master -q
echo "Repository cloned successfully!"

echo "Navigating to project folder..."
cd ~/Desktop/Augmented-Fashion-Android-AR-master
echo "Done! You are now in the project folder."

