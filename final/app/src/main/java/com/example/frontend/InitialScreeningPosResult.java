package com.example.frontend;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class InitialScreeningPosResult extends AppCompatActivity {

    private static final String TAG = "InitialScreeningPosResu";
    private TextView resultTextView;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial_screening_pos_result);

        // Retrieve the passed message
        String resultMessage = getIntent().getStringExtra("resultMessage");

        // Find the TextView where you want to display the result
        resultTextView = findViewById(R.id.resultTextView);
        resultTextView.setText(resultMessage);

        // Send request to backend when the activity is created
        sendRequestToServer();
    }

    private void sendRequestToServer() {
        // URL of your API endpoint for initial screening result
        String url = "http://192.168.10.4:5000/InitialScreeningResult";

        // Example data for the request
        JSONObject requestData = new JSONObject();
        try {
            requestData.put("result", "positive"); // Set the result label to "positive"
        } catch (JSONException e) {
            Log.e(TAG, "JSON Exception", e);
        }

        // Get the token from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        String token = sharedPreferences.getString("token", "");

        // Create a request queue
        RequestQueue queue = Volley.newRequestQueue(this);

        // Create the request
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, requestData,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Handle the response
                        Log.d(TAG, "Response from server: " + response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle errors
                        Log.e(TAG, "Error sending request to server", error);
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                // Pass the token in the headers
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        // Add the request to the queue
        queue.add(request);
    }
}
