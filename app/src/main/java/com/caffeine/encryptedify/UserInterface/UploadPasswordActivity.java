package com.caffeine.encryptedify.UserInterface;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.caffeine.encryptedify.Model.AccountDetails;
import com.caffeine.encryptedify.R;
import com.github.ybq.android.spinkit.sprite.Sprite;
import com.github.ybq.android.spinkit.style.Wave;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Random;

public class UploadPasswordActivity extends AppCompatActivity {

    private EditText name, email, password;
    private TextView save;
    private LinearLayout update, decrypt;
    private ImageView delete;
    private String Name, Email, Password;
    private String RId, RName, REmail, RPassword;
    private String a = "Save", b = "Update", Random;
    private ProgressBar progressBar;
    private Dialog dialog;

    private DatabaseReference ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_password);

        Intent intent = getIntent();
        RId = intent.getStringExtra("id");
        RName = intent.getStringExtra("name");
        REmail = intent.getStringExtra("email");
        RPassword = intent.getStringExtra("password");

        name = findViewById(R.id.et_name);
        email = findViewById(R.id.et_email);
        password = findViewById(R.id.et_password);
        save = findViewById(R.id.tv_save);
        decrypt = findViewById(R.id.decrypt);
        delete = findViewById(R.id.delete);
        update = findViewById(R.id.update_info);
        progressBar = findViewById(R.id.loading_bar);
        Sprite wave = new Wave();
        progressBar.setIndeterminateDrawable(wave);

        ref = FirebaseDatabase.getInstance().getReference().child("Encryptedify").child("Passwords").child(FirebaseAuth.getInstance().getUid());
        dialog = new Dialog(this);

        receivedTask();

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initialize();
                if (validate()){
                    progressBar.setVisibility(View.VISIBLE);
                    updateAccountInfo();
                }
            }
        });

        decrypt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(UploadPasswordActivity.this, EncryptionActivity.class);
                i.putExtra("password", password.getText().toString());
                startActivity(i);
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                ref.child(RId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            showPopupDialog(
                                    "Deleted Successfully",
                                    "The following account has been successfully deleted",
                                    "Close"
                            );
                            delete.setVisibility(View.GONE);
                            decrypt.setVisibility(View.GONE);
                            progressBar.setVisibility(View.GONE);
                            name.setText("");
                            email.setText("");
                            password.setText("");
                            save.setText(a);
                        }
                    }
                });
            }
        });
    }

    private void receivedTask(){
        if (RId != null){
            save.setText(b);
            decrypt.setVisibility(View.VISIBLE);
            delete.setVisibility(View.VISIBLE);
            name.setText(RName);
            email.setText(REmail);
            password.setText(RPassword);
        }
    }

    private void initialize(){
        Name = name.getText().toString();
        Email = email.getText().toString();
        Password = password.getText().toString();
    }

    private boolean validate(){
        boolean v = true;

        if (Name.isEmpty()){
            name.setError("Field cannot be empty");
            name.requestFocus();
            v = false;
        }

        else if (Email.isEmpty()){
            email.setError("Field cannot be empty");
            email.requestFocus();
            v = false;
        }

        else if (Password.isEmpty()){
            password.setError("Field cannot be empty");
            password.requestFocus();
            v = false;
        }

        return v;
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

    private void getSaltString() {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 18) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        Random = salt.toString();
    }

    private void updateAccountInfo(){
        getSaltString();
        AccountDetails acDetails = new AccountDetails(save.getText().toString().equals(b) ? RId : Random, Name, Email, Password);

        ref.child(save.getText().toString().equals(b) ? RId : Random).setValue(acDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){

                    showPopupDialog(
                            save.getText().toString().equals(a) ? "Account Added" : "Successfully Updated",
                            save.getText().toString().equals(a) ? "The account has been added successfully. For any interaction go to home"
                            : "The account has been updated successfully. For any interaction go to home",
                            "Close"
                    );

                    progressBar.setVisibility(View.GONE);
                }
                else {
                    showPopupDialog(
                            "Error Occurred",
                            "An error occurred while adding new account. Please try again later",
                            "Close"
                    );

                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(UploadPasswordActivity.this, HomeActivity.class));
        finish();
    }
}