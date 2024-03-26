//package com.mehreenishtiaq.dyslexiadetectfyp;
//
//import android.app.ProgressDialog;
//import android.content.Intent;
//import android.os.Bundle;
//import android.util.Log;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.android.volley.Request;
//import com.android.volley.RequestQueue;
//import com.android.volley.toolbox.StringRequest;
//import com.android.volley.toolbox.Volley;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
//
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import java.util.HashMap;
//import java.util.Map;
//
//public class SignUp extends AppCompatActivity {
//
//    private EditText eTEmail, eTName, eTUsername, eTAge, eTPassword;
//    private Button signUpButton;
//    private ProgressDialog dialog;
//    private TextView login;
//
//    private FirebaseAuth mAuth;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_sign_up);
//
//        mAuth = FirebaseAuth.getInstance();
//
//        dialog = new ProgressDialog(SignUp.this);
//        dialog.setCancelable(true);
//
//        signUpButton = findViewById(R.id.signUpButton);
//        eTEmail = findViewById(R.id.eTEmail);
//        eTName = findViewById(R.id.eTName);
//        eTUsername = findViewById(R.id.eTUsername);
//        eTAge = findViewById(R.id.eTAge);
//        eTPassword = findViewById(R.id.eTPassword);
//        login = findViewById(R.id.goTologin);
//
//        login.setOnClickListener(view -> startActivity(new Intent(SignUp.this, Login.class)));
//
//        signUpButton.setOnClickListener(view -> {
//            String email = eTEmail.getText().toString().trim();
//            String name = eTName.getText().toString().trim();
//            String username = eTUsername.getText().toString().trim();
//            String age = eTAge.getText().toString().trim();
//            String password = eTPassword.getText().toString().trim();
//
//            if (validateInput(email, name, username, age, password)) {
//                registerUser(email, password, name, username, age);
//            }
//        });
//    }
//
//    private boolean validateInput(String email, String name, String username, String age, String password) {
//        if (email.isEmpty() || !email.contains("@")) {
//            eTEmail.setError("Valid email required");
//            return false;
//        }
//        if (name.isEmpty()) {
//            eTName.setError("Name required");
//            return false;
//        }
//        if (username.isEmpty()) {
//            eTUsername.setError("Username required");
//            return false;
//        }
//        if (age.isEmpty()) {
//            eTAge.setError("Age required");
//            return false;
//        }
//        if (password.isEmpty()) {
//            eTPassword.setError("Password required");
//            return false;
//        }
//        return true;
//    }
//
//    private void registerUser(String email, String password, String name, String username, String age) {
//        dialog.setTitle("Registering");
//        dialog.setMessage("Please wait...");
//        dialog.show();
//
//        mAuth.createUserWithEmailAndPassword(email, password)
//                .addOnCompleteListener(this, task -> {
//                    if (task.isSuccessful()) {
//                        FirebaseUser user = mAuth.getCurrentUser();
//                        if (user != null) {
//                            user.sendEmailVerification()
//                                    .addOnCompleteListener(task1 -> {
//                                        dialog.dismiss();
//                                        if (task1.isSuccessful()) {
//                                            Log.d("EmailVerification", "Email sent.");
//                                            Toast.makeText(SignUp.this, "Verification email sent to " + email + ". Please verify to complete registration.", Toast.LENGTH_LONG).show();
//                                            // Redirect to Login page or any other page you prefer
//                                            saveUserDetailsToServer(user.getUid(), name, username, age);
//                                            Intent intent = new Intent(SignUp.this, Login.class);
//                                            startActivity(intent);
//                                            finish();
//                                        } else {
//                                            Toast.makeText(SignUp.this, "Failed to send verification email.", Toast.LENGTH_SHORT).show();
//                                        }
//                                    });
//                        }
//                    } else {
//                        dialog.dismiss();
//                        Toast.makeText(SignUp.this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }
//
//    private void saveUserDetailsToServer(String firebaseUID, String name, String username, String age) {
//        String url = "http://192.168.10.8/dyslexiadetectfyp/register_user.php";
//
//        RequestQueue queue = Volley.newRequestQueue(this);
//        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
//                response -> {
//                    try {
//                        // Parse the JSON response
//                        JSONObject jsonResponse = new JSONObject(response);
//                        boolean success = jsonResponse.optBoolean("success");
//                        String message = jsonResponse.optString("message");
//
//                        if (success) {
//                            // Handle success
//                            Toast.makeText(SignUp.this, "User registered successfully", Toast.LENGTH_SHORT).show();
//                        } else {
//                            // Handle failure
//                            Toast.makeText(SignUp.this, "Registration failed: " + message, Toast.LENGTH_SHORT).show();
//                        }
//                    } catch (JSONException e) {
//                        // Handle JSON parsing error
//                        Toast.makeText(SignUp.this, "Error parsing response: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                    }
//                },
//                error -> {
//                    // Handle network error
//                    Toast.makeText(SignUp.this, "Network error: " + error.toString(), Toast.LENGTH_SHORT).show();
//                }
//        ) {
//            @Override
//            protected Map<String, String> getParams() {
//                Map<String, String> params = new HashMap<>();
//                params.put("firebase_uid", firebaseUID);
//                params.put("name", name);
//                params.put("username", username);
//                params.put("age", age);
//                return params;
//            }
//        };
//        queue.add(postRequest);
//    }
//
//}

