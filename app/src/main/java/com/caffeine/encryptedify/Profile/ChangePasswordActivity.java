package com.caffeine.encryptedify.Profile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.caffeine.encryptedify.Model.UserDetails;
import com.caffeine.encryptedify.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText oldPass, newPass, confirmPass;
    private ImageView toggleOld, toggleNew, toggleConfirm;
    private TextView btnChangePassword;
    private ProgressBar progressBar;
    private String OldPass, NewPass, ConfirmPass, a = "Change Password";
    private int counter = 0;

    private DatabaseReference ref;
    private UserDetails userDetails;
    private String Email, Password;
    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        gettingLayoutIDs();
        getAccountInformation();
        toggleListener();

        btnChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initialize();
                if (validate()){
                    //changePassword
                    btnChangePassword.setText("");
                    progressBar.setVisibility(View.VISIBLE);

                    if (OldPass.equals(Password)){
                        changePassword();
                    }

                    else {
                        oldPass.setError("Incorrect old password");
                        oldPass.requestFocus();
                        btnChangePassword.setText(a);
                        progressBar.setVisibility(View.GONE);
                    }
                }
            }
        });
    }

    private void gettingLayoutIDs(){
        ref = FirebaseDatabase.getInstance().getReference().child("Encryptedify").child("Users").child(FirebaseAuth.getInstance().getUid()).child("password");
        dialog = new Dialog(this);

        oldPass = findViewById(R.id.et_old_password);
        newPass = findViewById(R.id.et_new_password);
        confirmPass = findViewById(R.id.et_confirm_password);
        toggleOld = findViewById(R.id.pass_visibility_old);
        toggleNew = findViewById(R.id.pass_visibility_new);
        toggleConfirm = findViewById(R.id.pass_visibility_confirm);
        btnChangePassword = findViewById(R.id.btn_change_pass);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void toggleActions(EditText et, ImageView visibility){
        if (counter == 0){
            visibility.setImageResource(R.drawable.visible);
            et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            counter++;
        }

        else{
            visibility.setImageResource(R.drawable.invisible);
            et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            counter--;
        }
    }

    private void toggleListener(){
        toggleOld.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleActions(oldPass, toggleOld);
            }
        });

        toggleNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleActions(newPass, toggleNew);
            }
        });

        toggleConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleActions(confirmPass, toggleConfirm);
            }
        });
    }

    private void initialize(){
        OldPass = oldPass.getText().toString();
        NewPass = newPass.getText().toString();
        ConfirmPass = confirmPass.getText().toString();
    }

    private boolean validate(){
        boolean valid = true;

        if (OldPass.isEmpty()){
            oldPass.setError("Filed cannot be empty");
            oldPass.requestFocus();
            valid = false;
        }

        else if (NewPass.isEmpty()){
            newPass.setError("Filed cannot be empty");
            newPass.requestFocus();
            valid = false;
        }

        else if (NewPass.length() < 8 || NewPass.length() > 20){
            newPass.setError("Password must contain 8 to 20 characters");
            newPass.requestFocus();
            valid = false;
        }

        else if (ConfirmPass.isEmpty()){
            confirmPass.setError("Filed cannot be empty");
            confirmPass.requestFocus();
            valid = false;
        }

        else if (!ConfirmPass.equals(NewPass)){
            confirmPass.setError("Password did not match");
            confirmPass.requestFocus();
            valid = false;
        }

        return valid;
    }

    private void getAccountInformation(){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Encryptedify").child("Users").child(FirebaseAuth.getInstance().getUid());
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userDetails = snapshot.getValue(UserDetails.class);
                Email = userDetails.getEmail();
                Password = userDetails.getPassword();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

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

    private void changePassword(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        AuthCredential credential = EmailAuthProvider.getCredential(Email, Password);

        assert user != null;
        user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    user.updatePassword(NewPass).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                ref.setValue(NewPass).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                            showPopupDialog(
                                                    "Password Changed",
                                                    "Password changed successfully. Now you can login with your new password.",
                                                    "Close"
                                            );

                                            progressBar.setVisibility(View.GONE);
                                            btnChangePassword.setText(a);
                                        }

                                        else {
                                            showPopupDialog(
                                                    "Error Occurred",
                                                    "An error occurred while changing your password. Password did not changed.",
                                                    "Close"
                                            );

                                            progressBar.setVisibility(View.GONE);
                                            btnChangePassword.setText(a);
                                        }
                                    }
                                });
                            }
                        }
                    });
                }

                else {
                    showPopupDialog(
                            "Authentication Failed",
                            "An error occurred while changing your password. Password did not changed.",
                            "Close"
                    );

                    progressBar.setVisibility(View.GONE);
                    btnChangePassword.setText(a);
                }
            }
        });
    }
}