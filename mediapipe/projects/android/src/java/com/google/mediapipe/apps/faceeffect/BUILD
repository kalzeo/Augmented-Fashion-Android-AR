# Copyright 2020 The MediaPipe Authors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

licenses(["notice"])

package(default_visibility = ["//visibility:private"])

cc_binary(
    name = "libmediapipe_jni.so",
    linkshared = 1,
    linkstatic = 1,
    deps = [
        "//mediapipe/graphs/face_effect:face_effect_gpu_deps",
        "//mediapipe/java/com/google/mediapipe/framework/jni:mediapipe_framework_jni",
    ],
)

cc_library(
    name = "mediapipe_jni_lib",
    srcs = [":libmediapipe_jni.so"],
    alwayslink = 1,
)

android_binary(
    name = "faceeffect",
    srcs = glob(["*.java"]),
    assets = [
        "//mediapipe/graphs/face_effect/data:1.pngblob",
        "//mediapipe/graphs/face_effect/data:2.pngblob",
        "//mediapipe/graphs/face_effect/data:3.pngblob",
        "//mediapipe/graphs/face_effect/data:4.pngblob",
        "//mediapipe/graphs/face_effect/data:5.pngblob",
        "//mediapipe/graphs/face_effect/data:6.pngblob",
        "//mediapipe/graphs/face_effect/data:7.pngblob",
        "//mediapipe/graphs/face_effect/data:8.pngblob",
        "//mediapipe/graphs/face_effect/data:9.pngblob",
        "//mediapipe/graphs/face_effect/data:10.pngblob",
        "//mediapipe/graphs/face_effect/data:11.pngblob",
        "//mediapipe/graphs/face_effect/data:12.pngblob",
        "//mediapipe/graphs/face_effect/data:13.pngblob",
        "//mediapipe/graphs/face_effect/data:14.pngblob",
        "//mediapipe/graphs/face_effect/data:15.pngblob",
        "//mediapipe/graphs/face_effect/data:16.pngblob",
        "//mediapipe/graphs/face_effect/data:17.pngblob",
        "//mediapipe/graphs/face_effect/data:18.pngblob",
        "//mediapipe/graphs/face_effect:face_effect_gpu.binarypb",
        "//mediapipe/modules/face_detection:face_detection_short_range.tflite",
        "//mediapipe/modules/face_geometry/data:geometry_pipeline_metadata_detection.binarypb",
        "//mediapipe/modules/face_geometry/data:geometry_pipeline_metadata_landmarks.binarypb",
        "//mediapipe/modules/face_landmark:face_landmark.tflite",
    ],
    assets_dir = "",
    manifest = "//mediapipe/projects/android/src/java/com/google/mediapipe/apps/camera:AndroidManifest.xml",
    manifest_values = {
        "applicationId": "com.google.mediapipe.apps.faceeffect",
        "appName": "Augmented Fashion AR App",
        "mainActivity": ".MainActivity",
        "cameraFacingFront": "True",
        "binaryGraphName": "face_effect_gpu.binarypb",
        "inputVideoStreamName": "input_video",
        "outputVideoStreamName": "output_video",
        "flipFramesVertically": "True",
        "converterNumBuffers": "2",
    },
    multidex = "native",
    deps = [
        ":mediapipe_jni_lib",
        "//mediapipe/projects/android/src/java/com/google/mediapipe/apps/camera:camera_lib",
        "//mediapipe/framework/formats:matrix_data_java_proto_lite",
        "//mediapipe/java/com/google/mediapipe/framework:android_framework",
        "//mediapipe/modules/face_geometry/protos:face_geometry_java_proto_lite",
        "@maven//:androidx_annotation_annotation",
        "@maven//:androidx_appcompat_appcompat",
        "@maven//:androidx_core_core",
    ],
)
