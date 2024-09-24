package com.example.jots;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Login extends AppCompatActivity {

    EditText email_txt, password_txt;
    Button loginBtn;
    ProgressBar progressBar;
    TextView createAccountBtntxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ConstraintLayout constraintLayout = findViewById(R.id.main);

        AnimationDrawable animationDrawable = (AnimationDrawable) constraintLayout.getBackground();
        animationDrawable.setEnterFadeDuration(2000);
        animationDrawable.setExitFadeDuration(4000);
        animationDrawable.start();

        email_txt = findViewById(R.id.email_txtEdit);
        password_txt = findViewById(R.id.pswd_txtEdit);
        loginBtn = findViewById(R.id.Login_btn);
        progressBar = findViewById(R.id.progress);
        createAccountBtntxt = findViewById(R.id.Register_btn);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginAccountfunc();
            }
        });

        createAccountBtntxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Login.this, CreateAccount.class));
            }
        });

    }

    void progress(boolean inProgress){
        if(inProgress){
            progressBar.setVisibility(View.VISIBLE);
            loginBtn.setVisibility(View.GONE);
        }else{
            progressBar.setVisibility(View.GONE);
            loginBtn.setVisibility(View.VISIBLE);
        }
    }
    boolean validateData(String email, String pswd){
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            email_txt.setError("Invalid Email !");
            return false;
        }

        if (pswd.length()<6){
            password_txt.setError("Enter a password more than 6 characters.");
            return false;
        }

        return true;
    }

    void loginAccountfunc(){
        String email = email_txt.getText().toString();
        String pswd = password_txt.getText().toString();

        boolean response = validateData(email, pswd);

        if(!response){
            return;
        }

        loginAccount(email, pswd);
    }

    void loginAccount(String email, String pswd){
        FirebaseAuth firebaseauth = FirebaseAuth.getInstance();
        progress(true);
        firebaseauth.signInWithEmailAndPassword(email, pswd).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                progress(false);
                if(task.isSuccessful()){
                    if(firebaseauth.getCurrentUser().isEmailVerified()){
                        startActivity(new Intent(Login.this, MainActivity.class));
                    }
                    else {
                        Toast.makeText(Login.this, "Email is not Verified !", Toast.LENGTH_LONG).show();
                    }
                }else {
                    Toast.makeText(Login.this, task.getException().getLocalizedMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

    }
}