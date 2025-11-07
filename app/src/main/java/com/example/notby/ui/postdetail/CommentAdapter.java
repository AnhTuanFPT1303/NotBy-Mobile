package com.example.notby.ui.postdetail;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.notby.R;
import com.example.notby.data.model.Comment;
import com.example.notby.data.model.User;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {
    private List<Comment> comments = new ArrayList<>();
    private OnCommentLikeListener onCommentLikeListener;

    public interface OnCommentLikeListener {
        void onCommentLike(Comment comment, int position);
    }

    public CommentAdapter(OnCommentLikeListener listener) {
        this.onCommentLikeListener = listener;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = comments.get(position);

        // Set comment content
        holder.commentContent.setText(comment.getContent() != null ? comment.getContent() : "");

        // Set author name
        User author = comment.getCreatedByUser();
        if (author != null && author.getUsername() != null) {
            holder.commentAuthor.setText(author.getUsername());
        } else {
            holder.commentAuthor.setText("Anonymous");
        }

        // Set time
        holder.commentTime.setText(formatTime(comment.getCreatedAt()));

        // Set like count
        holder.commentLikeCount.setText(String.valueOf(comment.getLikes()));

        // Set like button click listener
        holder.commentLikeButton.setOnClickListener(v -> {
            if (onCommentLikeListener != null) {
                onCommentLikeListener.onCommentLike(comment, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments != null ? comments : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void updateComment(int position, Comment comment) {
        if (position >= 0 && position < comments.size()) {
            comments.set(position, comment);
            notifyItemChanged(position);
        }
    }

    private String formatTime(String timestamp) {
        if (timestamp == null || timestamp.isEmpty()) {
            return "";
        }

        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            Date date = inputFormat.parse(timestamp);

            long diffInMillis = System.currentTimeMillis() - (date != null ? date.getTime() : 0);
            long diffInSeconds = diffInMillis / 1000;
            long diffInMinutes = diffInSeconds / 60;
            long diffInHours = diffInMinutes / 60;
            long diffInDays = diffInHours / 24;

            if (diffInSeconds < 60) {
                return "Just now";
            } else if (diffInMinutes < 60) {
                return diffInMinutes + "m ago";
            } else if (diffInHours < 24) {
                return diffInHours + "h ago";
            } else if (diffInDays < 7) {
                return diffInDays + "d ago";
            } else {
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                return outputFormat.format(date);
            }
        } catch (Exception e) {
            return "";
        }
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView commentAuthor;
        TextView commentTime;
        TextView commentContent;
        View commentLikeButton;
        TextView commentLikeCount;

        CommentViewHolder(View itemView) {
            super(itemView);
            commentAuthor = itemView.findViewById(R.id.commentAuthor);
            commentTime = itemView.findViewById(R.id.commentTime);
            commentContent = itemView.findViewById(R.id.commentContent);
            commentLikeButton = itemView.findViewById(R.id.commentLikeButton);
            commentLikeCount = itemView.findViewById(R.id.commentLikeCount);
        }
    }
}

