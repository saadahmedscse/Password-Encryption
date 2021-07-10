package com.caffeine.encryptedify.Signing;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.caffeine.encryptedify.Model.UserDetails;
import com.caffeine.encryptedify.R;
import com.caffeine.encryptedify.UserInterface.HomeActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

public class SignUpActivity extends AppCompatActivity {

    private CardView placeholder;
    private ProgressBar progressBar;
    private TextView signIn, signUp, require;
    private ImageView profilePicture, togglePassVisibility;
    private EditText name, email, password;
    private String NAME, EMAIL, PASSWORD, UID, URI;
    public static final String EMAIL_PATTERN = "^([\\w-\\.]+){1,64}@([\\w&&[^_]]+){2,255}.[a-z]{2,}$";

    private FirebaseAuth firebaseAuth;
    private DatabaseReference ref;
    private StorageReference storageRef;
    private Uri uri;
    private Dialog dialog;

    private int visibility = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        gettingLayoutIDs();
        clickListeners();
    }

    private void gettingLayoutIDs(){
        firebaseAuth = FirebaseAuth.getInstance();
        ref = FirebaseDatabase.getInstance().getReference().child("Encryptedify").child("Users");
        storageRef = FirebaseStorage.getInstance().getReference().child("Profile Pictures");
        dialog = new Dialog(this);

        placeholder = findViewById(R.id.image_placeholder);
        progressBar = findViewById(R.id.progress_bar);
        signIn = findViewById(R.id.btn_sign_in);
        signUp = findViewById(R.id.btn_sign_up);
        profilePicture = findViewById(R.id.profile_picture);
        togglePassVisibility = findViewById(R.id.pass_visibility);
        name = findViewById(R.id.et_name);
        email = findViewById(R.id.et_email);
        password = findViewById(R.id.et_password);
        require = findViewById(R.id.require);
    }

    private void initialize(){
        NAME = name.getText().toString();
        EMAIL = email.getText().toString();
        PASSWORD = password.getText().toString();
    }

    private boolean validate(){
        boolean valid = true;

        if (uri == null){
            require.setTextColor(getResources().getColor(R.color.colorRed));
            valid = false;
        }

        else if (NAME.isEmpty()){
            name.setError("Field cannot be empty");
            name.requestFocus();
            valid = false;
        }

        if (NAME.length() <3 || NAME.length() >24){
            name.setError("Enter a valid name");
            name.requestFocus();
            valid = false;
        }

        else if (EMAIL.isEmpty()){
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

        else if (PASSWORD.length() < 8 || PASSWORD.length() > 20){
            password.setError("Password must contain 8 to 20 characters");
            password.requestFocus();
            valid = false;
        }

        return valid;
    }

    private void clickListeners(){
        togglePassVisibility.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (visibility == 0){
                    togglePassVisibility.setImageResource(R.drawable.visible);
                    password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    visibility++;
                }
                else {
                    togglePassVisibility.setImageResource(R.drawable.invisible);
                    password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    visibility--;
                }
            }
        });

        placeholder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignUpActivity.this, SignInActivity.class));
                finish();
            }
        });

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initialize();
                if (validate()){
                    signUp.setText("");
                    progressBar.setVisibility(View.VISIBLE);
                    signUpUser();
                }
            }
        });
    }

    private void showFailedDialog(String t, String m, String a){
        dialog.setContentView(R.layout.invalid_credential_popup);

        TextView title, message, action;
        title = dialog.findViewById(R.id.popup_title);
        message = dialog.findViewById(R.id.popup_message);
        action = dialog.findViewById(R.id.popup_action);

        title.setText(t);
        message.setText(m);
        action.setText(a);

        action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    private void signUpUser(){
        firebaseAuth.createUserWithEmailAndPassword(EMAIL, PASSWORD)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            sendDataToFirebase();
                        }
                        else {
                            showFailedDialog("Sign up Failed", "The email you entered has been registered already", "Close");
                            progressBar.setVisibility(View.GONE);
                            String S = "Create new account";
                            signUp.setText(S);
                        }
                    }
                });
    }

    //--------------------------------------------------------------------------------Send Data to Firebase-------------------------------------------------------------------

    private void chooseImage(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null){
            uri = data.getData();
            profilePicture.setImageURI(uri);
            String S = "(Uploaded)";
            require.setText(S);
            require.setTextColor(getResources().getColor(R.color.colorGreen));
        }
    }

    private void sendDataToFirebase(){
        Bitmap bmp = null;

        try {
            bmp = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
        } catch (Exception e) {
            e.printStackTrace();
        }

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 15, baos);
        byte[] fileInBytes = baos.toByteArray();

        final StorageReference photoRef = storageRef.child(uri.getLastPathSegment());
        photoRef.putBytes(fileInBytes).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                photoRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        URI = uri.toString();

                        UserDetails newUser = new UserDetails(FirebaseAuth.getInstance().getUid(), NAME, EMAIL, PASSWORD, URI, "blank", "0");
                        ref.child(FirebaseAuth.getInstance().getUid()).setValue(newUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    startActivity(new Intent(SignUpActivity.this, HomeActivity.class));
                                    finish();
                                }
                                else {
                                    showFailedDialog("SignUp Failed", "Account creation failed due to some reason. Try again latter", "Close");
                                    progressBar.setVisibility(View.GONE);
                                    String S = "Create new account";
                                    signUp.setText(S);
                                }
                            }
                        });
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                showFailedDialog("SignUp Failed", "An error occurred with the profile picture you selected. Try different picture", "Close");
                progressBar.setVisibility(View.GONE);
                String S = "Create new account";
                signUp.setText(S);
            }
        });
    }
}