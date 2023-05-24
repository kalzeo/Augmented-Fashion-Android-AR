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
//landmarks
import com.google.mediapipe.formats.proto.LandmarkProto.NormalizedLandmark;
import com.google.mediapipe.formats.proto.LandmarkProto.NormalizedLandmarkList;
import com.google.mediapipe.framework.AndroidPacketCreator;
//timer
import java.util.Timer;
import java.util.TimerTask;


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

    ////FACE LANDMARK
    private static final String INPUT_NUM_FACES_SIDE_PACKET_NAME = "num_faces";
    private static final String OUTPUT_LANDMARKS_STREAM_NAME = "multi_face_landmarks";
    // Max number of faces to detect/process.
    private static final int NUM_FACES = 1;

    private static final boolean USE_FACE_DETECTION_INPUT_SOURCE = false;
    private static final boolean USE_FACE_LANDMARK_INPUT_SOURCE = true;
    private static final int MATRIX_TRANSLATION_Z_INDEX = 14;

    private final Object effectSelectionLock = new Object();

    // The ID for the face effect
    private int selectedEffectId;
    //int object to compare if the original one has changed
    private int selectedEffectIdPrevious = 0;

    private Button screenshotButton;
    private LinearLayout previewMaterialLayout;
    private HorizontalScrollView horizontalScrollView;
    private GestureDetector tapGestureDetector;

    //face landmarks
    // class member variable to save the X,Y coordinates
    private float[] lastTouchDownXY = new float[2];
    private float boxlimitTop,boxlimitRight,boxlimitLeft,boxlimitBottom;

    //timer
    Timer timer = new Timer();
    TimerTask timerTask;
    private boolean timerHide=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Circle for testing
        Drawable shapeDrawable = getResources().getDrawable(R.drawable.white_circle);
        Drawable cancelCircleDrawable = getResources().getDrawable(R.drawable.cancel_circle);

        // In order: Camera, Screenshot Button, Thumbnail Previews, Horizontal Scrollview container
        View viewGroup = findViewById(R.id.preview_display_layout);
        //manage x,y coordinates about where does the user click
        viewGroup.setOnTouchListener(touchListener);
//        viewGroup.setOnClickListener(clickListener);
        screenshotButton = findViewById(R.id.screenshot_button);
        previewMaterialLayout = findViewById(R.id.preview_materials_layout);
        horizontalScrollView = findViewById(R.id.horizontal_scrollview);

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

//         This callback demonstrates how the output face geometry packet can be obtained and used
//         in an Android app. As an example, the Z-translation component of the face pose transform
//         matrix is logged for each face being equal to the approximate distance away from the camera
//         in centimeters.
//        processor.addPacketCallback(
//                OUTPUT_FACE_GEOMETRY_STREAM_NAME,
//                (packet) -> {
//                    Log.d(TAG, "Received a multi face geometry packet.");
//                    List<FaceGeometry> multiFaceGeometry =
//                            PacketGetter.getProtoVector(packet, FaceGeometry.parser());
//
//                    StringBuilder approxDistanceAwayFromCameraLogMessage = new StringBuilder();
//                    for (FaceGeometry faceGeometry : multiFaceGeometry) {
//                        if (approxDistanceAwayFromCameraLogMessage.length() > 0) {
//                            approxDistanceAwayFromCameraLogMessage.append(' ');
//                        }
//                        MatrixData poseTransformMatrix = faceGeometry.getPoseTransformMatrix();
//                        approxDistanceAwayFromCameraLogMessage.append(
//                                -poseTransformMatrix.getPackedData(MATRIX_TRANSLATION_Z_INDEX));
//                    }
//
//                    Log.d(
//                            TAG,
//                            "[TS:"
//                                    + packet.getTimestamp()
//                                    + "] size = "
//                                    + multiFaceGeometry.size()
//                                    + "; approx. distance away from camera in cm for faces = ["
//                                    + approxDistanceAwayFromCameraLogMessage
//                                    + "]");
//                });

        ///////TEST///////////

        //try to initialize face landmark
