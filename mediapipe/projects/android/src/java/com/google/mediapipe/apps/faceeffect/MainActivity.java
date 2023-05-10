// Copyright 2020 The MediaPipe Authors.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.mediapipe.apps.faceeffect;

import android.content.res.AssetFileDescriptor;
import android.graphics.Color;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.MeasureSpec;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.HorizontalScrollView;
import android.content.Intent;
import android.app.Activity;
import com.google.mediapipe.framework.Packet;
import com.google.mediapipe.framework.PacketGetter;
import com.google.mediapipe.modules.facegeometry.FaceGeometryProto.FaceGeometry;
import com.google.mediapipe.formats.proto.MatrixDataProto.MatrixData;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.webkit.MimeTypeMap;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Date;
import android.content.Context;
import androidx.core.content.ContextCompat;
import androidx.core.app.ActivityCompat;
import androidx.annotation.NonNull;
import android.Manifest;
import android.view.PixelCopy;
import android.graphics.Rect;
import android.os.Looper;
import android.os.Handler;
import java.util.function.Consumer;


/**
 * Main activity of MediaPipe face mesh app.
 */
public class MainActivity extends com.google.mediapipe.apps.camera.MainActivity {
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] permissionstorage = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_MEDIA_IMAGES};
    private static final int REQUEST_CODE_PERMISSION_WRITE_EXTERNAL_STORAGE = 123;


    private static final String TAG = "MainActivity";

    // Side packet / stream names.
    private static final String USE_FACE_DETECTION_INPUT_SOURCE_INPUT_SIDE_PACKET_NAME =
            "use_face_detection_input_source";
    private static final String SELECTED_EFFECT_ID_INPUT_STREAM_NAME = "selected_effect_id";
    private static final String OUTPUT_FACE_GEOMETRY_STREAM_NAME = "multi_face_geometry";

    private static final boolean USE_FACE_DETECTION_INPUT_SOURCE = false;
    private static final int MATRIX_TRANSLATION_Z_INDEX = 14;

    private final Object effectSelectionLock = new Object();

    // The ID for the face effect
    private int selectedEffectId;

    private Button screenshotButton;
    private LinearLayout previewMaterialLayout;
    private ViewGroup viewGroup;
    private GestureDetector tapGestureDetector;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Circle for testing
        Drawable shapeDrawable = getResources().getDrawable(R.drawable.white_circle);
        Drawable cancelCircleDrawable = getResources().getDrawable(R.drawable.cancel_circle);

        // In order: Camera, Screenshot Button, Thumbnail Previews, Horizontal Scrollview container
        viewGroup = findViewById(R.id.preview_display_layout);
        screenshotButton = findViewById(R.id.screenshot_button);
        previewMaterialLayout = findViewById(R.id.preview_materials_layout);
        HorizontalScrollView horizontalScrollView = findViewById(R.id.horizontal_scrollview);

        // Face effect is 0 by default i.e. first effect.
        selectedEffectId = 0;

        // Array to hold face effects
        Drawable[] thumbnails = new Drawable[18]; // create an array to hold 18 drawables
        //thumbnails[0] = cancelCircleDrawable;

        // Loop through the face resources and add the effect to the arraya
        for (int i = 0; i < 18; i++) {
            String drawableName = "thumbnail_" + (i + 1);
            int resourceId = getResources().getIdentifier(drawableName, "drawable", getPackageName());
            thumbnails[i] = getResources().getDrawable(resourceId);
        }

        // Pass the USE_FACE_DETECTION_INPUT_SOURCE flag value as an input side packet into the graph.
        Map<String, Packet> inputSidePackets = new HashMap<>();
        inputSidePackets.put(
                USE_FACE_DETECTION_INPUT_SOURCE_INPUT_SIDE_PACKET_NAME,
                processor.getPacketCreator().createBool(USE_FACE_DETECTION_INPUT_SOURCE));
        processor.setInputSidePackets(inputSidePackets);

        // This callback demonstrates how the output face geometry packet can be obtained and used
        // in an Android app. As an example, the Z-translation component of the face pose transform
        // matrix is logged for each face being equal to the approximate distance away from the camera
        // in centimeters.
        processor.addPacketCallback(
                OUTPUT_FACE_GEOMETRY_STREAM_NAME,
                (packet) -> {
                    Log.d(TAG, "Received a multi face geometry packet.");
                    List<FaceGeometry> multiFaceGeometry =
                            PacketGetter.getProtoVector(packet, FaceGeometry.parser());

                    StringBuilder approxDistanceAwayFromCameraLogMessage = new StringBuilder();
                    for (FaceGeometry faceGeometry : multiFaceGeometry) {
                        if (approxDistanceAwayFromCameraLogMessage.length() > 0) {
                            approxDistanceAwayFromCameraLogMessage.append(' ');
                        }
                        MatrixData poseTransformMatrix = faceGeometry.getPoseTransformMatrix();
                        approxDistanceAwayFromCameraLogMessage.append(
                                -poseTransformMatrix.getPackedData(MATRIX_TRANSLATION_Z_INDEX));
                    }

                    Log.d(
                            TAG,
                            "[TS:"
                                    + packet.getTimestamp()
                                    + "] size = "
                                    + multiFaceGeometry.size()
                                    + "; approx. distance away from camera in cm for faces = ["
                                    + approxDistanceAwayFromCameraLogMessage
                                    + "]");
                });

        // Alongside the input camera frame, we also send the `selected_effect_id` int32 packet to
        // indicate which effect should be rendered on this frame.
        processor.setOnWillAddFrameListener(
                (timestamp) -> {
                    Packet selectedEffectIdPacket = null;
                    try {
                        synchronized (effectSelectionLock) {
                            selectedEffectIdPacket = processor.getPacketCreator().createInt32(selectedEffectId);
                        }

                        processor
                                .getGraph()
                                .addPacketToInputStream(
                                        SELECTED_EFFECT_ID_INPUT_STREAM_NAME, selectedEffectIdPacket, timestamp);
                    } catch (RuntimeException e) {
                        Log.e(
                                TAG, "Exception while adding packet to input stream while switching effects: " + e);
                    } finally {
                        if (selectedEffectIdPacket != null) {
                            selectedEffectIdPacket.release();
                        }
                    }
                });

        // We use the tap gesture detector to switch between face effects. This allows users to try
        // multiple pre-bundled face effects without a need to recompile the app.
        // We use the tap gesture detector to hide the UI components.
        tapGestureDetector =
                new GestureDetector(
                        this,
                        new GestureDetector.SimpleOnGestureListener() {

                            // TODO: Long press on face so that they can change the effect
                            @Override
                            public void onLongPress(MotionEvent event) {
                                HideAllUIComponents();
                            }

                            // DONE: Hide UI when screen is tapped, ideally it should only hide the screenshot button since the Horizontal Scrollview with the preview effects should only appear when a long press is done,
                            @Override
                            public boolean onSingleTapUp(MotionEvent event) {
                                HideScreenShotComponent();
                                return true;
                            }
                        });

        // Loop to create the buttons in the horizontal scrollview with the preview image of each effect
        for (int i = 0; i < thumbnails.length; i++) {
            // Create a new Button
            Button button = new Button(this);

            // Set the tag to the index of the button
            button.setTag(i);

            // Set the width and height of the button
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(115, 115);
            params.setMargins(10, 0, 10, 0); // set horizontal margin of 10dp
            button.setLayoutParams(params);

            // Set the layer drawable as the background of the button
            button.setBackground(thumbnails[i]);

            // Set an OnClickListener for the button
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Get the tag value of the button that was clicked and assign to material
                    int index = (int) v.getTag();
                    selectedEffectId = index;
                }
            });

            // Add the button to the LinearLayout
            previewMaterialLayout.addView(button);
        }

        screenshotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {TakeScreenshot();}
        });
