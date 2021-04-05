package com.applin.twitly.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.applin.twitly.Activities.OthersProfileActivity;
import com.applin.twitly.Activities.PostViewActivity;
import com.applin.twitly.Entities.Notification;
import com.applin.twitly.Entities.User;
import com.applin.twitly.R;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private final Context mContext;
    private final List<Notification> notificationList;

    public NotificationAdapter(Context mContext, List<Notification> notificationList) {
        this.mContext = mContext;
        this.notificationList = notificationList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_notification_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Notification notification = notificationList.get(position);

        showUserDetails(holder.ivProfilePic, holder.tvName, notification.getSender());
        holder.tvAction.setText(notification.getAction());

        holder.itemView.setOnClickListener(v -> {
            Intent intent;
            if (notification.getType().equals("follow")) {
                intent = new Intent(mContext, OthersProfileActivity.class);
                intent.putExtra("USER_ID", notification.getSender());
            } else {
                intent = new Intent(mContext, PostViewActivity.class);
                intent.putExtra("POST_ID", notification.getPostid());
                intent.putExtra("USER_ID", notification.getReceiver());
            }
            mContext.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvName;
        private final TextView tvAction;
        public CircleImageView ivProfilePic;

        public ViewHolder(@NonNull View view) {
            super(view);

            tvName = view.findViewById(R.id.itNotify_name);
            tvAction = view.findViewById(R.id.itNotify_action);
            ivProfilePic = view.findViewById(R.id.itNotify_profileImage);

        }
    }

    private void showUserDetails(CircleImageView imageView, TextView textView, String publisherid) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users").child(publisherid);
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (mContext == null) {
                    return;
                }
                assert user != null;
                textView.setText(user.getName());
                if (user.getImage().equals("default")) {
                    imageView.setImageResource(R.drawable.user);
                } else {
                    Glide.with(mContext).load(user.getImage()).into(imageView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
