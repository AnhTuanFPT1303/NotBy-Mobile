package com.example.notby.ui.forumpost;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.example.notby.R;
import com.example.notby.data.model.ForumPost;
import com.example.notby.ui.postdetail.PostDetailActivity;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ForumPostAdapter extends RecyclerView.Adapter<ForumPostAdapter.ViewHolder> {
    private List<ForumPost> posts;

    public ForumPostAdapter(List<ForumPost> posts) {
        this.posts = new ArrayList<>(posts);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_forum_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ForumPost post = posts.get(position);
        holder.titleView.setText(post.getTitle() != null ? post.getTitle() : "");
        holder.contentView.setText(post.getContent() != null ? post.getContent() : "");
        holder.likesView.setText(String.format(Locale.getDefault(), "Likes: %d", post.getLikes()));
        holder.viewsView.setText(String.format(Locale.getDefault(), "Views: %d", post.getViews()));

        // Author name and avatar
        String authorName = "Anonymous";
        String avatarUrl = null;
        com.example.notby.data.model.User author = post.getAuthorUser();
        if (author != null) {
            if (author.getUsername() != null) authorName = author.getUsername();
            if (author.getPhoto() != null && !author.getPhoto().isEmpty()) avatarUrl = author.getPhoto();
        }
        holder.authorName.setText(authorName);
        if (avatarUrl != null) {
            holder.authorAvatar.setVisibility(View.VISIBLE);
            com.bumptech.glide.Glide.with(holder.authorAvatar.getContext())
                .load(avatarUrl)
                .placeholder(R.drawable.avatar_placeholder)
                .circleCrop()
                .into(holder.authorAvatar);
        } else {
            holder.authorAvatar.setImageResource(R.drawable.avatar_placeholder);
        }

        // Display media file thumbnail if available
        if (holder.imageView != null) {
            String fileUrl = null;
            if (post.getFileObject() != null) {
                fileUrl = post.getFileObject().getFileUrl();
            }
            if (fileUrl != null && !fileUrl.isEmpty()) {
                holder.imageView.setVisibility(View.VISIBLE);
                com.bumptech.glide.Glide.with(holder.imageView.getContext())
                    .load(fileUrl)
                    .centerCrop()
                    .into(holder.imageView);
            } else {
                holder.imageView.setVisibility(View.GONE);
            }
        }

        // Set click listener to open post detail
        holder.itemView.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, PostDetailActivity.class);
            intent.putExtra(PostDetailActivity.EXTRA_POST_ID, post.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public void updatePosts(final List<ForumPost> newPosts) {
        if (newPosts == null) return;

        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return posts.size();
            }

            @Override
            public int getNewListSize() {
                return newPosts.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return posts.get(oldItemPosition).getId().equals(newPosts.get(newItemPosition).getId());
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                ForumPost oldPost = posts.get(oldItemPosition);
                ForumPost newPost = newPosts.get(newItemPosition);
                return Objects.equals(oldPost.getTitle(), newPost.getTitle())
                    && Objects.equals(oldPost.getContent(), newPost.getContent())
                    && oldPost.getLikes() == newPost.getLikes()
                    && oldPost.getViews() == newPost.getViews();
            }
        });

        posts = new ArrayList<>(newPosts);
        diffResult.dispatchUpdatesTo(this);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleView;
        TextView contentView;
        TextView likesView;
        TextView viewsView;
        ImageView imageView;
        ImageView authorAvatar;
        TextView authorName;

        ViewHolder(View itemView) {
            super(itemView);
            titleView = itemView.findViewById(R.id.titleView);
            contentView = itemView.findViewById(R.id.contentView);
            likesView = itemView.findViewById(R.id.likesView);
            viewsView = itemView.findViewById(R.id.viewsView);
            imageView = itemView.findViewById(R.id.postImageThumb);
            authorAvatar = itemView.findViewById(R.id.authorAvatar);
            authorName = itemView.findViewById(R.id.authorName);
        }
    }
}
