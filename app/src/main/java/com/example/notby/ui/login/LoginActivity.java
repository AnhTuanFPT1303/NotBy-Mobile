package com.example.notby.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.notby.R;
import com.example.notby.data.TokenManager;
import com.example.notby.ui.forumpost.ForumPostActivity;
import com.example.notby.utils.ResponseLogger;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private static final int RC_SIGN_IN = 9001;
    private static final String API_URL = "https://notby-be-8q9y.onrender.com/auth/mobile/google";

    private GoogleSignInClient mGoogleSignInClient;
    private RequestQueue mRequestQueue;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        tokenManager = new TokenManager(this);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("391965880918-9b0jq9cvolrov0lpa36issb2t8edq76s.apps.googleusercontent.com")   //thay clientId vo day
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        SignInButton signInButton = findViewById(R.id.btnLogin);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        mRequestQueue = Volley.newRequestQueue(this);
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            String idToken = account.getIdToken();
            sendIdTokenToServer(idToken);
        } catch (ApiException e) {
            Toast.makeText(this, "Google Sign In failed.", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendIdTokenToServer(String idToken) {
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("idToken", idToken);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, API_URL, jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Log the complete response structure
                        ResponseLogger.logAllFields(TAG, response);

                        try {
                            // Try multiple possible response structures
                            String jwtToken = null;
                            String userId = null;
                            String email = null;
                            String name = null;

                            // Check if response has 'data' wrapper
                            if (response.has("data")) {
                                JSONObject data = response.getJSONObject("data");
                                jwtToken = data.optString("token", null);
                                if (jwtToken == null) jwtToken = data.optString("accessToken", null);

                                if (data.has("user")) {
                                    JSONObject user = data.getJSONObject("user");
                                    userId = user.optString("id", null);
                                    if (userId == null) userId = user.optString("_id", null);
                                    email = user.optString("email", "");
                                    name = user.optString("name", "");
                                } else {
                                    // User data might be directly in data
                                    userId = data.optString("id", null);
                                    if (userId == null) userId = data.optString("_id", null);
                                    email = data.optString("email", "");
                                    name = data.optString("name", "");
                                }
                            } else {
                                // Token might be at root level
                                jwtToken = response.optString("token", null);
                                if (jwtToken == null) jwtToken = response.optString("accessToken", null);

                                if (response.has("user")) {
                                    JSONObject user = response.getJSONObject("user");
                                    userId = user.optString("id", null);
                                    if (userId == null) userId = user.optString("_id", null);
                                    email = user.optString("email", "");
                                    name = user.optString("name", "");
                                } else {
                                    // User data might be at root level
                                    userId = response.optString("id", null);
                                    if (userId == null) userId = response.optString("_id", null);
                                    email = response.optString("email", "");
                                    name = response.optString("name", "");
                                }
                            }

                            if (jwtToken != null && jwtToken.length() > 0) {
                                tokenManager.saveToken(jwtToken);
                            } else {
                                Log.e(TAG, "No token found in response!");
                            }

                            if (userId != null && !userId.isEmpty()) {
                                tokenManager.saveUserData(userId, email, name);
                            } else {
                                Log.e(TAG, "No user ID found in response!");
                            }

                        } catch (JSONException e) {
                            Log.e(TAG, "Error parsing response: " + e.getMessage());
                            e.printStackTrace();
                        }

                        Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();

                        // Proceed to ForumPostActivity
                        Intent intent = new Intent(LoginActivity.this, ForumPostActivity.class);
                        startActivity(intent);
                        finish();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Login error: " + error.toString());
                        Toast.makeText(LoginActivity.this, "Login failed. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });

        mRequestQueue.add(jsonObjectRequest);
    }
}
