package com.example.frontend;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;

public class Otp extends AppCompatActivity {

    private static final String TAG = "OtpVerification";
    private EditText[] otpEditTexts = new EditText[6];
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);
        Log.d(TAG, "onCreate: Activity started");

        email = getIntent().getStringExtra("UserEmail");
        Log.d(TAG, "Received email: " + email);

        otpEditTexts[0] = findViewById(R.id.otpEdit1);
        otpEditTexts[1] = findViewById(R.id.otpEdit2);
        otpEditTexts[2] = findViewById(R.id.otpEdit3);
        otpEditTexts[3] = findViewById(R.id.otpEdit4);
        otpEditTexts[4] = findViewById(R.id.otpEdit5);
        otpEditTexts[5] = findViewById(R.id.otpEdit6);
        // Initialize other EditTexts and add them to the otpEditTexts array
        // ...

        Button buttonVerifyOtp = findViewById(R.id.buttonVerifyOtp);
        buttonVerifyOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String otp = getOtpFromEditTexts();
                Log.d(TAG, "OTP entered: " + otp);
                verifyOtp(email, otp);
            }
        });
        setupOtpEditTexts();
    }

    private void setupOtpEditTexts() {
        for (int i = 0; i < otpEditTexts.length; i++) {
            final int index = i;
            otpEditTexts[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 1 && index < otpEditTexts.length - 1) {
                        otpEditTexts[index + 1].requestFocus();
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }
    private String getOtpFromEditTexts() {
        StringBuilder otp = new StringBuilder();
        for (EditText editText : otpEditTexts) {
            otp.append(editText.getText().toString());
        }
        Log.d(TAG, "Concatenated OTP: " + otp.toString());
        return otp.toString();
    }

    private void verifyOtp(String email, String otp) {
        Log.d(TAG, "verifyOtp: Verifying OTP");
        String url = "http://192.168.10.4:5000/verify";  // Replace with your server URL
        RequestQueue queue = Volley.newRequestQueue(this);

        JSONObject postData = new JSONObject();
        try {
            postData.put("email", email);
            postData.put("otp", otp);
        } catch (JSONException e) {
            Log.e(TAG, "JSON Exception", e);
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, postData,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "OTP Verification successful");
                        Toast.makeText(Otp.this, "OTP Verified!", Toast.LENGTH_LONG).show();
                        // Navigate to SignInActivity or next screen
                        Intent intent = new Intent(Otp.this, SignIn.class);
                        startActivity(intent);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Error during OTP verification", error);
                Toast.makeText(Otp.this, "Error during OTP verification: " + error.toString(), Toast.LENGTH_LONG).show();
            }
        });

        queue.add(jsonObjectRequest);
    }
}
