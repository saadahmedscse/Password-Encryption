package com.caffeine.encryptedify.Signing;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.caffeine.encryptedify.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText email;
    private TextView submit;
    private ProgressBar progressBar;
    private String Email;
    private Dialog dialog;
    public static final String EMAIL_PATTERN = "^([\\w-\\.]+){1,64}@([\\w&&[^_]]+){2,255}.[a-z]{2,}$";
    private String txt = "Send Verification Link";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        email = findViewById(R.id.et_email);
        progressBar = findViewById(R.id.progress_bar);
        submit = findViewById(R.id.btn_send_reset_pass_link);
        dialog = new Dialog(this);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Email = email.getText().toString();

                if (Email.isEmpty()){
                    email.setError("Field cannot be empty");
                    email.requestFocus();
                }
                else if (!Email.matches(EMAIL_PATTERN)){
                    email.setError("Invalid email address");
                    email.requestFocus();
                }
                else {
                    submit.setText("");
                    progressBar.setVisibility(View.VISIBLE);
                    sendLinkToEmail();
                }
            }
        });
    }

    private void showPopupDialog(String title, String message, String action){
        dialog.setContentView(R.layout.invalid_credential_popup);
        dialog.setCancelable(false);

        TextView Title, Message, Action;
        Title = dialog.findViewById(R.id.popup_title);
        Message = dialog.findViewById(R.id.popup_message);
        Action = dialog.findViewById(R.id.popup_action);

        Title.setText(title);
        Message.setText(message);
        Action.setText(action);

        Action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    private void sendLinkToEmail(){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Encryptedify").child("Users");

        ref.orderByChild("email").equalTo(Email)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            FirebaseAuth.getInstance().sendPasswordResetEmail(Email).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        showPopupDialog(
                                                "Check Email",
                                                "A password reset link has been sent to your email address. Click on the link and reset your password",
                                                "Close"
                                        );
                                        progressBar.setVisibility(View.GONE);
                                        submit.setText(txt);
                                    }

                                    else{
                                        showPopupDialog(
                                                "Error Occurred",
                                                "An error occurred while sending your password reset email. Please try again later",
                                                "Close"
                                        );
                                        progressBar.setVisibility(View.GONE);
                                        submit.setText(txt);
                                    }
                                }
                            });
                        }
                        else {
                            showPopupDialog(
                                    "Error Occurred",
                                    "The email you entered has not registered yet. If you don't have an account, Sign up",
                                    "Close"
                            );
                            progressBar.setVisibility(View.GONE);
                            submit.setText(txt);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}