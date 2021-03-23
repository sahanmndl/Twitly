package com.applin.twitly.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.applin.twitly.Activities.OthersProfileActivity;
import com.applin.twitly.Entities.User;
import com.applin.twitly.R;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder>{

    private final Context mContext;
    private final List<User> mUsers;

    private FirebaseUser firebaseUser;

    public UserAdapter(Context mContext, List<User> mUsers) {
        this.mContext = mContext;
        this.mUsers = mUsers;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_user_layout, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        final User user = mUsers.get(position);

        holder.username.setText("@" + user.getUsername());
        holder.name.setText(user.getName());
        Glide.with(mContext).load(user.getImage()).into(holder.profilePic);

        holder.itemView.setOnClickListener(v -> {
            if (!user.getId().equals(firebaseUser.getUid())) {
                Intent intent = new Intent(mContext, OthersProfileActivity.class);
                intent.putExtra("USER_ID", user.getId());
                mContext.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        private final TextView username;
        private final TextView name;
        public CircleImageView profilePic;

        public ViewHolder(@NonNull View view) {
            super(view);

            username = view.findViewById(R.id.itUser_username);
            name = view.findViewById(R.id.itUser_name);
            profilePic = view.findViewById(R.id.itUser_profileImage);

        }
    }
}
