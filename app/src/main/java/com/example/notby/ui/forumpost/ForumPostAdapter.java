package com.example.notby.ui.forumpost;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.example.notby.R;
import com.example.notby.data.model.ForumPost;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ForumPostAdapter extends RecyclerView.Adapter<ForumPostAdapter.ViewHolder> {
    private List<ForumPost> posts = new ArrayList<>();

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

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleView;
        TextView contentView;
        TextView likesView;
        TextView viewsView;

        ViewHolder(View itemView) {
            super(itemView);
            titleView = itemView.findViewById(R.id.titleView);
            contentView = itemView.findViewById(R.id.contentView);
            likesView = itemView.findViewById(R.id.likesView);
            viewsView = itemView.findViewById(R.id.viewsView);
        }
    }
}
