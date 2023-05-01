#!/bin/bash

bazel build -c opt --config=android_arm64 --android_cpu=arm64-v8a mediapipe/projects/android/src/java/com/google/mediapipe/apps/faceeffect:faceeffect