package com.example.frontend;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;

public class SignUp extends AppCompatActivity {

    private static final String TAG = "SignUpActivity";
    EditText editTextEmail, editTextName, editTextUsername, editTextAge, editTextPassword;
    TextView textViewLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        Log.d(TAG, "onCreate: Activity started");

        editTextEmail = findViewById(R.id.eTEmail);
        editTextName = findViewById(R.id.eTName);
        editTextUsername = findViewById(R.id.eTUsername);
        editTextAge = findViewById(R.id.eTAge);
        editTextPassword = findViewById(R.id.eTPassword);
        textViewLogin = findViewById(R.id.goTologin);
        Log.d(TAG, "onCreate: Views initialized");

        Button signUpButton = findViewById(R.id.signUpButton);
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editTextEmail.getText().toString();
                String name = editTextName.getText().toString();
                String username = editTextUsername.getText().toString();
                int age = Integer.parseInt(editTextAge.getText().toString());
                String password = editTextPassword.getText().toString();
                Log.d(TAG, "onClick: Button clicked with email: " + email);

                sendSignUpRequest(email, name, username, age, password);
            }
        });

        textViewLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUp.this, SignIn.class);
                startActivity(intent);
            }
        });
    }

    private void sendSignUpRequest(String email, String name, String username, int age, String password) {
        Log.d(TAG, "sendSignUpRequest: Preparing to send request");
        String url = "http://192.168.10.4:5000/signup";
        RequestQueue queue = Volley.newRequestQueue(this);

        JSONObject postData = new JSONObject();
        try {
            postData.put("email", email);
            postData.put("name", name);
            postData.put("username", username);
            postData.put("age", age);
            postData.put("password", password);
            Log.d(TAG, "sendSignUpRequest: JSON Object created");
        } catch (JSONException e) {
            Log.e(TAG, "sendSignUpRequest: JSON Exception", e);
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, postData,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "onResponse: Received response from server");
                        Toast.makeText(SignUp.this, "Signup Successful!", Toast.LENGTH_LONG).show();

                        saveUserDetails(email, name, username, age);

                        Intent intent = new Intent(SignUp.this, Otp.class);
                        intent.putExtra("UserEmail", email);
                        startActivity(intent);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "onErrorResponse: Error during signup", error);
                Toast.makeText(SignUp.this, "Error during signup: " + error.toString(), Toast.LENGTH_LONG).show();
            }
        });

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000,  // 10 seconds timeout
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        Log.d(TAG, "sendSignUpRequest: Request added to queue");
        queue.add(jsonObjectRequest);
    }

    public void saveUserDetails(String email, String name, String username, int age) {
        SharedPreferences sharedPreferences = getSharedPreferences("UserDetails", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("UserEmail", email);
        editor.putString("UserName", name);
        editor.putString("UserUsername", username);
        editor.putInt("UserAge", age);

        editor.apply();
        Log.d(TAG, "User details saved in Shared Preferences");
    }

}
