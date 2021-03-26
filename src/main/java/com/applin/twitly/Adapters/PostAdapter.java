package com.applin.twitly.Adapters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.applin.twitly.Activities.CommentsActivity;
import com.applin.twitly.Activities.ImageViewActivity;
import com.applin.twitly.Activities.OthersProfileActivity;
import com.applin.twitly.Activities.PostViewActivity;
import com.applin.twitly.Entities.Post;
import com.applin.twitly.Entities.User;
import com.applin.twitly.R;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    public Context mContext;
    public List<Post> mPost;

    private FirebaseUser currentUser;

    public PostAdapter(Context mContext, List<Post> mPost) {
        this.mContext = mContext;
        this.mPost = mPost;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_post_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        Post post = mPost.get(position);

        if (post.getPostcontent().equals("")) {
            holder.tvContent.setVisibility(View.GONE);
        } else {
            holder.tvContent.setVisibility(View.VISIBLE);
            holder.tvContent.setText(post.getPostcontent());
        }

        holder.tvTimestamp.setText(post.getTimestamp());

        if (post.getPostimage() == null) {
            holder.ivPostedImg.setVisibility(View.GONE);
        } else {
            holder.ivPostedImg.setVisibility(View.VISIBLE);
            Glide.with(mContext).load(post.getPostimage()).into(holder.ivPostedImg);
        }

        holder.ivProfileImg.setOnClickListener(v -> {
            if (!post.getPublisher().equals(currentUser.getUid())) {
                Intent intent = new Intent(mContext, OthersProfileActivity.class);
                intent.putExtra("USER_ID", post.getPublisher());
                mContext.startActivity(intent);
            }
        });

        holder.ivPostedImg.setOnClickListener(v -> {
            Intent intent = new Intent(mContext, ImageViewActivity.class);
            intent.putExtra("EXTRA_IMAGE", post.getPostimage());
            mContext.startActivity(intent);
        });

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(mContext, PostViewActivity.class);
            intent.putExtra("POST_ID", post.getPostid());
            intent.putExtra("USER_ID", post.getPublisher());
            mContext.startActivity(intent);
        });

        holder.btnComment.setOnClickListener(v -> {
            Intent intent = new Intent(mContext, CommentsActivity.class);
            intent.putExtra("POST_ID", post.getPostid());
            mContext.startActivity(intent);
        });

        holder.btnLike.setOnClickListener(v -> {
            if (holder.btnLike.getTag().equals("like")) {
                FirebaseDatabase.getInstance().getReference("Likes")
                        .child(post.getPostid()).child(currentUser.getUid()).setValue(true);
            } else {
                FirebaseDatabase.getInstance().getReference("Likes")
                        .child(post.getPostid()).child(currentUser.getUid()).removeValue();
            }
        });

        holder.btnBookmark.setOnClickListener(v -> {
            if (holder.btnBookmark.getTag().equals("bookmark")) {
                FirebaseDatabase.getInstance().getReference("Bookmarks")
                        .child(currentUser.getUid()).child(post.getPostid()).setValue(true);
            } else {
                FirebaseDatabase.getInstance().getReference("Bookmarks")
                        .child(currentUser.getUid()).child(post.getPostid()).removeValue();
            }
        });

        if (post.getPublisher().equals(currentUser.getUid())) {
            holder.btnPopUp.setVisibility(View.VISIBLE);
            holder.btnPopUp.setOnClickListener(v -> {
                PopupMenu popupMenu = new PopupMenu(mContext, v);
                popupMenu.setOnMenuItemClickListener(item -> {
                    switch (item.getItemId()) {
                        case R.id.popup_btnDelete:
                            AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
                            alertDialog.setTitle("Do you want to delete this post?");

                            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No",
                                    (dialog, which) -> dialog.dismiss());

                            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes",
                                    (dialog, which) -> {
                                        FirebaseDatabase.getInstance().getReference("Posts")
                                                .child(post.getPostid()).removeValue();
                                        FirebaseDatabase.getInstance().getReference("Comments")
                                                .child(post.getPostid()).removeValue();
                                        FirebaseDatabase.getInstance().getReference("Likes")
                                                .child(post.getPostid()).removeValue();
                                        FirebaseDatabase.getInstance().getReference("Bookmarks")
                                                .child(post.getPostid()).removeValue();

                                        String url = post.getPostimage();
                                        if (url != null) {
                                            StorageReference imagesRef = FirebaseStorage.getInstance().getReferenceFromUrl(url);
                                            imagesRef.delete().addOnSuccessListener(aVoid -> Toast.makeText(mContext, "Post deleted!", Toast.LENGTH_SHORT).show())
                                                    .addOnFailureListener(e -> Toast.makeText(mContext, "ERROR: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                                        } else {
                                            Toast.makeText(mContext, "Deleted!", Toast.LENGTH_SHORT).show();
                                        }

                                        dialog.dismiss();
                                    });

                            alertDialog.show();

                            return true;

                        default:
                            return false;
                    }
                });
                popupMenu.inflate(R.menu.itemview_popup_menu);
                popupMenu.show();
            });
        } else {
            holder.btnPopUp.setVisibility(View.GONE);
        }

        publisherInfo(holder.ivProfileImg, holder.tvUsername, holder.tvName, post.getPublisher());
        countComments(holder.commentsCounter, post.getPostid());
        isLiked(post.getPostid(), holder.btnLike);
        countLikes(holder.likesCounter, post.getPostid());
        isBookmarked(post.getPostid(), holder.btnBookmark);
    }

    @Override
    public int getItemCount() {
        return mPost.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView ivProfileImg, ivPostedImg, btnLike, btnComment, btnBookmark, btnPopUp;
        public TextView tvUsername, likesCounter, tvName, tvContent, tvTimestamp, commentsCounter;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            ivProfileImg = itemView.findViewById(R.id.post_profileImg);
            ivPostedImg = itemView.findViewById(R.id.post_image);
            btnLike = itemView.findViewById(R.id.post_btnLike);
            btnComment = itemView.findViewById(R.id.post_btnComment);
            btnBookmark = itemView.findViewById(R.id.post_btnBookmark);
            btnPopUp = itemView.findViewById(R.id.post_btnPopUp);
            tvName = itemView.findViewById(R.id.post_name);
            tvUsername = itemView.findViewById(R.id.post_username);
            tvTimestamp = itemView.findViewById(R.id.post_timing);
            tvContent = itemView.findViewById(R.id.post_content);
            likesCounter = itemView.findViewById(R.id.post_txtLike);
            commentsCounter = itemView.findViewById(R.id.post_txtComment);
        }
    }

    private void publisherInfo(ImageView imageProfile, TextView username, TextView name, String id) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(id);
        reference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (mContext == null) {
                    return;
                }

                User user = snapshot.getValue(User.class);
                assert user != null;

                if (user.getImage().equals("default")) {
                    imageProfile.setImageResource(R.drawable.user);
                } else {
                    Glide.with(mContext).load(user.getImage()).into(imageProfile);
                }
                username.setText("@" + user.getUsername());
                name.setText(user.getName());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void isLiked(String postid, final ImageView imageView) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Likes").child(postid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                assert currentUser != null;
                if (snapshot.child(currentUser.getUid()).exists()) {
                    imageView.setImageResource(R.drawable.ic_like_filled);
                    imageView.setTag("liked");
                } else {
                    imageView.setImageResource(R.drawable.ic_like);
                    imageView.setTag("like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void countLikes(TextView likes, String postid) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Likes").child(postid);
        reference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                likes.setText(snapshot.getChildrenCount() + "");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void countComments(TextView commentsCounter, String postid) {
        DatabaseReference commentsRef = FirebaseDatabase.getInstance().getReference("Comments").child(postid);
        commentsRef.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                commentsCounter.setText(snapshot.getChildrenCount() + "");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void isBookmarked(String postid, ImageView imageView) {
        DatabaseReference bookmarksRef = FirebaseDatabase.getInstance().getReference("Bookmarks").child(currentUser.getUid());
        bookmarksRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(postid).exists()) {
                    imageView.setImageResource(R.drawable.ic_bookmark_filled);
                    imageView.setTag("bookmarked");
                } else {
                    imageView.setImageResource(R.drawable.ic_bookmark);
                    imageView.setTag("bookmark");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
