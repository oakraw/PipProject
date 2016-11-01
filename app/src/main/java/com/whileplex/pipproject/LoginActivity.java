package com.whileplex.pipproject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

public class LoginActivity extends AppCompatActivity {

    private Button loginBtn;
    private EditText username;
    private EditText password;
    private Firebase.AuthStateListener authStateListener = new Firebase.AuthStateListener() {
        @Override
        public void onAuthStateChanged(AuthData authData) {
            if (authData != null) {
                AppController.uid = authData.getUid();
                startMainActivity();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        AppController.getFirebase().addAuthStateListener(authStateListener);

        loginBtn = (Button)findViewById(R.id.login_btn);
        username = (EditText)findViewById(R.id.username);
        password = (EditText)findViewById(R.id.password);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logIn(username.getText().toString(), password.getText().toString());
            }
        });
    }

    public void logIn(String username, String password) {
        if (!username.equals("") && !password.equals("")) {
            final ProgressDialog progressDialog = ProgressDialog.show(LoginActivity.this ,null,"Logging in...", true);
            AppController.getFirebase().authWithPassword(username, password, new Firebase.AuthResultHandler() {
                @Override
                public void onAuthenticated(AuthData authData) {
                    System.out.println("User ID: " + authData.getUid() + ", Provider: " + authData.getProvider());
                    AppController.uid = authData.getUid();
                    StoreData.getInstance(getApplicationContext()).setUid(authData.getUid());
                    startMainActivity();
                    progressDialog.dismiss();
                }

                @Override
                public void onAuthenticationError(FirebaseError firebaseError) {
                    Toast.makeText(getApplicationContext(), firebaseError.getMessage() + "", Toast.LENGTH_LONG).show();
                    progressDialog.dismiss();
                }
            });
        }
    }

    private void startMainActivity(){
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppController.getFirebase().removeAuthStateListener(authStateListener);
    }
}
