package com.example.frontend;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class InitialScreening extends AppCompatActivity {

    private TextView paragraphTextView, expectedReadingTimeView;
    private Chronometer chronometerTimer;
    private Button startStopButton, doneButton;
    private boolean isTimerRunning = false;
    private long timeElapsedMillis = 0;
    private int wordCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial_screening);

        paragraphTextView = findViewById(R.id.tvParagraph);
        expectedReadingTimeView = findViewById(R.id.expectedReadingTime);
        chronometerTimer = findViewById(R.id.chronometerTimer);
        startStopButton = findViewById(R.id.startStopButton);
        doneButton = findViewById(R.id.doneButton);

        fetchParagraphForAge();

        startStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleTimer();
            }
        });

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                processReadingResult();
            }
        });
    }

    private void fetchParagraphForAge() {
        int age = getUserAgeFromSharedPreferences();
        if (age == -1) {
            Log.e("InitialScreening", "Age not found in Shared Preferences");
            return;
        }

        String url = "http://192.168.10.4:5000/get_paragraph";
        JSONObject postData = new JSONObject();
        try {
            postData.put("age", age);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("InitialScreening", "JSON Exception", e);
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, postData,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        String paragraph = response.optString("paragraph");
                        wordCount = response.optInt("word_count", 0);
                        paragraphTextView.setText(paragraph);
                        displayExpectedReadingTime(wordCount);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Log.e("InitialScreening", "Error Response", error);
            }
        });

        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

    private void toggleTimer() {
        if (isTimerRunning) {
            chronometerTimer.stop();
            timeElapsedMillis = SystemClock.elapsedRealtime() - chronometerTimer.getBase();
            startStopButton.setText("Start");
        } else {
            chronometerTimer.setBase(SystemClock.elapsedRealtime() - timeElapsedMillis);
            chronometerTimer.start();
            startStopButton.setText("Stop");
        }
        isTimerRunning = !isTimerRunning;
    }

    private void processReadingResult() {
        int readingSpeed = calculateReadingSpeed(wordCount, timeElapsedMillis);
        boolean isNormal = isReadingAbilityNormal(getUserAgeFromSharedPreferences(), readingSpeed);

        String resultMessage;
        if (isNormal) {
            resultMessage = "Reading ability is within the normal range for your age group";
            Intent intent = new Intent(InitialScreening.this, InitialScreeningPosResult.class);
            intent.putExtra("resultMessage", resultMessage);
            startActivity(intent);
        } else {
            resultMessage = "Reading speed is outside the normal range for your age group";
            Intent intent = new Intent(InitialScreening.this, InitialScreeningNegResult.class);
            intent.putExtra("resultMessage", resultMessage);
            startActivity(intent);
        }

    }


    private int calculateReadingSpeed(int wordCount, long timeElapsedMillis) {
        double timeElapsedMinutes = timeElapsedMillis / 60000.0;
        return (int) (wordCount / timeElapsedMinutes);
    }

    private boolean isReadingAbilityNormal(int age, int readingSpeed) {
        if (age >= 4 && age <= 6) {
            return readingSpeed >= 60 && readingSpeed <= 80;
        } else if (age >= 7 && age <= 8) {
            return readingSpeed >= 115 && readingSpeed <= 138;
        } else if (age >= 9 && age <= 12) {
            return readingSpeed >= 158 && readingSpeed <= 185;
        }
        return false;
    }

    private void displayExpectedReadingTime(int wordCount) {
        int age = getUserAgeFromSharedPreferences();
        double averageSpeed = getAverageSpeedForAgeGroup(age);
        double expectedTime = wordCount / averageSpeed;
        double expectedTimeSeconds = expectedTime * 60;
        expectedReadingTimeView.setText(String.format("Expected reading time: %.2f seconds", expectedTimeSeconds));
    }

    private double getAverageSpeedForAgeGroup(int age) {
        if (age >= 4 && age <= 6) {
            return 70.0; // Average of 60 and 80
        } else if (age >= 7 && age <= 8) {
            return 126.5; // Average of 115 and 138
        } else if (age >= 9 && age <= 12) {
            return 171.5; // Average of 158 and 185
        }
        return 0;
    }

    private int getUserAgeFromSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserDetails", MODE_PRIVATE);
        return sharedPreferences.getInt("UserAge", -1);
    }
}




