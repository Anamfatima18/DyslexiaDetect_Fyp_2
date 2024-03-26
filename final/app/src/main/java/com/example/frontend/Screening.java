package com.example.frontend;


import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.graphics.Color;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class Screening extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int REQUEST_CODE_PERMISSIONS = 101;
    private static final String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA};
    private boolean isCameraReady = false;
    private SharedPreferences sharedPreferences;
     public String token ;
    private NetworkUtils networkUtils;
    private ScheduledExecutorService cameraExecutor;
    private ImageCapture imageCapture;
    private final long delayBetweenPhotos = 500; // Adjust as needed
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_screening);
        cameraExecutor = Executors.newSingleThreadScheduledExecutor();
        //LinearLayout wordsContainer = findViewById(R.id.words_container);
        GridLayout wordsContainer = findViewById(R.id.words_container);
//        sharedPreferences = getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
//        token = sharedPreferences.getString("token", "");
        Log.e(TAG, "Token : " + token);

        networkUtils = new NetworkUtils(this);
        String paragraphText = "Here is a paragraph for you to read with exactly three words per line as you requested Here is a paragraph for you to read with exactly three words per line as you requested Here is a paragraph for you to read with exactly three words per line as you requested Here is a paragraph for you to read with exactly three words per line as you requested.";
        String[] words = paragraphText.split("\\s+"); // Split by whitespace
        //Get the instance of WindowManager
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();

        // Get the screen size in pixels
        display.getSize(size);
        int screenWidth = size.x;
        int screenHeight = size.y;
        Log.d("ScreenSize", "Width: " + screenWidth + " Height: " + screenHeight);
        sendDeviceMetrics(screenWidth , screenHeight);
        LinearLayout currentLineLayout = new LinearLayout(this);
        currentLineLayout.setOrientation(LinearLayout.HORIZONTAL);

        // Assuming 'words' array and 'wordsContainer' GridLayout are already defined
        for (int i = 0; i < words.length; i++) {
            TextView textView = new TextView(this);
            textView.setTextColor(Color.BLACK);
            textView.setText(words[i]);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0; // Use 0 for width for equal distribution
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.rightMargin = 5; // Adjust right margin for spacing between words
            params.topMargin = 20; // Adjust top margin for spacing between rows
            params.setGravity(Gravity.CENTER);
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f); // Equal weight to all columns
            textView.setLayoutParams(params);

            wordsContainer.addView(textView);
        }


        Button startButton = findViewById(R.id.startButton);
        Button stopButton = findViewById(R.id.stopButton);
        startButton.setOnClickListener(v -> startCapturing());
        stopButton.setOnClickListener(v -> stopCapturing());

        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
    }

    //    private void startCamera() {
//
//
//        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
//        cameraProviderFuture.addListener(() -> {
//            try {
//                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
//                bindCameraPreview(cameraProvider);
//                isCameraReady = true; // Camera is ready
//            } catch (Exception e) {
//                Log.e(TAG, "Use case binding failed", e);
//            }
//        }, ContextCompat.getMainExecutor(this));
//    }
    private void startCamera() {

        // Initialize CameraSelector for front-facing camera
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                .build();


        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                // Check camera availability (optional)
                if (!cameraProvider.hasCamera(cameraSelector)) {
                    // Handle camera not available scenario (e.g., show error message)
                    return;
                }

                // Create Preview and ImageCapture use cases
                Preview preview = new Preview.Builder().build();
                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();

                // Bind use cases to the camera lifecycle
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);

                isCameraReady = true; // Camera is ready
            } catch (Exception e) {
                Log.e(TAG, "Use case binding failed", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }


    private void sendDeviceMetrics(int width, int height) {
        String url = "http://192.168.10.4:5000/sendDeviceMetrics"; // Use 10.0.2.2 for localhost from the Android emulator
        RequestQueue queue = Volley.newRequestQueue(this);

        Map<String, Integer> params = new HashMap<>();
        params.put("width", width);
        params.put("height", height);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, url, new JSONObject(params), response -> {
                    // Handle response
                    Log.d("Response", response.toString());
                }, error -> {
                    // Handle error
                    Log.e("Error", error.toString());
                });

        queue.add(jsonObjectRequest);
    }

