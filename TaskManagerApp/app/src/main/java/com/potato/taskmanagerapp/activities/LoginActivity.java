package com.potato.taskmanagerapp.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.potato.taskmanagerapp.R;
import com.rey.material.widget.Button;
import com.rey.material.widget.CheckBox;
import com.rey.material.widget.EditText;

import java.util.Arrays;

/**
 * Created by Razvan on 13.08.2015.
 */
public class LoginActivity extends Activity {
    EditText user, password;
    TextView forgotPassword, register;
    Button facebookLogin, login;
    CheckBox remember;
    private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences preferences = getSharedPreferences("com.potato.TaskManagerApp_preferences", MODE_PRIVATE);
        if(preferences.getString("user",null)!=null){
            Intent intent = new Intent(LoginActivity.this,SplashActivity.class);
            startActivity(intent);
            finish();
        }
        FacebookSdk.sdkInitialize(LoginActivity.this);

        callbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Log.e("fb:", loginResult.getAccessToken().getUserId());

                        SharedPreferences preferences = getSharedPreferences("com.potato.TaskManagerApp_preferences", MODE_PRIVATE);
                        preferences.edit().putString("user", loginResult.getAccessToken().getUserId()).commit();
                        Intent intent = new Intent(LoginActivity.this, SplashActivity.class);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onCancel() {
                        Toast.makeText(LoginActivity.this, "Login Cancel", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Toast.makeText(LoginActivity.this, exception.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
        setContentView(R.layout.login_activity);

        user = (EditText) findViewById(R.id.user);
        password = (EditText) findViewById(R.id.password);

        forgotPassword = (TextView) findViewById(R.id.forgot_password);
        register = (TextView) findViewById(R.id.register);

        facebookLogin = (Button) findViewById(R.id.facebook_login);
        login = (Button) findViewById(R.id.login);

        remember = (CheckBox) findViewById(R.id.remember);

//        user.setFocusable(false);
//        password.setFocusable(false);


        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(registerIntent);
            }
        });

        password.clearError();
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginIntent = new Intent(LoginActivity.this, SplashActivity.class);
                startActivity(loginIntent);
                finish();
//                password.clearFocus();
//                if (password.getText().toString().equals("")) {
//                    password.setError("Wrong password!");
//                } else {
//                    password.setError(null);
//                    if(remember.isChecked()){
//                        SharedPreferences preferences = getSharedPreferences("com.potato.TaskManagerApp_preferences",MODE_PRIVATE);
//                        preferences.edit().putString("user",user.getText().toString()).commit();
//                    }
//                    Intent loginIntent = new Intent(LoginActivity.this, SplashActivity.class);
//                    startActivity(loginIntent);
//                    finish();
//                }
            }
        });
        password.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    password.setError(null);
            }
        });
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(LoginActivity.this);

                // Setting Dialog Title
                alertDialog.setTitle("Forgot password...");

                // Setting Dialog Message
                alertDialog.setMessage("Enter email address: ");
                final EditText input = new EditText(LoginActivity.this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                input.setLayoutParams(lp);
                input.setPadding(16, 8, 16, 8);
                input.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                input.setImeOptions(EditorInfo.IME_ACTION_DONE);
                input.setSingleLine();


                alertDialog.setView(input);

                // Setting Positive "Yes" Button
                alertDialog.setPositiveButton("Send", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        String email = input.getText().toString();
                        if (SignUpActivity.isEmailValid(email)) {
                            Toast.makeText(LoginActivity.this, "Your account information has been sent to your email address:" + email, Toast.LENGTH_LONG).show();

                        } else {
                            Toast.makeText(LoginActivity.this,"You have entered an invalid email address, dumbass!",Toast.LENGTH_LONG).show();
                        }
                    }
                });

                // Setting Negative "NO" Button
                alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Write your code here to invoke NO event
                        dialog.cancel();
                    }
                });

                // Showing Alert Message
                alertDialog.show();

            }
        });
        facebookLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(getApplicationContext(),"Facebook",Toast.LENGTH_SHORT).show();
                LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this, Arrays.asList("public_profile"));

            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        user.getText().clear();
        password.getText().clear();
        remember.setChecked(false);
        user.clearFocus();
        password.clearFocus();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
