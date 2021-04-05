package com.applin.twitly.Adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.applin.twitly.Activities.OthersProfileActivity;
import com.applin.twitly.Entities.Comment;
import com.applin.twitly.Entities.User;
import com.applin.twitly.R;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

    private final Context mContext;
    private final List<Comment> commentList;
    private String postid, publisher;

    private FirebaseUser currentUser;

    public CommentAdapter(Context mContext, List<Comment> commentList, String postid, String publisher) {
        this.mContext = mContext;
        this.commentList = commentList;
        this.postid = postid;
        this.publisher = publisher;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_comment_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        Comment comment = commentList.get(position);

        Intent i = ((Activity) mContext).getIntent();
        postid = i.getStringExtra("POST_ID");
        publisher = i.getStringExtra("USER_ID");

        holder.tvComment.setText(comment.getComment());
        holder.tvTimestamp.setText(comment.getTimestamp());
        getUserInfo(holder.ivProfileImg, holder.tvName, holder.tvUsername, comment.getPublisher());

        holder.itemView.setOnClickListener(v -> {
            if (!comment.getPublisher().equals(currentUser.getUid())) {
                Intent intent = new Intent(mContext, OthersProfileActivity.class);
                intent.putExtra("USER_ID", comment.getPublisher());
                mContext.startActivity(intent);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (comment.getPublisher().equals(currentUser.getUid())) {
                PopupMenu popupMenu = new PopupMenu(mContext, v);
                popupMenu.setOnMenuItemClickListener(item -> {
                    switch (item.getItemId()) {
                        case R.id.popup_btnDelete:
                            if (postid != null) {
                                AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
                                alertDialog.setTitle("Do you want to delete this comment?");

                                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No",
                                        (dialog, which) -> dialog.dismiss());

                                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes",
                                        (dialog, which) -> {
                                            FirebaseDatabase.getInstance().getReference("Comments")
                                                    .child(postid).child(comment.getCommentid()).removeValue()
                                                    .addOnCompleteListener(task -> {
                                                        if (task.isSuccessful()) {
                                                            clearNotificationOnCommentDeletion(publisher, postid);
                                                            Toast.makeText(mContext, "Comment Deleted!", Toast.LENGTH_SHORT).show();
                                                        } else {
                                                            FirebaseAuthException exception = (FirebaseAuthException) task.getException();
                                                            assert exception != null;
                                                            Toast.makeText(mContext, "ERROR: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    });

                                            dialog.dismiss();
                                        });

                                alertDialog.show();

                                return true;

                            } else {
                                Toast.makeText(mContext, "Something went wrong!", Toast.LENGTH_SHORT).show();
                            }

                        default:
                            return false;
                    }
                });
                popupMenu.inflate(R.menu.itemview_popup_menu);
                popupMenu.show();
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        public CircleImageView ivProfileImg;
        public TextView tvName, tvUsername, tvTimestamp, tvComment;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            ivProfileImg = itemView.findViewById(R.id.itComment_profileImg);
            tvName = itemView.findViewById(R.id.itComment_name);
            tvUsername = itemView.findViewById(R.id.itComment_username);
            tvTimestamp = itemView.findViewById(R.id.itComment_timing);
            tvComment = itemView.findViewById(R.id.itComment_content);
        }
    }

    private void getUserInfo(CircleImageView circleImageView, TextView name, TextView username, String publisher) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(publisher);
        reference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                assert user != null;
                if (mContext == null) {
                    return;
                }
                Glide.with(mContext).load(user.getImage()).into(circleImageView);
                name.setText(user.getName());
                username.setText("@" + user.getUsername());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void clearNotificationOnCommentDeletion(String publisher, String postid) {
        DatabaseReference notifyRef = FirebaseDatabase.getInstance().getReference("Notifications").child(publisher);
        notifyRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    if (Objects.equals(snapshot1.child("postid").getValue(), postid)) {
                        if (Objects.equals(snapshot1.child("type").getValue(), "comment")) {
                            snapshot1.getRef().removeValue()
                                    .addOnCompleteListener(task -> {
                                        if (!task.isSuccessful()) {
                                            FirebaseAuthException exception = (FirebaseAuthException) task.getException();
                                            assert exception != null;
                                            Toast.makeText(mContext, "ERROR: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
