package com.caffeine.encryptedify.UserInterface;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.caffeine.encryptedify.Model.UserDetails;
import com.caffeine.encryptedify.Profile.AppInfoActivity;
import com.caffeine.encryptedify.Profile.ChangePasswordActivity;
import com.caffeine.encryptedify.Profile.EditProfileActivity;
import com.caffeine.encryptedify.R;
import com.caffeine.encryptedify.Signing.SignInActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private CircleImageView profilePicture;
    private LinearLayout signOut;
    private RelativeLayout editProfile, changePass, rateApp, appInfo;
    private TextView name, key;
    private ProgressBar progressBar;

    private DatabaseReference reference;
    private UserDetails user;
    private String Key;
    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        gettingLayoutIDs();
        getUserInfo();
        onClickListeners();
    }

    private void gettingLayoutIDs(){
        reference = FirebaseDatabase.getInstance().getReference().child("Encryptedify").child("Users").child(FirebaseAuth.getInstance().getUid());
        dialog = new Dialog(this);

        profilePicture = findViewById(R.id.profile_picture);
        signOut = findViewById(R.id.btn_sign_out);
        name = findViewById(R.id.tv_name);
        key = findViewById(R.id.encryption_key);
        progressBar = findViewById(R.id.progress_bar);
        editProfile = findViewById(R.id.edit_profile);
        changePass = findViewById(R.id.change_pass);
        rateApp = findViewById(R.id.rate_app);
        appInfo = findViewById(R.id.app_info);
    }

    private void intentToActivity(Class c){
        startActivity(new Intent(ProfileActivity.this, c));
    }

    private void onClickListeners(){
        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(ProfileActivity.this, SignInActivity.class));
                finish();
            }
        });

        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Edit Profile
                intentToActivity(EditProfileActivity.class);
            }
        });

        changePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Change Password
                intentToActivity(ChangePasswordActivity.class);
            }
        });

        rateApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Rate this app
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://apkpure.com/p/com.caffeine.encryptedify"));
                startActivity(browserIntent);
            }
        });

        appInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //App info page
                intentToActivity(AppInfoActivity.class);
            }
        });
    }

    private void getUserInfo(){
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user = snapshot.getValue(UserDetails.class);
                name.setText(user.getName());
                Key = user.getKey();
                keyTask();
                Picasso.get().load(user.getUri()).into(profilePicture, new Callback() {
                    @Override
                    public void onSuccess() {
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(Exception e) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void popupDialog(){
        dialog.setContentView(R.layout.encryption_dialog);

        TextView submit, keyZero;
        EditText keyOne, keyTwo;
        ProgressBar progressBar;

        submit = dialog.findViewById(R.id.btn_submit_key);
        keyOne = dialog.findViewById(R.id.et_key_one);
        keyTwo = dialog.findViewById(R.id.et_key_two);
        keyZero = dialog.findViewById(R.id.key_zero);
        progressBar = dialog.findViewById(R.id.progress_bar);

        keyOne.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (keyOne.getText().toString().length() == 1){
                    keyTwo.requestFocus();
                    keyZero.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String KEY = keyOne.getText().toString() + keyTwo.getText().toString();
                int key = 0;

                if (KEY.isEmpty()){
                    keyZero.setVisibility(View.VISIBLE);
                }

                else {
                    key = Integer.parseInt(KEY);

                    if (key < 1){
                        String keyZ = "Encryption Key cannot be 0";
                        keyZero.setText(keyZ);
                        keyZero.setVisibility(View.VISIBLE);
                    }

                    else {
                        submit.setText("");
                        progressBar.setVisibility(View.VISIBLE);
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Encryptedify").child("Users").child(FirebaseAuth.getInstance().getUid());
                        ref.child("key").setValue(KEY).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    dialog.dismiss();
                                }
                                else {
                                    progressBar.setVisibility(View.GONE);
                                    String sub = "Submit";
                                    String failed = "Failed to submit key";
                                    submit.setText(sub);
                                    keyZero.setText(failed);
                                    keyZero.setVisibility(View.VISIBLE);
                                }
                            }
                        });
                    }
                }
            }
        });

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.show();
    }

    private void keyTask(){
        if (Key.equals("blank")){
            popupDialog();
        }
        else {
            key.setText(Key);
            key.setVisibility(View.VISIBLE);
        }
    }
}