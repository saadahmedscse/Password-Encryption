package com.caffeine.encryptedify.UserInterface;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.caffeine.encryptedify.Adapter.RecyclerAdapter;
import com.caffeine.encryptedify.Model.AccountDetails;
import com.caffeine.encryptedify.Model.UserDetails;
import com.caffeine.encryptedify.R;
import com.caffeine.encryptedify.Signing.SignInActivity;
import com.github.ybq.android.spinkit.sprite.Sprite;
import com.github.ybq.android.spinkit.style.DoubleBounce;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeActivity extends AppCompatActivity {

    private TextView userName;
    private EditText search;
    private CircleImageView profilePicture;
    private ProgressBar progressBar, loadingBar;
    private RelativeLayout encrypt, addPass;
    private LinearLayout noPassLayout;
    private RecyclerView recyclerView;

    private DatabaseReference reference, passRef;
    private UserDetails user;
    private ArrayList<AccountDetails> list;
    private RecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        gettingLayoutIDs();
        list = new ArrayList<>();
        getUserInfo();
        onClickListeners();
    }

    @Override
    protected void onStart() {
        super.onStart();

        recyclerTask();

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String txt = s.toString().toLowerCase();
                filterAccounts(txt);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void gettingLayoutIDs(){
        reference = FirebaseDatabase.getInstance().getReference().child("Encryptedify").child("Users").child(FirebaseAuth.getInstance().getUid());
        passRef = FirebaseDatabase.getInstance().getReference().child("Encryptedify").child("Passwords").child(FirebaseAuth.getInstance().getUid());

        userName = findViewById(R.id.tv_name);
        search = findViewById(R.id.et_search);
        profilePicture = findViewById(R.id.profile_picture);
        progressBar = findViewById(R.id.progress_bar);
        encrypt = findViewById(R.id.encrypt);
        addPass = findViewById(R.id.add_pass);
        noPassLayout = findViewById(R.id.layout_no_password);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadingBar = findViewById(R.id.loading_bar);
        Sprite doubleBounce = new DoubleBounce();
        loadingBar.setIndeterminateDrawable(doubleBounce);
    }

    private void getUserInfo(){
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user = snapshot.getValue(UserDetails.class);
                userName.setText(user.getName());
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

    private void intentToActivity(Class c){
        startActivity(new Intent(HomeActivity.this, c));
    }

    private void onClickListeners(){
        encrypt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intentToActivity(EncryptionActivity.class);
            }
        });

        addPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Add Pass Activity
                intentToActivity(UploadPasswordActivity.class);
                finish();
            }
        });

        profilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intentToActivity(ProfileActivity.class);
            }
        });
    }

    private void recyclerTask(){
        if (passRef != null){
            passRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    list.clear();
                    for (DataSnapshot ds: snapshot.getChildren()){
                        list.add(ds.getValue(AccountDetails.class));
                    }

                    adapter = new RecyclerAdapter(list, HomeActivity.this);
                    adapter.notifyDataSetChanged();
                    recyclerView.setAdapter(adapter);

                    if (adapter.getItemCount() > 0){
                        loadingBar.setVisibility(View.GONE);
                    }
                    else {
                        loadingBar.setVisibility(View.GONE);
                        noPassLayout.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    private void filterAccounts(String txt){
        ArrayList<AccountDetails> myList = new ArrayList<>();

        for (AccountDetails obj : list){
            if (obj.getName().toLowerCase().contains(txt)){
                myList.add(obj);
            }
        }

        RecyclerAdapter adapter = new RecyclerAdapter(myList,HomeActivity.this);
        recyclerView.setAdapter(adapter);
    }
}