//        AndroidPacketCreator packetCreator = processor.getPacketCreator();
//        Map<String, Packet> faceinputSidePackets = new HashMap<>();
//        faceinputSidePackets.put(INPUT_NUM_FACES_SIDE_PACKET_NAME, packetCreator.createInt32(NUM_FACES));
//        processor.setInputSidePackets(faceinputSidePackets);

//         To show verbose logging, run:
//         adb shell setprop log.tag.MainActivity VERBOSE
//        processor.addPacketCallback(
//                OUTPUT_LANDMARKS_STREAM_NAME,
//                (packet) -> {
////                  Log.v(TAG, "Received multi face landmarks packet.");
//                    List<NormalizedLandmarkList> multiFaceLandmarks =
//                            PacketGetter.getProtoVector(packet, NormalizedLandmarkList.parser());
//                    //initialize coordinates of the limit of the "bounding box"
//                    boxlimitTop = multiFaceLandmarks.get(0).getLandmarkList().get(10).getY()*1920f;
//                    boxlimitLeft = multiFaceLandmarks.get(0).getLandmarkList().get(234).getX()*1080f;
//                    boxlimitRight = multiFaceLandmarks.get(0).getLandmarkList().get(152).getX()*1080f;
//                    boxlimitBottom = multiFaceLandmarks.get(0).getLandmarkList().get(454).getY()*1920f;
//                    });

        /////////////////////

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

        //This callback demonstrates how the output normalized landmark packet can be obtained and used in an Android app.
        //initialize coordinates of used landmarks to create kind of bounding box
        processor.addPacketCallback(
                OUTPUT_LANDMARKS_STREAM_NAME,
                (packet) -> {
//                  Log.v(TAG, "Received multi face landmarks packet.");
                    List<NormalizedLandmarkList> multiFaceLandmarks =
                            PacketGetter.getProtoVector(packet, NormalizedLandmarkList.parser());
                    //initialize coordinates of the limit of the "bounding box"
                    boxlimitTop = multiFaceLandmarks.get(0).getLandmarkList().get(10).getY()*1920f; //multiply by 1920 allow to obtain an usable value as cooridnate to mark the top limit of the bounding box
                    boxlimitLeft = multiFaceLandmarks.get(0).getLandmarkList().get(234).getX()*1080f;//multiply by 1080 allow to obtain an usable value as cooridnate to mark the left limit of the bounding box
                    boxlimitRight = multiFaceLandmarks.get(0).getLandmarkList().get(454).getX()*viewGroup.getRight(); // *1080f make the right limit shorter
                    boxlimitBottom = multiFaceLandmarks.get(0).getLandmarkList().get(152).getY()*viewGroup.getBottom();// *9120f make the bottom limit shorter
                });
        //round float values of landmarks

        // We use the tap gesture detector to switch between face effects. This allows users to try
        // multiple pre-bundled face effects without a need to recompile the app.
        // We use the tap gesture detector to hide the UI components.
        tapGestureDetector =
                new GestureDetector(
                        this,
                        new GestureDetector.SimpleOnGestureListener() {

                            // TODO: Long press on face so that they can change the effect
                            @Override
                            public void onLongPress(MotionEvent event) {HideMaterialComponents();}

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
//                    timerHide=true;
//                    timer.schedule(timerTask = new TimerTask(){
//                        @Override
//                        public void run() {
//                            //                   Toast.makeText(context, "Apparition au bout de 3s", Toast.LENGTH_SHORT).show();
//                            //                    horizontalScrollView.setVisibility(View.GONE);
//                            timerHide=true;
//                        }
//                    }, 3000);
                }
            });

            // Add the button to the LinearLayout
            previewMaterialLayout.addView(button);

        }
//        if(timerHide){
//            Toast.makeText(this, "Apparition au bout de 3s", Toast.LENGTH_SHORT).show();
////            selectedEffectIdPrevious = selectedEffectId;
//            timerHide=false;
//        }
        screenshotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {TakeScreenshot();}
        });
    }

