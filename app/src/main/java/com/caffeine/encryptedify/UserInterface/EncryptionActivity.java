package com.caffeine.encryptedify.UserInterface;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import com.caffeine.encryptedify.R;
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

public class EncryptionActivity extends AppCompatActivity {

    private RelativeLayout btnPaste, btnEncrypt, btnDecrypt, copyPass;
    private LinearLayout encrypted, decrypted;
    private TextView key, result;
    private EditText plainText;
    private String Encrypted = "", Decrypted = "", RPassword;

    private DatabaseReference reference;
    private UserDetails user;
    private Dialog dialog;
    private String Key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encryption);

        Intent i = getIntent();
        RPassword = i.getStringExtra("password");

        gettingLayoutIDs();
        onClickListeners();
        getUserInfo();
    }

    private void gettingLayoutIDs(){
        reference = FirebaseDatabase.getInstance().getReference().child("Encryptedify").child("Users").child(FirebaseAuth.getInstance().getUid());
        dialog = new Dialog(this);

        btnPaste = findViewById(R.id.btn_paste);
        key = findViewById(R.id.encryption_key);
        plainText = findViewById(R.id.et_plain_txt);
        btnEncrypt = findViewById(R.id.btn_encrypt);
        btnDecrypt = findViewById(R.id.btn_decrypt);
        encrypted = findViewById(R.id.encrypted);
        decrypted = findViewById(R.id.decrypted);
        result = findViewById(R.id.tv_result);
        copyPass = findViewById(R.id.copy_pass);
    }

    private void receivedTask(){
        if (RPassword != null){
            plainText.setText(RPassword);

            encryptDecrypt("decrypt");

            decrypted.setVisibility(View.VISIBLE);
            encrypted.setVisibility(View.GONE);
            result.setVisibility(View.VISIBLE);
            copyPass.setVisibility(View.VISIBLE);
        }
    }

    private void onClickListeners(){
        btnPaste.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                plainText.setText(clipboard.getText());
            }
        });

        btnEncrypt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                encryptDecrypt("encrypt");
            }
        });

        btnDecrypt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                encryptDecrypt("decrypt");
            }
        });

        copyPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                clipboard.setText(result.getText().toString());
            }
        });
    }

    private void encryptDecrypt(String type){
        String password = plainText.getText().toString();
        int KEY = Integer.parseInt(key.getText().toString());

        Encrypted = "";
        Decrypted = "";

        char[] passArray = password.toCharArray();

        if (password.isEmpty()){
            plainText.setError("Filed cannot be empty");
            plainText.requestFocus();
        }

        else if (type.equals("encrypt")){
            for (char c : passArray){
                c += KEY;
                Encrypted += c;
            }
            result.setText(Encrypted);
            encrypted.setVisibility(View.VISIBLE);
            decrypted.setVisibility(View.GONE);
            result.setVisibility(View.VISIBLE);
            copyPass.setVisibility(View.VISIBLE);
        }

        else if (type.equals("decrypt")){
            for (char c : passArray){
                c -= KEY;
                Decrypted += c;
            }
            result.setText(Decrypted);
            decrypted.setVisibility(View.VISIBLE);
            encrypted.setVisibility(View.GONE);
            result.setVisibility(View.VISIBLE);
            copyPass.setVisibility(View.VISIBLE);
        }
    }

    private void getUserInfo(){
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user = snapshot.getValue(UserDetails.class);
                Key = user.getKey();
                keyTask();

                receivedTask();
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
        }
    }
}