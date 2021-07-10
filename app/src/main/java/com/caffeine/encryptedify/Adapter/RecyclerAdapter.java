package com.caffeine.encryptedify.Adapter;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.caffeine.encryptedify.Model.AccountDetails;
import com.caffeine.encryptedify.R;
import com.caffeine.encryptedify.UserInterface.UploadPasswordActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

    ArrayList<AccountDetails> list;
    Activity activity;

    public RecyclerAdapter() {
    }

    public RecyclerAdapter(ArrayList<AccountDetails> list, Activity activity) {
        this.list = list;
        this.activity = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.account_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String Init, Name, Email, Password;
        String defaultTxt = "●●●●●●●●●●";
        char initial = list.get(position).getName().charAt(0);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Encryptedify").child("Passwords").child(FirebaseAuth.getInstance().getUid());

        Init = Character.toString(initial);
        Name = list.get(position).getName();
        Email = list.get(position).getEmail();
        Password = list.get(position).getPassword();

        holder.init.setText(Init.toUpperCase());
        holder.name.setText(Name);
        holder.email.setText(Email);

        holder.visible.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.password.getText().equals(defaultTxt)){
                    holder.visible.setImageResource(R.drawable.invisible);
                    holder.password.setText(Password);
                }
                else {
                    holder.visible.setImageResource(R.drawable.visible);
                    holder.password.setText(defaultTxt);
                }
            }
        });

        holder.details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, UploadPasswordActivity.class);
                intent.putExtra("id", list.get(position).getId());
                intent.putExtra("name", Name);
                intent.putExtra("email", Email);
                intent.putExtra("password", Password);
                activity.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        RelativeLayout layout;
        TextView init, name, email, password;
        ImageView visible, details;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            layout = itemView.findViewById(R.id.account_layout);
            init = itemView.findViewById(R.id.tv_icon);
            name = itemView.findViewById(R.id.tv_name);
            email = itemView.findViewById(R.id.tv_email);
            password = itemView.findViewById(R.id.tv_password);
            visible = itemView.findViewById(R.id.visible);
            details = itemView.findViewById(R.id.details);
        }
    }
}