//    protected void timerHideScrollbarView(){
////        if (selectedEffectId != selectedEffectIdPrevious || selectedEffectId == selectedEffectIdPrevious){
////            Toast.makeText(this, selectedEffectId+" != "+selectedEffectIdPrevious, Toast.LENGTH_SHORT).show();
////            timer.schedule(timerTask = new TimerTask(){
////                @Override
////                public void run() {
//////                    Toast.makeText(context, "Apparition au bout de 3s", Toast.LENGTH_SHORT).show();
//////                    horizontalScrollView.setVisibility(View.GONE);
////                }
////            }, 3000);
//            if(timerHide){
//                Toast.makeText(this, "Apparition au bout de 3s", Toast.LENGTH_SHORT).show();
//                selectedEffectIdPrevious = selectedEffectId;
//                timerHide=false;
//            }
////        }
////        else {
////            Toast.makeText(this, selectedEffectId+" == "+selectedEffectIdPrevious, Toast.LENGTH_SHORT).show();
////        }
//    }


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
        HideAllComponents();

        View cameraView = findViewById(R.id.preview_display_layout);
        Bitmap cameraBitmap = GetCameraViewBitmap(cameraView);
        //Rect object based on screen's caracteristic
        Rect rect = new Rect(cameraView.getLeft(), cameraView.getTop(), cameraView.getRight(), cameraView.getBottom());
        //regarder la doc de cosumer pour faire commentaire
        Consumer<Bitmap> check= new Consumer<Bitmap>() {
            @Override
            public void accept(Bitmap bitmap) {
                saveImage(bitmap, filename);
            }
        };
        checkScreenshot(cameraBitmap, this, rect, check);
        HideAllComponents();

        /////OLD VERSION
        // Capture the bitmap of the rootView
//        HideAllComponents();
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
    private void HideAllComponents() {
        ToggleVisibility(previewMaterialLayout);
        ToggleVisibility(screenshotButton);
    }


    private void HideMaterialComponents() {
        if(lastTouchDownXY[1]>boxlimitTop && lastTouchDownXY[0]>boxlimitLeft && lastTouchDownXY[1]<boxlimitBottom && lastTouchDownXY[0]<boxlimitRight){
//            MultipleToggleVisibility(screenshotButton, previewMaterialLayout);
            ToggleVisibility(horizontalScrollView);
        }else{
            Toast.makeText(this, "Press on your face", Toast.LENGTH_SHORT).show();
        }

//        ToggleVisibility(previewMaterialLayout);
//        ToggleVisibility(screenshotButton);
    }

    private void HideScreenShotComponent(){
//        if(clickY<boxlimitTop){
//            Toast.makeText(this, "inf a la limite top"+clickY+"<"+boxlimitTop, Toast.LENGTH_SHORT).show();
//        } else if (clickX<boxlimitLeft) {
//            Toast.makeText(this, "inf a la limite gauche"+clickX+"<"+boxlimitLeft, Toast.LENGTH_SHORT).show();
//        } else if (clickY>boxlimitBottom) {
//            Toast.makeText(this, "sup a la limite bottom "+clickY+">"+boxlimitBottom, Toast.LENGTH_SHORT).show();
//        } else if (clickX>boxlimitRight) {
//            Toast.makeText(this, "sup a la limite droite "+clickX+">"+boxlimitRight, Toast.LENGTH_SHORT).show();
//        }else
        if (lastTouchDownXY[1]>boxlimitTop && lastTouchDownXY[0]>boxlimitLeft && lastTouchDownXY[1]<boxlimitBottom && lastTouchDownXY[0]<boxlimitRight){
            ToggleVisibility(screenshotButton);
//            Toast.makeText(this, "onClick: x = " + clickX + ", y = " + clickY, Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(this, "Tap on your face", Toast.LENGTH_SHORT).show();
        }
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

    //face landmarks

    // the purpose of the touch listener is just to store the touch X,Y coordinates
    View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {

            // save the X,Y coordinates
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                lastTouchDownXY[0] = event.getX();
                lastTouchDownXY[1] = event.getY();
            }

            // let the touch event pass on to whoever needs it
            return false;
        }
    };

    View.OnClickListener hideScrollListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
//            Context context = getApplicationContext();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    horizontalScrollView.setVisibility(View.GONE);
                }
            }, 3000);
//            Toast.makeText(context, "onClick: x = " + x + ", y = " + y, Toast.LENGTH_SHORT).show();
        }
    };

    //BOUDNING BOX

}
