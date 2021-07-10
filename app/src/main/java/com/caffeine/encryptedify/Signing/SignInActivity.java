package com.caffeine.encryptedify.Signing;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.caffeine.encryptedify.R;
import com.caffeine.encryptedify.UserInterface.HomeActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignInActivity extends AppCompatActivity {

    private TextView signIn, signUp, forgotPassword;
    private ProgressBar progressBar;
    private EditText email, password;
    private String EMAIL, PASSWORD;
    public static final String EMAIL_PATTERN = "^([\\w-\\.]+){1,64}@([\\w&&[^_]]+){2,255}.[a-z]{2,}$";
    private ImageView togglePasswordVisibility;
    private Dialog invalidCredentialDialog;
    private int visibility = 0;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        performAutoLogin();
        gettingLayoutIDs();
        clickListeners();
    }

    private void gettingLayoutIDs(){
        mAuth = FirebaseAuth.getInstance();
        invalidCredentialDialog = new Dialog(this);

        signIn = findViewById(R.id.btn_sign_in);
        signUp = findViewById(R.id.btn_sign_up);
        forgotPassword = findViewById(R.id.tv_forgot_password);
        email = findViewById(R.id.et_email);
        password = findViewById(R.id.et_password);
        togglePasswordVisibility = findViewById(R.id.pass_visibility);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void initialize(){
        EMAIL = email.getText().toString();
        PASSWORD = password.getText().toString();
    }

    private boolean validate(){
        boolean valid = true;

        if (EMAIL.isEmpty()){
            email.setError("Field cannot be empty");
            email.requestFocus();
            valid = false;
        }

        else if (!EMAIL.matches(EMAIL_PATTERN)){
            email.setError("Invalid email address");
            email.requestFocus();
            valid = false;
        }

        else if (PASSWORD.isEmpty()){
            password.setError("Field cannot be empty");
            password.requestFocus();
            valid = false;
        }

        return valid;
    }

    private void intentToActivity(Class c){
        /*
          User has to pass a Class as a parameter to navigate
          Use finish(); method if needed
         */
        startActivity(new Intent(SignInActivity.this, c));
    }

    private void showInvalidCredentialPopup(){
        invalidCredentialDialog.setContentView(R.layout.invalid_credential_popup);
        invalidCredentialDialog.setCancelable(true);

        String pTitle = "Login Failed";
        String pMessage = "The email or password you entered is invalid. If you forgot your password you can recover it.";
        String pAction = "Close";

        TextView title = invalidCredentialDialog.findViewById(R.id.popup_title);
        TextView message = invalidCredentialDialog.findViewById(R.id.popup_message);
        TextView close = invalidCredentialDialog.findViewById(R.id.popup_action);

        title.setText(pTitle);
        message.setText(pMessage);
        close.setText(pAction);

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                invalidCredentialDialog.dismiss();
            }
        });

        invalidCredentialDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        invalidCredentialDialog.show();
    }

    private void performAutoLogin(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mAuth.getCurrentUser() != null){
                    intentToActivity(HomeActivity.class);
                    finish();
                }
                else {
                    progressBar.setVisibility(View.GONE);
                    String S = "Sign in";
                    signIn.setText(S);
                }
            }
        },2000);
    }

    private void SignInUserWithEmailAndPassword(){
        mAuth.signInWithEmailAndPassword(EMAIL, PASSWORD)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            intentToActivity(HomeActivity.class);
                            finish();
                        }
                        else {
                            showInvalidCredentialPopup();
                            progressBar.setVisibility(View.GONE);
                            String S = "Sign in";
                            signIn.setText(S);
                        }
                    }
                });
    }

    private void clickListeners(){
        togglePasswordVisibility.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (visibility == 0){
                    togglePasswordVisibility.setImageResource(R.drawable.visible);
                    password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    visibility++;
                }
                else {
                    togglePasswordVisibility.setImageResource(R.drawable.invisible);
                    password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    visibility--;
                }
            }
        });

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initialize();
                if (validate()){
                    signIn.setText("");
                    progressBar.setVisibility(View.VISIBLE);
                    SignInUserWithEmailAndPassword();
                }
            }
        });

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intentToActivity(SignUpActivity.class);
                finish();
            }
        });

        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //If user forget their password
                intentToActivity(ForgotPasswordActivity.class);
            }
        });
    }
}