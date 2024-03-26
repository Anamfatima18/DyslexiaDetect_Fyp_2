//package com.mehreenishtiaq.dyslexiadetectfyp;
//
//import android.app.ProgressDialog;
//import android.content.Intent;
//import android.content.SharedPreferences;
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
//import java.util.HashMap;
//import java.util.Map;
//
//public class Login extends AppCompatActivity {
//
//    private EditText eTEmail, eTPassword;
//    private Button loginButton;
//    private TextView goToRegistration;
//    private ProgressDialog dialog;
//
//    private FirebaseAuth mAuth;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_login);
//
//        mAuth = FirebaseAuth.getInstance();
//
//        dialog = new ProgressDialog(Login.this);
//        dialog.setCancelable(false);
//
//        eTEmail = findViewById(R.id.eTEmailAddress);
//        eTPassword = findViewById(R.id.eTPassword);
//        loginButton = findViewById(R.id.loginButton);
//        goToRegistration = findViewById(R.id.signUp);
//
//        loginButton.setOnClickListener(view -> {
//            String email = eTEmail.getText().toString().trim();
//            String password = eTPassword.getText().toString().trim();
//
//            if (!email.isEmpty() && !password.isEmpty()) {
//                userLogin(email, password);
//            } else {
//                if (email.isEmpty()) {
//                    eTEmail.setError("Required");
//                }
//                if (password.isEmpty()) {
//                    eTPassword.setError("Required");
//                }
//            }
//        });
//
//        goToRegistration.setOnClickListener(view -> startActivity(new Intent(Login.this, SignUp.class)));
//    }
//
//    private void userLogin(String email, String password) {
//        dialog.setTitle("Logging In");
//        dialog.setMessage("Please wait while we log you in");
//        dialog.show();
//
//        mAuth.signInWithEmailAndPassword(email, password)
//                .addOnCompleteListener(this, task -> {
//                    if (task.isSuccessful()) {
//                        FirebaseUser user = mAuth.getCurrentUser();
//                        if (user != null && user.isEmailVerified()) {
//                            saveUserId(user.getUid());
//                            //triggerNotificationsForUser(user.getUid());
//                            //navigateToNextActivity();
//                            Toast.makeText(Login.this, "Login successful", Toast.LENGTH_SHORT).show();
//                            Intent intent = new Intent(Login.this, SignUp.class);
//                            startActivity(intent);
//                        } else {
//                            dialog.dismiss();
//                            Toast.makeText(Login.this, "Please verify your email address.", Toast.LENGTH_SHORT).show();
//                        }
//                    } else {
//                        dialog.dismiss();
//                        Toast.makeText(Login.this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }
//
//    // New method to trigger notifications
////    private void triggerNotificationsForUser(String firebaseUID) {
////        String url = "http://172.16.49.144/smdProjectt/trigger_notifications.php";
////
////        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
////                response -> Log.d("NotificationsTrigger", "Notifications triggered for UID: " + firebaseUID),
////                error -> Log.e("NotificationsTrigger", "Error: " + error.toString())
////        ) {
////            @Override
////            protected Map<String, String> getParams() {
////                Map<String, String> params = new HashMap<>();
////                params.put("firebase_uid", firebaseUID);
////                return params;
////            }
////        };
////        RequestQueue queue = Volley.newRequestQueue(this);
////        queue.add(postRequest);
////    }
//
////    private void navigateToNextActivity() {
////        Intent intent = new Intent(Login.this, PeerToPeerActivity.class);
////        startActivity(intent);
////        finish();
////    }
//
//    private void saveUserId(String userId) {
//        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putString("user_id", userId);
//        editor.apply();
//    }
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
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;

public class SignIn extends AppCompatActivity {
    private static final String TAG = "SignInActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        EditText editTextEmail = findViewById(R.id.eTEmailAddress);
        EditText editTextPassword = findViewById(R.id.eTPassword);
        Button loginButton = findViewById(R.id.loginButton);
        TextView goToRegistration = findViewById(R.id.signUp);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = editTextEmail.getText().toString();
                String password = editTextPassword.getText().toString();
                Log.d(TAG, "Login button clicked with email: " + email);
                signIn(email, password);
            }
        });

        goToRegistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignIn.this, SignUp.class);
                startActivity(intent);
            }
        });
    }
    private void saveToken(String token) {
        // Store the token in SharedPreferences or other storage mechanism
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("token", token);
        editor.apply();
    }
    private void signIn(String email, String password) {
        Log.d(TAG, "Preparing to sign in with email: " + email);
        String url = "http://192.168.10.4:5000/signin"; // Replace with your server URL
        RequestQueue queue = Volley.newRequestQueue(this);

        JSONObject postData = new JSONObject();
        try {
            postData.put("email", email);
            postData.put("password", password);
            Log.d(TAG, "JSON Object for sign in created");
        } catch (JSONException e) {
            Log.e(TAG, "JSON Exception in signIn", e);
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, postData,
                response -> {
                    String token = null;
                    try {
                        token = response.getString("token");
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    Log.d("SignIn", "Sign in successful. Token: " + token);

                    // Save the token for future requests
                    saveToken(token);
                    Log.d(TAG, "Sign in successful: " + response.toString());
                    // For example, navigate to the next screen
                    Intent intent = new Intent(SignIn.this, StartScreening.class);
                    startActivity(intent);
                }, error -> {
                    Log.e(TAG, "Error during sign in", error);
                    // Handle error
                    // For example, show an error message
                    Toast.makeText(SignIn.this, "Error during sign in", Toast.LENGTH_LONG).show();
                });

        Log.d(TAG, "Sign in request added to queue");
        queue.add(jsonObjectRequest);
    }

}