//        setContentView(R.layout.activity_main);
    }


    /**
     * Takes a screenshot and saves it to the devices storage. The GetCameraViewBitmap method needs
     * fixed so that the camera view can be included in the screenshot. Currently, the camera preview
     * is shown as black.
     */
    private void TakeScreenshot() {
        //NOTE : Since SDK 30, WRITE_EXTERNAL_STORAGE is deprecated. Also, the permission is already managed with onRequestPermissionsResult()
        // Check for permission to write to external storage
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
//                    REQUEST_CODE_PERMISSION_WRITE_EXTERNAL_STORAGE);
//            return;
//        }

        // Create a filename for the screenshot
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
        String filename = "AugmentedFashionFace_" + dateFormat.format(now) + ".jpg";

        // Capture the bitmap of the camera view
        HideAllUIComponents();

        View cameraView = findViewById(R.id.preview_display_layout);
        Bitmap cameraBitmap = GetCameraViewBitmap(viewGroup);
        //Rect object based on screen's caracteristic
        Rect rect = new Rect(viewGroup.getLeft(), viewGroup.getTop(), viewGroup.getRight(), viewGroup.getBottom());
        //regarder la doc de cosumer pour faire commentaire
        Consumer<Bitmap> check= new Consumer<Bitmap>() {
            @Override
            public void accept(Bitmap bitmap) {
                saveImage(bitmap, filename);
            }
        };
        checkScreenshot(cameraBitmap, this, rect, check);
        HideAllUIComponents();

        /////OLD VERSION
        // Capture the bitmap of the rootView
//        HideAllUIComponents();
//        View rootView = getWindow().getDecorView().getRootView();
//        rootView.setDrawingCacheEnabled(true);
//        Bitmap rootViewBitmap = Bitmap.createBitmap(rootView.getDrawingCache());
//        rootView.setDrawingCacheEnabled(false);


        // Combine the two bitmaps into a single bitmap
//        Bitmap combinedBitmap = Bitmap.createBitmap(rootViewBitmap.getWidth(), rootViewBitmap.getHeight() + cameraBitmap.getHeight(), Bitmap.Config.ARGB_8888);
//        Bitmap combinedBitmap = Bitmap.createBitmap(rootViewBitmap.getWidth(), rootViewBitmap.getHeight(), Bitmap.Config.ARGB_8888);
//        Canvas canvas = new Canvas(combinedBitmap);
//        Canvas canvas = new Canvas(cameraBitmap);
//        cameraView.layout(cameraView.getLeft(), cameraView.getTop(), cameraView.getRight(), cameraView.getBottom());
//        cameraView.draw(canvas);
//        canvas.drawBitmap(cameraBitmap, 0f, 0f, null);


        // Save the screenshot to the Pictures directory
//        ContentResolver resolver = getContentResolver();
//        ContentValues contentValues = new ContentValues();
//        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, filename);
//        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
//        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);
//        Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
//
////        String path = getRealPathFromURI(imageUri);
////        Bitmap usableBitmap = decodeBitmap(path);
//
//        try {
////            OutputStream imageOutputStream = resolver.openOutputStream(imageUri);
//            OutputStream imageOutputStream = resolver.openOutputStream(imageUri);
////            usableBitmap.compress(Bitmap.CompressFormat.JPEG, 100, imageOutputStream);
////            rootViewBitmap.compress(Bitmap.CompressFormat.JPEG, 100, imageOutputStream);
//            combinedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, imageOutputStream);
//
//            imageOutputStream.close();
//            Toast.makeText(this, "Screenshot saved", Toast.LENGTH_SHORT).show();
//            OpenScreenshot(imageUri);
//        } catch (IOException e) {
//            Toast.makeText(this, "Failed to save screenshot", Toast.LENGTH_SHORT).show();
//            //e.printStackTrace();
//        }

    }

    /**
     * Open a screenshot when it is passed a filepath
     * @param screenshot - the filepath to the screenshot
     */
    private void OpenScreenshot(Uri screenshot) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(screenshot, "image/jpeg");
        startActivity(intent);
    }

    ////////////////////

    /**
     *
     * @param bitmap
     * @param activity
     * @param viewRect
     * @param onSuccess
     */
    private void checkScreenshot(Bitmap bitmap, Activity activity, Rect viewRect, Consumer<Bitmap> onSuccess) {
//        Bitmap bitmap = Bitmap.createBitmap(viewRect.width(), viewRect.height(), Bitmap.Config.ARGB_8888);
        //regarder doc pixelcopy pour faire commentaire
        PixelCopy.request(previewDisplayView, viewRect, bitmap,
                (copyResult) -> {
                    if (copyResult == PixelCopy.SUCCESS) {
                        onSuccess.accept(bitmap);
                    } else {
                        throw new RuntimeException("problem with taking a screenshot");
                    }
                    bitmap.recycle();
                },
                new Handler(Looper.getMainLooper())
        );
    }

    /**
     * Save an image to a phone's gallery
     * @param bitmap - The bitmap that it has to be convert as an Image file to be saved into the gallery .
     * @param Filename - The string that contains the name of the file.
     */
    protected void saveImage(Bitmap bitmap, String Filename) {
        if (android.os.Build.VERSION.SDK_INT >= 29) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, Filename);
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);
            values.put(MediaStore.Images.Media.IS_PENDING, true);

            Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (uri != null) {
                try {
                    saveImageToStream(bitmap, getContentResolver().openOutputStream(uri));
                    values.put(MediaStore.Images.Media.IS_PENDING, false);
                    this.getContentResolver().update(uri, values, null, null);
                    Toast.makeText(this, "Screenshot saved", Toast.LENGTH_SHORT).show();
                    OpenScreenshot(uri);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    /**
     * Save an image using a given outputstream and a given bitmap
     */
    private void saveImageToStream(Bitmap bitmap, OutputStream outputStream) {
        if (outputStream != null) {
            try {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                outputStream.flush();
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //////////////////

    /**
     * TODO
     * Fetch the current View of the camera so that it can be included in the screenshot.
     * Obtain a bitmap from the given view
     * @return A bitmap of the view
     */
    private Bitmap GetCameraViewBitmap(View v) {
        // Find the camera view by its ID
//        View cameraView = findViewById(R.id.preview_display_layout);
        Bitmap bitmap = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        v.layout(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
        v.draw(canvas);
        return bitmap;
    }

    /**
     * Toggle the visibility of a View to show or hide it
     * @param v - The View to be hidden or shown.
     */
    private void ToggleVisibility(View v) {
        v.setVisibility(v.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
    }

    /**
     * Toggle the visibility of two Views to show or hide them
     * We did this on only two views to represent Screenshot button and filter's Scrollbar
     * @param v1 - The View to be hidden or shown.
     */
    private void MultipleToggleVisibility(View v1, View v2){
        switch (v1.getVisibility()){
            case View.VISIBLE:
                if(v2.getVisibility()==View.VISIBLE){
                    v1.setVisibility(View.GONE);
                    v2.setVisibility(View.GONE);
                }else{
                    v2.setVisibility(View.VISIBLE);
                }
                break;
            case View.GONE:
                if(v2.getVisibility()==View.VISIBLE){
                    v1.setVisibility(View.VISIBLE);
                }else{
                    v1.setVisibility(View.VISIBLE);
                    v2.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    /**
     * Hide all the components in one go, useful for when it comes to capturing
     * the screenshot so that only the camera view and face effect will be shown.
     */
    private void HideAllUIComponents() {
        MultipleToggleVisibility(screenshotButton, previewMaterialLayout);
//        ToggleVisibility(previewMaterialLayout);
//        ToggleVisibility(screenshotButton);
    }

    private void HideScreenShotComponent(){
        ToggleVisibility(screenshotButton);
    }

    /***************OVERRIDES**********************************/
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return tapGestureDetector.onTouchEvent(event);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSION_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                TakeScreenshot();
            } else {
                Toast.makeText(this, "Permission to save screenshot denied", Toast.LENGTH_LONG).show();
            }
        }
    }
}
