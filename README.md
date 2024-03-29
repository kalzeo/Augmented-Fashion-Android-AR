
# Augmented Fashion Android AR App

This document provides step-by-step instructions on how to set up and build the Augmented Fashion Android AR project. This project is a mobile application designed for Android that uses machine learning to visualize effects on a user's face, may extend the functionality to other use cases in the future. It relies on the MediaPipe framework, an open-source library that provides tools for building perception pipelines.

This guide is intended for developers who want to build and customize the application. It assumes that you have basic knowledge of Android app development, MediaPipe, and the Bazel build system.

The document is organized into various sections to walk you through how to get started. Each section explains a set of tasks that you need to perform to prepare the project for building. The instructions are accompanied by code snippets that will need to be entered in terminal.

By following this guide, you will be able to clone the project from GitHub, set up the required SDK and NDK versions, configure the project settings, and build the APK or install it directly on your device. You can use this project as a starting point for your own augmented reality applications powered by AI, or as a learning resource to understand how to use MediaPipe and AR technologies in Android.


## Platform

The app requires the Google Mediapipe framework, which is not natively supported on Windows. Therefore, it is recommended to use **Linux**, specifically Ubuntu 22.04.2 LTS.

## Prerequisite - Install Github CLI for Ubuntu
1. Install Git

   ```sudo apt-get install git -qq```

2. Install Github CLI through terminal using:

   ``````  
   type -p curl >/dev/null || (sudo apt update && sudo apt install curl -y -qq)  
   curl -fsSL https://cli.github.com/packages/githubcli-archive-keyring.gpg | sudo dd of=/usr/share/keyrings/githubcli-archive-keyring.gpg \  
   && sudo chmod go+r /usr/share/keyrings/githubcli-archive-keyring.gpg \  
   && echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/githubcli-archive-keyring.gpg] https://cli.github.com/packages stable main" | sudo tee /etc/apt/sources.list.d/github-cli.list > /dev/null \  
   && sudo apt update -qq \  
   && sudo apt install gh -y -qq  
   ``````  

3. Login to Github and following through the steps:

   ```gh auth login```
   > What account do you want to log into? GitHub.com  
   > What is your preferred protocol for Git operations? HTTPS  
   > How would you like to authenticate GitHub CLI? Login with a web browser  
   > Copy the code, press enter to launch GitHub and use the code to authorise

4. Clone the repo to your desktop:

   ```gh repo clone kalzeo/Augmented-Fashion-Android-AR ~/Desktop/Augmented-Fashion-Android-AR-master```

5. Navigate to the project folder using:

   ```cd ~/Desktop/Augmented-Fashion-Android-AR-master```

## Install via Scripts
I've created some scripts to speed up the installation process and would highly recommend using them. These scripts can be found in the helpers folder of the project and must be executed using terminal within this directory. If any of the scripts do not work, then follow the manual steps which can be found further down this README.

### Installing and Setting Up Android Studio
Execute the script ```install_android_studio.sh``` inside the helpers folder through terminal. This script will setup and install Android studio, all the relevant environment variables and necessary libraries. Some of the commands may require sudo privileges.

### Some Necessary Installs
Please ensure that you have the following dependencies installed on your system. These installs must be done through terminal, and some require sudo privileges. A script called 'install_dependencies.sh' can be found in the helpers folder in root directory of the project, open terminal and enter ```sh install_dependencies.sh```. You **MUST** also turn on developer settings on your Android device to enable USB debugging. Once USB debugging is turned on, you should get a popup message asking you to allow it when you connect your device to the PC. Allow it and run the following command in terminal to see whether the device is visible: ```adb devices```.

### Bazel Installation
A script called ```install_bazel.sh``` has been provided in the helpers folder to speed up the process of installing Bazel. It can be run using the following command: ```sh install_bazel.sh```. Again, this must be done through terminal inside the helpers directory and will require sudo privileges.

### Building

The project can be built in one of two ways: as an APK or through a direct install. Make sure you have a terminal opened in the root directory of the project.

**Build APK**:

```sh make_apk.sh```
> Any APK that gets built will be stored in the bazel-out folder. Typically it will be in ```bazel-bin/mediapipe/projects/android/src/java/com/google/mediapipe/apps/faceeffect```.

**Direct Install**:

```sh make_install.sh```
> **NOTE:** remove the `--start_app` flag in the script if you don't want the app to automatically launch once installed.


## Manual Steps
### Installing and Setting Up Android Studio
**ONLY DO THESE STEPS IF THE INSTALL SCRIPT DOES NOT WORK**

