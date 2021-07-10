package com.caffeine.encryptedify.Profile;

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
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.caffeine.encryptedify.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity extends AppCompatActivity {

    private CardView placeHolder;
    private ImageView profilePicture;
    private EditText name;
    private TextView tvSave, tvUpload, tvChangePic;
    private RelativeLayout save, upload;
    private ProgressBar progressBarOne, progressBarTwo;
    private String Name, Url, NewUrl;
    final private String a = "Save", b = "Upload";

    private DatabaseReference ref, photoRef;
    private StorageReference profilePicRef;
    private Dialog dialog;
    private Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        gettingLayoutIDs();
        getPhotoUrl();

        placeHolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (uri == null){
                    String e = "Select profile picture first";
                    tvChangePic.setText(e);
                    tvChangePic.setTextColor(getResources().getColor(R.color.colorRed));
                }

                else {
                    progressBarTwo.setVisibility(View.VISIBLE);
                    tvUpload.setText("");
                    deletePreviousProfilePicture();
                }
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Name = name.getText().toString();

                if (Name.isEmpty()){
                    name.setError("Field cannot be empty");
                    name.requestFocus();
                }
                else {
                    progressBarOne.setVisibility(View.VISIBLE);
                    tvSave.setText("");
                    ref.setValue(Name).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                               showPopupDialog(
                                       "Name changed",
                                       "The name you provided has been updated to your account",
                                       "Close"
                               );
                                progressBarOne.setVisibility(View.GONE);
                                tvSave.setText(a);
                            }

                            else {
                                showPopupDialog(
                                        "Error occurred",
                                        "Something went wrong, please try again later",
                                        "Close"
                                );
                                progressBarOne.setVisibility(View.GONE);
                                tvSave.setText(a);
                            }
                        }
                    });
                }
            }
        });
    }

    private void getPhotoUrl(){
        photoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Url = snapshot.getValue(String.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void deletePreviousProfilePicture(){
        StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(Url);
        storageRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    uploadNewProfilePicture();
                }

                else {
                    showPopupDialog(
                            "Upload Failed",
                            "And error occurred while uploading your profile picture",
                            "Close"
                    );
                    progressBarTwo.setVisibility(View.GONE);
                    tvUpload.setText(b);
                }
            }
        });
    }

    private void uploadNewProfilePicture(){
        Bitmap bmp = null;

        try {
            bmp = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
        }
        catch (Exception ignored){}

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 15, baos);
        byte[] fileInBytes = baos.toByteArray();

        final StorageReference storageReference = profilePicRef.child(uri.getLastPathSegment());
        storageReference.putBytes(fileInBytes).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        NewUrl = uri.toString();

                        photoRef.setValue(NewUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    showPopupDialog(
                                            "Uploaded",
                                            "Profile picture has been successfully uploaded",
                                            "Close"
                                    );
                                    progressBarTwo.setVisibility(View.GONE);
                                    tvUpload.setText(b);
                                }

                                else {
                                    showPopupDialog(
                                            "Upload Failed",
                                            "And error occurred while uploading your profile picture",
                                            "Close"
                                    );
                                    progressBarTwo.setVisibility(View.GONE);
                                    tvUpload.setText(b);
                                }
                            }
                        });
                    }
                });
            }
        });
    }

    private void gettingLayoutIDs(){
        ref = FirebaseDatabase.getInstance().getReference().child("Encryptedify").child("Users").child(FirebaseAuth.getInstance().getUid()).child("name");
        photoRef = FirebaseDatabase.getInstance().getReference().child("Encryptedify").child("Users").child(FirebaseAuth.getInstance().getUid()).child("uri");
        profilePicRef = FirebaseStorage.getInstance().getReference().child("Profile Picture");
        dialog = new Dialog(this);

        placeHolder = findViewById(R.id.image_placeholder);
        profilePicture = findViewById(R.id.profile_picture);
        tvChangePic = findViewById(R.id.tv_change_pic);
        name = findViewById(R.id.et_name);
        upload = findViewById(R.id.change_picture);
        save = findViewById(R.id.change_name);
        tvSave = findViewById(R.id.tv_save);
        tvUpload = findViewById(R.id.tv_upload);
        progressBarOne = findViewById(R.id.progress_bar_one);
        progressBarTwo = findViewById(R.id.progress_bar_two);
    }

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
            String o = "Change profile picture";
            tvChangePic.setText(o);
            tvChangePic.setTextColor(getResources().getColor(R.color.colorDarkGrey));
            profilePicture.setImageURI(uri);
        }
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
}