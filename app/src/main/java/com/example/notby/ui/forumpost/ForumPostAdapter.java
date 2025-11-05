package com.example.notby.ui.forumpost;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.notby.R;
import com.example.notby.model.ForumPost;

import java.util.List;

public class ForumPostAdapter extends RecyclerView.Adapter<ForumPostAdapter.ForumPostViewHolder> {

    private List<ForumPost> forumPostList;

    public ForumPostAdapter(List<ForumPost> forumPostList) {
        this.forumPostList = forumPostList;
    }

    @NonNull
    @Override
    public ForumPostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_forum_post, parent, false);
        return new ForumPostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ForumPostViewHolder holder, int position) {
        ForumPost forumPost = forumPostList.get(position);
        holder.bind(forumPost);
    }

    @Override
    public int getItemCount() {
        return forumPostList.size();
    }

    static class ForumPostViewHolder extends RecyclerView.ViewHolder {
        private ImageView authorImage;
        private TextView authorName;
        private TextView postDate;
        private TextView postTitle;
        private TextView postContent;
        private TextView commentsCount;
        private TextView viewsCount;
        private TextView likesCount;

        public ForumPostViewHolder(@NonNull View itemView) {
            super(itemView);
            authorImage = itemView.findViewById(R.id.authorImage);
            authorName = itemView.findViewById(R.id.authorName);
            postDate = itemView.findViewById(R.id.postDate);
            postTitle = itemView.findViewById(R.id.postTitle);
            postContent = itemView.findViewById(R.id.postContent);
            commentsCount = itemView.findViewById(R.id.commentsCount);
            viewsCount = itemView.findViewById(R.id.viewsCount);
            likesCount = itemView.findViewById(R.id.likesCount);
        }

        public void bind(ForumPost forumPost) {
            authorName.setText(forumPost.getAuthor().getFullName());
            postDate.setText(forumPost.getCreatedAt()); // You might want to format this date
            postTitle.setText(forumPost.getTitle());
            postContent.setText(forumPost.getContent());
            viewsCount.setText(String.valueOf(forumPost.getViews()));
            likesCount.setText(String.valueOf(forumPost.getLikes()));

            if (forumPost.getAuthor().getPhoto() != null && !forumPost.getAuthor().getPhoto().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(forumPost.getAuthor().getPhoto())
                        .into(authorImage);
            }
        }
    }
}