1. [Download Android Studio Flamingo | 2022.2.1 for Linux](https://redirector.gvt1.com/edgedl/android/studio/ide-zips/2022.2.1.18/android-studio-2022.2.1.18-linux.tar.gz)

2. Install JAVA JDK using:

   ```sudo apt-get install openjdk-11-jdk```

3. Install the required libraries for Android Studio with:

   ```sudo apt-get install libc6:i386 libncurses5:i386 libstdc++6:i386 lib32z1 libbz2-1.0:i386```

4. Unpack the Android Studio tar.gz file which should be in your download folder using:

   ```sudo tar -xf $HOME/Downloads/android-studio-2022.2.1.18-linux.tar.gz -C /opt/local/```

5. Add an Android Studio Desktop entry to make launching it easier / searchable using:
    ``````  
     sudo bash -c 'cat <<EOF > /usr/share/applications/android-studio.desktop  
    [Desktop Entry] Name=Android Studio Comment=Android Studio IDE Exec=/opt/local/android-studio/bin/studio.sh Icon=/opt/local/android-studio/bin/studio.png Terminal=false Type=Application Categories=Development;IDE; EOF'
    ``````  
6. Press the Windows key and search for Android Studio and follow the setup wizard.

7. Once the setup wizard completes, you should be on the "Welcome to Android Studio" page. Press More Actions > SDK Manager.

8. **IMPORTANT**: A SDK Manager settings window should be visible. Check the boxes 'Hide Obsolete Packages' and 'Show Package Details'.

9. **IMPORTANT**: On the SDK Platforms tab, check the boxes for 'Android SDK Platform 33' and 'Sources for Android 33' under Android 13.0 (Tiramisu).

10. **IMPORTANT**: On the SDK Tools tab, check the box for '33.0.2' under Android SDK Build-Tools 34-rc3.

11. **IMPORTANT**: On the SDK Tools tab, check the box for '21.4.7075529' under NDK (Side by side).

12. **IMPORTANT**: Setup the Android SDK and NDK environment variables using the following command in the terminal: ```sh setup_sdk_ndk_environment_variables.sh```
13. Close Android Studio

### Necessary Installs Before Anything Else

**ONLY DO THESE STEPS IF THE INSTALL SCRIPT DOES NOT WORK**

1. Update all outdated packages and dependencies on your system using:

   ```sudo apt-get update```

3. Install OpenCV and FFmpeg using:
      ```sudo apt-get install -y \  
     libopencv-core-dev \  
    libopencv-highgui-dev \ libopencv-calib3d-dev \ libopencv-features2d-dev \ libopencv-imgproc-dev \ libopencv-video-dev```
4. Install Android Debug Bridge using:

   ```sudo apt-get install adb```

5. Install Python pip and the required packages using:

   ```sudo apt-get install python3-pip```

   and

   ```pip3 install --user six absl-py attrs flatbuffers numpy opencv-contrib-python protobuf==3.11 sounddevice==0.4.4```

You **MUST** also turn on developer settings on your Android device to enable USB debugging. Once USB debugging is turned on, you should get a popup message asking you to allow it when you connect your device to the PC. Allow it and run the following command in terminal to see whether the device is visible:

```adb devices```

### Installing Bazel
**ONLY DO THESE STEPS IF THE INSTALL SCRIPT DOES NOT WORK**

1. *Add the Bazel distribution URI as a package source using*
   ``````  
   sudo apt install apt-transport-https curl gnupg -y &&  
   curl -fsSL https://bazel.build/bazel-release.pub.gpg | gpg --dearmor >bazel-archive-keyring.gpg &&  
   sudo mv bazel-archive-keyring.gpg /usr/share/keyrings &&  
   echo "deb [arch=amd64 signed-by=/usr/share/keyrings/bazel-archive-keyring.gpg] https://storage.googleapis.com/bazel-apt stable jdk1.8" | sudo tee /etc/apt/sources.list.d/bazel.list  
   ``````  

2. *Install and update Bazel 6.1.1 using* ```sudo apt update && sudo apt install bazel-6.1.1```

3. Check Bazel has installed correctly using ```bazel --version```
4. Set up the environment using ```export PATH="$PATH:$HOME/bin"```
6. The `android_sdk_repository` and `android_ndk_repository` paths will need to be configured in the WORKSPACE file found in the root directory of the project. Run the following script in the helpers folder: `sh setup_workspace_sdk_ndk_paths.sh`. This step will only need to be completed once.

### Building

The project can be built in one of two ways: as an APK or through a direct install. Make sure you have a terminal opened in the root directory of the project.

**Build APK**:

```bazel build -c opt --config=android_arm64 mediapipe/projects/android/src/java/com/google/mediapipe/apps/faceeffect:faceeffect```
> Any APK that gets built will be stored in the bazel-out folder. Typically it will be in ```bazel-bin/mediapipe/projects/android/src/java/com/google/mediapipe/apps/faceeffect```.

**Direct Install**:

```bazel mobile-install --start_app --config=android_arm64 --android_cpu=arm64-v8a //mediapipe/projects/android/src/java/com/google/mediapipe/apps/faceeffect:faceeffect --verbose_failures```
> **NOTE:** remove the `--start_app` flag if you don't want the app to automatically launch once installed.


## Opening the Project
**DO STEPS 1 AND 2 IF THE PROJECT HAS NOT BEEN DOWNLOADED / CLONED OTHERWISE GO TO STEP 3**.

1. Open Android Studio and click on "Get Project from Version Control" on the welcome screen.
2. Enter the URL as https://github.com/kalzeo/Augmented-Fashion-Android-AR.git and click the Clone button.
3. Open Android Studio and click on "Open" and find the project directory.
4. Once the project opens, go to `File > Project Structure > Project` and make sure the SDK is set to Android API 33.
5. Go to `File > Project Structure > Modules > mediapipe-0.9.3.0 > Android`. Make sure the Manifest file and Resources directory are correct. They should be along the lines of:

   Manifest file location:
   > /home/[your_username]/Desktop/Augmented-Fashion-Android-AR-master/mediapipe/projects/android/src/java/com/google/mediapipe/apps/camera/AndroidManifest.xml

   Resources directory location:
   > /home/[your_username]/Desktop/Augmented-Fashion-Android-AR-master/mediapipe/projects/android/src/java/com/google/mediapipe/apps/camera/res

   These paths may need to be adjusted to the location you have stored the files.  
