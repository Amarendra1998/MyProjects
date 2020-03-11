package com.example.helmet40;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import com.facebook.login.widget.LoginButton;

public class FacebookLogin extends AppCompatActivity {
    private LoginButton loginButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facebook_login);
    }
}
