//
//
//
//package com.example.frontend;
//
//import android.content.Context;
//import android.content.SharedPreferences;
//import android.util.Log;
//
//import com.android.volley.DefaultRetryPolicy;
//import com.android.volley.Request;
//import com.android.volley.RequestQueue;
//import com.android.volley.Response;
//import com.android.volley.toolbox.Volley;
//import com.android.volley.NetworkResponse;
//import com.android.volley.ParseError;
//import com.android.volley.toolbox.HttpHeaderParser;
//
//import java.io.ByteArrayOutputStream;
//import java.io.DataOutputStream;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.Map;
//
//import javax.security.auth.callback.Callback;
//
//public class NetworkUtils {
//    private static final String SERVER_URL = "http://192.168.10.4:5000/process_frame";
//    private RequestQueue requestQueue;
//
////    public NetworkUtils(Context context) {
////        requestQueue = Volley.newRequestQueue(context);
////    }
//private SharedPreferences sharedPreferences;
//
//    public NetworkUtils(Context context) {
//        requestQueue = Volley.newRequestQueue(context);
//        sharedPreferences = context.getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
//    }
//
//
//    private String getToken() {
//
//        // Return the token from SharedPreferences, or an empty string if not found
//        return sharedPreferences.getString("token", "");
//
//
//    }
//
//
//
//
//
//    private byte[] convertFileToByteArray(File file) {
//        try (FileInputStream fileInputStream = new FileInputStream(file);
//             ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
//            byte[] buffer = new byte[1024];
//            int bytesRead;
//            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
//                byteArrayOutputStream.write(buffer, 0, bytesRead);
//            }
//            return byteArrayOutputStream.toByteArray();
//        } catch (IOException e) {
//            Log.e("NetworkUtils", "Error in file to byte array conversion: " + e.getMessage());
//            return null;
//        }
//    }
//
////public void sendFrameToServer(File file, String timestamp,  ImageUploadListener imageUploadListener) {
////    byte[] byteArray = convertFileToByteArray(file);
////    if (byteArray == null) {
////        Log.e("NetworkUtils", "Failed to convert file to byte array.");
////        return;
////    }
////
////    Log.d("NetworkUtils", "Preparing to send frame to server");
////    MultipartRequest multipartRequest = new MultipartRequest(SERVER_URL, byteArray, file.getName(),
////            response -> Log.d("NetworkUtils", "Server Response: " + response),
////            error -> Log.e("NetworkUtils", "Error response: " + error.toString())) {
////        String token = getToken();
////
////        @Override
////        public Map<String, String> getHeaders() {
////            Map<String, String> headers = new HashMap<>();
////            headers.put("Authorization", "Bearer " + token); // Add token to the request headers
////            return headers;
////        }
////    };
////
////    multipartRequest.setRetryPolicy(new DefaultRetryPolicy(
////            5000, // Timeout in milliseconds.
////            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
////            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
////
////    Log.d("NetworkUtils", "Sending frame to server");
////    requestQueue.add(multipartRequest);
////}
////public void sendFrameToServer(File frameFile, String timestamp, File screenshot, ImageUploadListener imageUploadListener) {
////    byte[] frameByteArray = convertFileToByteArray(frameFile);
////    if (frameByteArray == null) {
////        Log.e("NetworkUtils", "Failed to convert frame file to byte array.");
////        return;
////    }
////
////    byte[] screenshotByteArray = convertFileToByteArray(screenshot);
////    if (screenshotByteArray == null) {
////        Log.e("NetworkUtils", "Failed to convert screenshot file to byte array.");
////        return;
////    }
////
////    Log.d("NetworkUtils", "Preparing to send frame and screenshot to server");
////    MultipartRequest multipartRequest = new MultipartRequest(
////            SERVER_URL,
////            frameByteArray,
////            frameFile.getName(),
////            response -> Log.d("NetworkUtils", "Server Response: " + response),
////            error -> Log.e("NetworkUtils", "Error response: " + error.toString())) {
////        String token = getToken();
////
////        @Override
////        public Map<String, String> getHeaders() {
////            Map<String, String> headers = new HashMap<>();
////            headers.put("Authorization", "Bearer " + token); // Add token to the request headers
////            return headers;
////        }
////
////        @Override
////        public byte[] getBody() {
////            ByteArrayOutputStream bos = new ByteArrayOutputStream();
////            DataOutputStream dos = new DataOutputStream(bos);
////            try {
////                // Write frame data
////                dos.writeBytes("--" + Boundary + "\r\n");
////                dos.writeBytes("Content-Disposition: form-data; name=\"frame\"; filename=\"" + frameFile.getName() + "\"\r\n\r\n");
////                dos.writeBytes("Content-Type: image/jpeg\r\n\r\n");
////                dos.write(frameByteArray);
////                dos.writeBytes("\r\n");
////
////                // Write screenshot data
////                dos.writeBytes("--" + Boundary + "\r\n");
////                dos.writeBytes("Content-Disposition: form-data; name=\"screenshot\"; filename=\"" + screenshot.getName() + "\"\r\n\r\n");
////                dos.writeBytes("Content-Type: image/jpeg\r\n\r\n");
////                dos.write(screenshotByteArray);
////                dos.writeBytes("\r\n");
////
////                // End of the multipart form data
////                dos.writeBytes("--" + Boundary + "--\r\n");
////                Log.d("MultipartRequest", "Multipart request body prepared");
////                return bos.toByteArray();
////            } catch (IOException e) {
////                Log.e("MultipartRequest", "Error in preparing request body: " + e.getMessage());
////                e.printStackTrace();
////            }
////            return null;
////        }
////    };
////
////    multipartRequest.setRetryPolicy(new DefaultRetryPolicy(
////            5000, // Timeout in milliseconds.
////            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
////            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
////
////    Log.d("NetworkUtils", "Sending frame and screenshot to server");
////    requestQueue.add(multipartRequest);
////}
////
//public void sendFrameToServer(File frameFile, String timestamp, File screenshot, ImageUploadListener imageUploadListener) {
//    byte[] frameByteArray = convertFileToByteArray(frameFile);
//    if (frameByteArray == null) {
//        Log.e("NetworkUtils", "Failed to convert frame file to byte array.");
//        return;
//    }
//
//    byte[] screenshotByteArray = convertFileToByteArray(screenshot);
//    if (screenshotByteArray == null) {
//        Log.e("NetworkUtils", "Failed to convert screenshot file to byte array.");
//        return;
//    }
//
//    Log.d("NetworkUtils", "Preparing to send frame and screenshot to server");
//    MultipartRequest multipartRequest = new MultipartRequest(
//            SERVER_URL,
//            frameByteArray,
//            "image",
//            response -> Log.d("NetworkUtils", "Server Response: " + response),
//            error -> Log.e("NetworkUtils", "Error response: " + error.toString())) {
//        String token = getToken();
//
//        @Override
//        public Map<String, String> getHeaders() {
//            Map<String, String> headers = new HashMap<>();
//            headers.put("Authorization", "Bearer " + token); // Add token to the request headers
//            return headers;
//        }
//
//
//    private static class MultipartRequest extends Request<String> {
//        private final Response.Listener<String> mListener;
//        private final byte[] mFilePart;
//        private final String mFileName;
//        final String Boundary = "apiclient-" + System.currentTimeMillis();
//
//        public MultipartRequest(String url, byte[] filePart, String fileName,
//                                Response.Listener<String> listener,
//                                Response.ErrorListener errorListener) {
//            super(Method.POST, url, errorListener);
//            this.mListener = listener;
//            this.mFilePart = filePart;
//            this.mFileName = fileName;
//        }
//
//        @Override
//        public String getBodyContentType() {
//            return "multipart/form-data; boundary=" + Boundary;
//        }
//
//        @Override
//        public byte[] getBody() {
//            ByteArrayOutputStream bos = new ByteArrayOutputStream();
//            DataOutputStream dos = new DataOutputStream(bos);
//            try {
//                dos.writeBytes("--" + Boundary + "\r\n");
//                dos.writeBytes("Content-Disposition: form-data; name=\"image\"; filename=\"" +
//                        mFileName + "\"\r\n");
//                dos.writeBytes("\r\n");
//                dos.write(mFilePart);
//                dos.writeBytes("\r\n");
//                dos.writeBytes("--" + Boundary + "--\r\n");
//                Log.d("MultipartRequest", "Request body prepared");
//                return bos.toByteArray();
//            } catch (IOException e) {
//                Log.e("MultipartRequest", "Error in preparing request body: " + e.getMessage());
//                e.printStackTrace();
//            }
//            return null;
//        }
//
//        @Override
//        protected Response<String> parseNetworkResponse(NetworkResponse response) {
//            try {
//                String json = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
//                return Response.success(json, HttpHeaderParser.parseCacheHeaders(response));
//            } catch (Exception e) {
//                return Response.error(new ParseError(e));
//            }
//        }
//
//        @Override
//        protected void deliverResponse(String response) {
//            mListener.onResponse(response);
//        }
//    }
//}
package com.example.frontend;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class NetworkUtils {
    private static final String SERVER_URL = "http://192.168.10.4:5000/process_frame";
    private RequestQueue requestQueue;
    private SharedPreferences sharedPreferences;

    public NetworkUtils(Context context) {
        requestQueue = Volley.newRequestQueue(context);
        sharedPreferences = context.getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
    }

    private String getToken() {
        // Return the token from SharedPreferences, or an empty string if not found
        return sharedPreferences.getString("token", "");
    }

    private byte[] convertFileToByteArray(File file) {
        try (FileInputStream fileInputStream = new FileInputStream(file);
             ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            Log.e("NetworkUtils", "Error in file to byte array conversion: " + e.getMessage());
            return null;
        }
    }

    public void sendFrameToServer(File frameFile, String timestamp, File screenshot, ImageUploadListener imageUploadListener) {
        Log.d("NetworkUtils", "Frame file name: " + frameFile.getName());
        Log.d("NetworkUtils", "Screenshot file name: " + screenshot.getName());
        byte[] frameByteArray = convertFileToByteArray(frameFile);
        if (frameByteArray == null) {
            Log.e("NetworkUtils", "Failed to convert frame file to byte array.");
            return;
        }

        byte[] screenshotByteArray = convertFileToByteArray(screenshot);
        if (screenshotByteArray == null) {
            Log.e("NetworkUtils", "Failed to convert screenshot file to byte array.");
            return;
        }
       // String timestamp = getCurrentTimestamp();
        Log.d("NetworkUtils", "Preparing to send frame and screenshot to server");
        MultipartRequest multipartRequest = new MultipartRequest(
                SERVER_URL,
                frameByteArray,
                screenshotByteArray,
                frameFile.getName(), // Use frame file name as the file name for the frame
                screenshot.getName(), // Use screenshot file name as the file name for the screenshot
                timestamp, // Pass the timestamp to the request
                getToken(),
                response -> Log.d("NetworkUtils", "Server Response: " + response),
                error -> Log.e("NetworkUtils", "Error response: " + error.toString()));
        {
//            String token = getToken();

//            public Map<String, String> getHeaders() {
//                Map<String, String> headers = new HashMap<>();
//                headers.put("Authorization", "Bearer " + token); // Add token to the request headers
//                return headers;
//            }
        };

        multipartRequest.setRetryPolicy(new DefaultRetryPolicy(
                5000, // Timeout in milliseconds.
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        Log.d("NetworkUtils", "Sending frame and screenshot to server");
        requestQueue.add(multipartRequest);
    }

//    private static class MultipartRequest extends Request<String> {
//        private final Response.Listener<String> mListener;
//        private final byte[] mFramePart;
//        private final byte[] mScreenshotPart;
//        private final String mFrameFileName;
//        private final String mScreenshotFileName;
//        private final String mTimestamp;
//        final String Boundary = "apiclient-" + System.currentTimeMillis();
//
//        public MultipartRequest(String url, byte[] framePart, byte[] screenshotPart, String frameFileName, String screenshotFileName, String timestamp,
//                                Response.Listener<String> listener, Response.ErrorListener errorListener) {
//            super(Method.POST, url, errorListener);
//            this.mListener = listener;
//            this.mFramePart = framePart;
//            this.mScreenshotPart = screenshotPart;
//            this.mFrameFileName = frameFileName;
//            this.mScreenshotFileName = screenshotFileName;
//            this.mTimestamp = timestamp;
//        }
//
//
//        @Override
//        public String getBodyContentType() {
//            return "multipart/form-data; boundary=" + Boundary;
//        }
//
//        @Override
//        public byte[] getBody() {
//            ByteArrayOutputStream bos = new ByteArrayOutputStream();
//            DataOutputStream dos = new DataOutputStream(bos);
//            try {
//                // Add timestamp
//                dos.writeBytes("--" + Boundary + "\r\n");
//                dos.writeBytes("Content-Disposition: form-data; name=\"timestamp\"\r\n\r\n");
//                dos.writeBytes(mTimestamp + "\r\n");
//                // Add frame file
//                dos.writeBytes("--" + Boundary + "\r\n");
//                dos.writeBytes("Content-Disposition: form-data; name=\"image\"; filename=\"" +
//                        mFrameFileName + "\"\r\n");
//                dos.writeBytes("\r\n");
//                dos.write(mFramePart);
//                dos.writeBytes("\r\n");
//
//                // Add screenshot file
//                dos.writeBytes("--" + Boundary + "\r\n");
//                dos.writeBytes("Content-Disposition: form-data; name=\"screenshot\"; filename=\"" +
//                        mScreenshotFileName + "\"\r\n");
//                dos.writeBytes("\r\n");
//                dos.write(mScreenshotPart);
//                dos.writeBytes("\r\n");
//
//                dos.writeBytes("--" + Boundary + "--\r\n");
//                Log.d("MultipartRequest", "Request body prepared");
//                return bos.toByteArray();
//            } catch (IOException e) {
//                Log.e("MultipartRequest", "Error in preparing request body: " + e.getMessage());
//                e.printStackTrace();
//            }
//            return null;
//        }
//
//        @Override
//        protected Response<String> parseNetworkResponse(NetworkResponse response) {
//            try {
//                String json = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
//                return Response.success(json, HttpHeaderParser.parseCacheHeaders(response));
//            } catch (Exception e) {
//                return Response.error(new ParseError(e));
//            }
//        }
//
//        @Override
//        protected void deliverResponse(String response) {
//            mListener.onResponse(response);
//        }

    private static class MultipartRequest extends Request<String> {
        private final Response.Listener<String> mListener;
        private final byte[] mFramePart;
        private final byte[] mScreenshotPart;
        private final String mFrameFileName;
        private final String mScreenshotFileName;
        private final String mTimestamp;
        private final String mToken;
        final String Boundary = "apiclient-" + System.currentTimeMillis();

        public MultipartRequest(String url, byte[] framePart, byte[] screenshotPart, String frameFileName, String screenshotFileName,
                                String timestamp, String token,
                                Response.Listener<String> listener,
                                Response.ErrorListener errorListener) {
            super(Method.POST, url, errorListener);
            this.mListener = listener;
            this.mFramePart = framePart;
            this.mScreenshotPart = screenshotPart;
            this.mFrameFileName = frameFileName;
            this.mScreenshotFileName = screenshotFileName;
            this.mTimestamp = timestamp;
            this.mToken = token;
        }

        @Override
        public String getBodyContentType() {
            return "multipart/form-data; boundary=" + Boundary;
        }

        @Override
        public byte[] getBody() {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);
            try {
                // Add frame data to the request
                dos.writeBytes("--" + Boundary + "\r\n");
                dos.writeBytes("Content-Disposition: form-data; name=\"image\"; filename=\"" +
                        mFrameFileName + "\"\r\n");
                dos.writeBytes("\r\n");
                dos.write(mFramePart);
                dos.writeBytes("\r\n");

                // Add screenshot data to the request
                dos.writeBytes("--" + Boundary + "\r\n");
                dos.writeBytes("Content-Disposition: form-data; name=\"screenshot\"; filename=\"" +
                        mScreenshotFileName + "\"\r\n");
                dos.writeBytes("\r\n");
                dos.write(mScreenshotPart);
                dos.writeBytes("\r\n");

                // Add timestamp to the request
                dos.writeBytes("--" + Boundary + "\r\n");
                dos.writeBytes("Content-Disposition: form-data; name=\"timestamp\"\r\n");
                dos.writeBytes("\r\n");
                dos.writeBytes(mTimestamp + "\r\n");

                // End of the multipart form data
                dos.writeBytes("--" + Boundary + "--\r\n");

                Log.d("MultipartRequest", "Request body prepared");
                return bos.toByteArray();
            } catch (IOException e) {
                Log.e("MultipartRequest", "Error in preparing request body: " + e.getMessage());
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public Map<String, String> getHeaders() {
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + mToken); // Add token to the request headers
            return headers;
        }

        @Override
        protected Response<String> parseNetworkResponse(NetworkResponse response) {
            try {
                String json = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
                return Response.success(json, HttpHeaderParser.parseCacheHeaders(response));
            } catch (Exception e) {
                return Response.error(new ParseError(e));
            }
        }

        @Override
        protected void deliverResponse(String response) {
            mListener.onResponse(response);
        }
    }

}
