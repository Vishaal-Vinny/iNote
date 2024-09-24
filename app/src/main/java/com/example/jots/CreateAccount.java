package com.example.jots;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
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

public class CreateAccount extends AppCompatActivity {

    EditText email_txt, password_txt, confirm_pswd_txt;
    Button createAcountBtn;
    ProgressBar progressBar;
    TextView loginBtnView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        ConstraintLayout constraintLayout = findViewById(R.id.main);

        AnimationDrawable animationDrawable = (AnimationDrawable) constraintLayout.getBackground();
        animationDrawable.setEnterFadeDuration(2000);
        animationDrawable.setExitFadeDuration(4000);
        animationDrawable.start();

        email_txt = findViewById(R.id.email_txtEdit);
        password_txt = findViewById(R.id.pswd_txtEdit);
        confirm_pswd_txt = findViewById(R.id.confirm_pswd_txtEdit);
        createAcountBtn = findViewById(R.id.create_account_btn);
        progressBar = findViewById(R.id.progress);
        loginBtnView = findViewById(R.id.login_btn);

        createAcountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAccount();
            }
        });

        loginBtnView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CreateAccount.this, Login.class));
            }
        });
    }

    void createAccount(){
        String email = email_txt.getText().toString();
        String pswd = password_txt.getText().toString();
        String confirm_pswd = confirm_pswd_txt.getText().toString();

        boolean response = validateData(email, pswd, confirm_pswd);

        if(!response){
            return;
        }

        createAccountInFirebase(email, pswd);
    }

    void createAccountInFirebase(String email, String pswd){
         progress(true);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.createUserWithEmailAndPassword(email, pswd).addOnCompleteListener(CreateAccount.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                progress(false);
                if (task.isSuccessful()){
                    Toast.makeText(CreateAccount.this, "Successfully Created, Check Email !", Toast.LENGTH_LONG).show();
                    firebaseAuth.getCurrentUser().sendEmailVerification();
                    firebaseAuth.signOut();
                    finish();
                } else{
                    Toast.makeText(CreateAccount.this, task.getException().getLocalizedMessage(), Toast.LENGTH_LONG).show();

                }
            }
        });

    }

    void progress(boolean inProgress){
        if(inProgress){
            progressBar.setVisibility(View.VISIBLE);
            createAcountBtn.setVisibility(View.GONE);
        }else{
            progressBar.setVisibility(View.GONE);
            createAcountBtn.setVisibility(View.VISIBLE);
        }
    }
    boolean validateData(String email, String pswd, String confirm_pswd){
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            email_txt.setError("Invalid Email !");
            return false;
        }

        if (pswd.length()<6){
            password_txt.setError("Enter a password more than 6 characters.");
            return false;
        }

        if (!pswd.equals(confirm_pswd)){
            confirm_pswd_txt.setError("Incorrect the Password !");
            return false;
        }

        return true;
    }
}