//    private void predictGazeData() {
//        // Create a Volley request
//        String url = "http://192.168.10.21:5000/predict";
//        StringRequest request = new StringRequest(Request.Method.GET, url,
//                new Response.Listener<String>() {
//                    @Override
//                    public void onResponse(String response) {
//                        try {
//                            // Extract and handle the prediction response
//                            JSONObject responseJson = new JSONObject(response);
//                            String prediction = responseJson.getString("prediction");
//                            Log.d("Prediction:", prediction);
//                            // Update UI or perform other actions based on the prediction
//
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                            // Handle JSON parsing errors
//                        }
//                    }
//                },
//                new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        Log.e("Error:", error.getMessage());
//                        // Handle network errors or other issues
//                    }
//                });
//
//        // Add the request to the request queue
//        RequestQueue queue = Volley.newRequestQueue(this);
//        queue.add(request);
//    }
private void predictGazeData() {
    // Retrieve the token from SharedPreferences
    SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
    String token = sharedPreferences.getString("token", "");

    String url = "http://192.168.10.4:5000/predict";
    StringRequest request = new StringRequest(Request.Method.GET, url,
            response -> {
                try {
                    // Extract and handle the prediction response
                    JSONObject responseJson = new JSONObject(response);
                    String prediction = responseJson.getString("prediction");
                    Log.d("Prediction:", prediction);
                    // Update UI or perform other actions based on the prediction
                } catch (JSONException e) {
                    e.printStackTrace();
                    // Handle JSON parsing errors
                }
            },
            error -> Log.e("Error:", error.toString())
    ) {
        @Override
        public Map<String, String> getHeaders() throws AuthFailureError {
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + token); // Include the token in the request headers
            return headers;
        }
    };

    // Add the request to the request queue
    RequestQueue queue = Volley.newRequestQueue(this);
    queue.add(request);
}


    void bindCameraPreview(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                .build();
        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build();
        cameraProvider.bindToLifecycle(this, cameraSelector, imageCapture, preview);
    }

    private void startCapturing() {
        captureAndProcessImage();
    }

    private void stopCapturing() {
        if (cameraExecutor != null && !cameraExecutor.isShutdown()) {
            cameraExecutor.shutdownNow();
        }
        predictGazeData();
    }



    private void sendImage(File photoFile , File screenshot ) {
        // Get the current timestamp in milliseconds
       long timestamp = System.currentTimeMillis();
      //  String timestamp = new SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(new Date());

        if (networkUtils != null) {

                // Create JSON object with timestamp
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("timestamp", timestamp);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                networkUtils.sendFrameToServer(photoFile, String.valueOf(timestamp), screenshot ,    new ImageUploadListener() {
                @Override
                public void onUploadSuccess(String response) {
                    // Handle successful upload (e.g., update UI)
                    Log.d("Screening", "Image upload successful: " + response);
                    Toast.makeText(Screening.this, "Image uploaded successfully!", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onUploadFailure(String error) {
                    // Handle upload failure (e.g., show an error message)
                    Log.e("Screening", "Image upload failed: " + error);
                    Toast.makeText(Screening.this, "Image upload failed. Please try again.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Log.e("Screening", "Error: NetworkUtils instance is null. Cannot send image to server.");
            Toast.makeText(Screening.this, "An error occurred. Please check your internet connection and try again.", Toast.LENGTH_LONG).show();
        }
    }

//    private void captureAndSaveScreenshot() {
//        // Obtain the root view and create a bitmap from it
//        View rootView = getWindow().getDecorView().getRootView();
//        Bitmap bitmap = Bitmap.createBitmap(rootView.getWidth(), rootView.getHeight(), Bitmap.Config.ARGB_8888);
//        Canvas canvas = new Canvas(bitmap);
//        rootView.draw(canvas);
//
//        // Filename with timestamp
//        String fileName = "screenshot_" + System.currentTimeMillis() + ".jpg";
//        File screenshotFile = new File(getFilesDir(), fileName);
//
//        try (FileOutputStream out = new FileOutputStream(screenshotFile)) {
//            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out); // PNG is a lossless format
//           networkUtils.sendScreenshotToServer(screenshotFile);
//                    // Implement this method to send the file
//        } catch (IOException e) {
//            Log.e(TAG, "Error saving screenshot", e);
//        }
//    }



//    private void captureAndProcessImage() {
//        if (cameraExecutor == null || cameraExecutor.isShutdown()) {
//            cameraExecutor = Executors.newSingleThreadScheduledExecutor();
//        }
//        cameraExecutor.scheduleWithFixedDelay(() -> {
//            if (Thread.interrupted() || !isCameraReady) {
//                return; // Check if the camera is not ready
//            }
//            String fileName = System.currentTimeMillis() + ".jpg";
//            File photoFile = new File(getExternalFilesDir(null), fileName);
//            ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();
//
//            imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(this), new ImageCapture.OnImageSavedCallback() {
//                @Override
//                public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
//                    Log.d(TAG, "Photo capture succeeded: " + fileName);
//                    sendImage(photoFile); // Send the captured image to the server
//                }
//
//                @Override
//                public void onError(@NonNull ImageCaptureException exception) {
//                    Log.e(TAG, "Photo capture failed: " + exception.getMessage(), exception);
//                }
//            });
//        }, 0, delayBetweenPhotos, TimeUnit.MILLISECONDS);
//    }
private void captureAndProcessImage() {
    if (cameraExecutor == null || cameraExecutor.isShutdown()) {
        cameraExecutor = Executors.newSingleThreadScheduledExecutor();
    }
    cameraExecutor.scheduleWithFixedDelay(() -> {
        if (Thread.interrupted() || !isCameraReady) {
            return; // Check if the camera is not ready
        }
        String frameFileName = System.currentTimeMillis() + "_frame.jpg";
        // Use internal storage for the photo file
        File photoFile = new File(getFilesDir(), frameFileName);
        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(this), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                Log.d(TAG, "Photo capture succeeded: " + frameFileName);
                // Capture screenshot and save it to internal storage
                File screenshotFile = captureScreenshot();
                if (screenshotFile != null) {
                    sendImage(photoFile, screenshotFile); // Send the captured image and screenshot to the server
                }
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Log.e(TAG, "Photo capture failed: " + exception.getMessage(), exception);
            }
        });
    }, 0, delayBetweenPhotos, TimeUnit.MILLISECONDS);
}

    private File captureScreenshot() {
        String screenshotFileName = System.currentTimeMillis() + "_screenshot.jpg";
        // Use internal storage for the screenshot file
        File screenshotFile = new File(getFilesDir(), screenshotFileName);
        View rootView = getWindow().getDecorView().getRootView();
        rootView.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(rootView.getDrawingCache());
        rootView.setDrawingCacheEnabled(false);

        try (FileOutputStream out = new FileOutputStream(screenshotFile)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            Log.d(TAG, "Screenshot capture succeeded: " + screenshotFileName);
            return screenshotFile;
        } catch (IOException e) {
            Log.e(TAG, "Screenshot capture failed: " + e.getMessage(), e);
            return null;
        }
    }






    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS && allPermissionsGranted()) {
            startCamera();
        } else {
            Log.e(TAG, "Permissions not granted by the user.");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraExecutor != null && !cameraExecutor.isShutdown()) {
            cameraExecutor.shutdown();
        }
    }
}
