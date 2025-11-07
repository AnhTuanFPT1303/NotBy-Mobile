package com.example.notby.ui.library;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.notby.R;
import com.example.notby.data.model.Article;
import com.example.notby.data.model.MediaFile;

import java.util.ArrayList;
import java.util.List;

public class LibraryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_ARTICLE = 0;
    private static final int TYPE_VIDEO = 1;
    private List<Article> articles = new ArrayList<>();
    private List<MediaFile> videos = new ArrayList<>();
    private OnArticleClickListener articleListener;
    private OnVideoClickListener videoListener;

    public interface OnArticleClickListener {
        void onArticleClick(Article article);
    }
    public interface OnVideoClickListener {
        void onVideoClick(MediaFile video);
    }
    public void setOnArticleClickListener(OnArticleClickListener listener) {
        this.articleListener = listener;
    }
    public void setOnVideoClickListener(OnVideoClickListener listener) {
        this.videoListener = listener;
    }
    public void setArticles(List<Article> articles) {
        this.articles = articles;
        this.videos.clear();
        notifyDataSetChanged();
    }
    public void setVideos(List<MediaFile> videos) {
        this.videos = videos;
        this.articles.clear();
        notifyDataSetChanged();
    }
    @Override
    public int getItemViewType(int position) {
        return articles.isEmpty() ? TYPE_VIDEO : TYPE_ARTICLE;
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_ARTICLE) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_library_article, parent, false);
            return new ArticleViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_library_video, parent, false);
            return new VideoViewHolder(view);
        }
    }
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_ARTICLE) {
            ((ArticleViewHolder) holder).bind(articles.get(position));
        } else {
            ((VideoViewHolder) holder).bind(videos.get(position));
        }
    }
    @Override
    public int getItemCount() {
        return articles.isEmpty() ? videos.size() : articles.size();
    }
    class ArticleViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageArticle;
        private final TextView textTitle;
        private final TextView textDescription;
        private final TextView textViews;
        private final TextView textLikes;
        private final TextView textReadTime;
        private final TextView textCategory;

        public ArticleViewHolder(@NonNull View itemView) {
            super(itemView);
            imageArticle = itemView.findViewById(R.id.imageArticle);
            textTitle = itemView.findViewById(R.id.textTitle);
            textDescription = itemView.findViewById(R.id.textDescription);
            textViews = itemView.findViewById(R.id.textViews);
            textLikes = itemView.findViewById(R.id.textLikes);
            textReadTime = itemView.findViewById(R.id.textReadTime);
            textCategory = itemView.findViewById(R.id.textCategory);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && articleListener != null) {
                    articleListener.onArticleClick(articles.get(position));
                }
            });
        }

        public void bind(Article article) {
            textTitle.setText(article.getTitle());
            textDescription.setText(article.getDescription());
            textViews.setText(String.format("%d views", article.getViews()));
            textLikes.setText(String.format("%d likes", article.getLikes()));
            textReadTime.setText(String.format("%d min read", article.getReadTime()));

            if (article.getCategory() != null) {
                textCategory.setText(article.getCategory().getTitle());
                textCategory.setVisibility(View.VISIBLE);
            } else {
                textCategory.setVisibility(View.GONE);
            }

            if (article.getImageUrl() != null && !article.getImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(article.getImageUrl())
                        .centerCrop()
                        .into(imageArticle);
            }
        }
    }
    class VideoViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageVideoThumb;
        private final TextView textVideoTitle;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            imageVideoThumb = itemView.findViewById(R.id.imageVideoThumb);
            textVideoTitle = itemView.findViewById(R.id.textVideoTitle);
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && videoListener != null) {
                    videoListener.onVideoClick(videos.get(position));
                }
            });
        }

        public void bind(MediaFile video) {
            textVideoTitle.setText(video.getFileName());
            Glide.with(itemView.getContext())
                    .load(video.getFileUrl())
                    .centerCrop()
                    .into(imageVideoThumb);
        }
    }
}
