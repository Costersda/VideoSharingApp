package com.example.videosharingapp;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;


public class LoginActivity extends AppCompatActivity {


    SignInButton signInButton;
    FirebaseAuth auth;
    FirebaseDatabase database;
    GoogleSignInClient mGoogleSignInClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        // Check if the user is already signed in
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            // User is already signed in, navigate to MainActivity
            startMainActivity();
        } else {
            // User is not signed in, set up sign-in button
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.web_client_id))
                    .requestEmail()
                    .build();

            mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

            signInButton = findViewById(R.id.sign_in_button);
            signInButton.setSize(SignInButton.SIZE_STANDARD);

            signInButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    signIn();
                }
            });
        }
    }

    int RC_SIGN_IN = 40;

    void signIn(){
        Intent intent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(intent,RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuth(account.getIdToken());
            } catch (ApiException e){
                throw new RuntimeException(e);
            }
        }

    }

    private void firebaseAuth(String idToken) {
        // Record the start time
        long startTime = System.currentTimeMillis();

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // Record the end time when the task completes
                        long endTime = System.currentTimeMillis();

                        if (task.isSuccessful()) {
                            FirebaseUser user = auth.getCurrentUser();
                            Users users = new Users();
                            users.setUserID(user.getUid());
                            users.setName(user.getDisplayName());
                            users.setProfile(user.getPhotoUrl().toString());

                            database.getReference().child("Users").child(user.getUid()).setValue(users);

                            // Calculate and log the time taken
                            long timeTaken = endTime - startTime;
                            Log.d("FirebaseAuthTime", "Time taken: " + timeTaken + " milliseconds");

                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this, "error", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void startMainActivity() {
        finish(); // Finish the LoginActivity so the user can't go back to it after login
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);

    }

}

// This code uses best practices for optimisation in several ways:
// The code starts by checking if the user is already signed in (FirebaseUser currentUser = auth.getCurrentUser();).
// This is a good practice to quickly skip the sign-in process if the user is already authenticated.
// The code correctly handles asynchronous operations, such as sign-in and Firebase authentication,
// using callbacks like OnCompleteListener. This is essential to avoid blocking the main UI thread.

// Factors that can affect the performance of data access in the cloud:
// Network Speed: The speed and stability of the user's network connection can significantly impact data access performance.
// Data Volume: The amount of data retrieved can impact performance. Minimize data transfer by requesting only what's necessary.
// Local Caching: Effective use of local caching can reduce the need for frequent network requests.
// Background Tasks: Ensure that background tasks don't consume excessive resources or interfere with the user experience.

// It took 1258 milliseconds between the request being made and the response