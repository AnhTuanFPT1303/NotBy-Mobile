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
import com.example.notby.data.model.User;

import java.util.ArrayList;
import java.util.List;

public class LibraryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_ARTICLE = 0;
    private static final int TYPE_VIDEO = 1;
    private static final int TYPE_DOCUMENT = 2;
    private static final int TYPE_EXPERT = 3;
    private List<Article> articles = new ArrayList<>();
    private List<MediaFile> videos = new ArrayList<>();
    private List<MediaFile> documents = new ArrayList<>();
    private List<User> experts = new ArrayList<>();
    private OnArticleClickListener articleListener;
    private OnVideoClickListener videoListener;
    private OnDocumentClickListener documentListener;
    private OnExpertClickListener expertListener;

    public interface OnArticleClickListener {
        void onArticleClick(Article article);
    }
    public interface OnVideoClickListener {
        void onVideoClick(MediaFile video);
    }
    public interface OnDocumentClickListener {
        void onDocumentClick(MediaFile document);
    }
    public interface OnExpertClickListener {
        void onExpertClick(User expert);
    }
    public void setOnArticleClickListener(OnArticleClickListener listener) {
        this.articleListener = listener;
    }
    public void setOnVideoClickListener(OnVideoClickListener listener) {
        this.videoListener = listener;
    }
    public void setOnDocumentClickListener(OnDocumentClickListener listener) {
        this.documentListener = listener;
    }
    public void setOnExpertClickListener(OnExpertClickListener listener) {
        this.expertListener = listener;
    }
    public void setArticles(List<Article> articles) {
        this.articles = articles;
        this.videos.clear();
        this.documents.clear();
        this.experts.clear();
        notifyDataSetChanged();
    }

    // Insert a newly created article at the front of the list and notify adapter.
    public void addArticleAtFront(Article article) {
        if (article == null) return;
        if (articles == null) articles = new ArrayList<>();
        articles.add(0, article);
        // ensure other lists are cleared because this adapter shows one type at a time
        this.videos.clear();
        this.documents.clear();
        this.experts.clear();
        // Dataset type changed (was maybe video/document), safer to refresh whole adapter
        notifyDataSetChanged();
    }
    public void setVideos(List<MediaFile> videos) {
        this.videos = videos;
        this.articles.clear();
        this.documents.clear();
        this.experts.clear();
        notifyDataSetChanged();
    }
    public void setDocuments(List<MediaFile> documents) {
        this.documents = documents;
        this.articles.clear();
        this.videos.clear();
        this.experts.clear();
        notifyDataSetChanged();
    }

    public void setExperts(List<User> experts) {
        this.experts = experts;
        this.articles.clear();
        this.videos.clear();
        this.documents.clear();
        notifyDataSetChanged();
    }
    @Override
    public int getItemViewType(int position) {
        // Priority: articles, videos, documents, experts
        if (!articles.isEmpty()) return TYPE_ARTICLE;
        if (!videos.isEmpty()) return TYPE_VIDEO;
        if (!documents.isEmpty()) return TYPE_DOCUMENT;
        return TYPE_EXPERT;
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_ARTICLE) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_library_article, parent, false);
            return new ArticleViewHolder(view);
        } else if (viewType == TYPE_VIDEO) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_library_video, parent, false);
            return new VideoViewHolder(view);
        } else if (viewType == TYPE_EXPERT) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_library_expert, parent, false);
            return new ExpertViewHolder(view);
        } else {
            // use same layout as video for documents (thumbnail + title)
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_library_video, parent, false);
            return new DocumentViewHolder(view);
        }
    }
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_ARTICLE) {
            ((ArticleViewHolder) holder).bind(articles.get(position));
        } else if (getItemViewType(position) == TYPE_VIDEO) {
            ((VideoViewHolder) holder).bind(videos.get(position));
        } else if (getItemViewType(position) == TYPE_EXPERT) {
            ((ExpertViewHolder) holder).bind(experts.get(position));
        } else {
            ((DocumentViewHolder) holder).bind(documents.get(position));
        }
    }
    @Override
    public int getItemCount() {
        if (!articles.isEmpty()) return articles.size();
        if (!videos.isEmpty()) return videos.size();
        if (!documents.isEmpty()) return documents.size();
        return experts.size();
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
    class DocumentViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageDocThumb;
        private final TextView textDocTitle;

        public DocumentViewHolder(@NonNull View itemView) {
            super(itemView);
            imageDocThumb = itemView.findViewById(R.id.imageVideoThumb);
            textDocTitle = itemView.findViewById(R.id.textVideoTitle);
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && documentListener != null) {
                    documentListener.onDocumentClick(documents.get(position));
                }
            });
        }

        public void bind(MediaFile document) {
            textDocTitle.setText(document.getFileName());
            // Try to load fileUrl as thumbnail if available, otherwise show a generic icon
            if (document.getFileUrl() != null && !document.getFileUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(document.getFileUrl())
                        .centerCrop()
                        .into(imageDocThumb);
            } else {
                imageDocThumb.setImageResource(R.drawable.ic_file); // fallback icon
            }
        }
    }

    class ExpertViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageExpert;
        private final TextView textExpertName;
        private final TextView textExpertRole;
        private final TextView textExpertEmail;

        public ExpertViewHolder(@NonNull View itemView) {
            super(itemView);
            imageExpert = itemView.findViewById(R.id.imageExpert);
            textExpertName = itemView.findViewById(R.id.textExpertName);
            textExpertRole = itemView.findViewById(R.id.textExpertRole);
            textExpertEmail = itemView.findViewById(R.id.textExpertEmail);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && expertListener != null) {
                    expertListener.onExpertClick(experts.get(position));
                }
            });
        }

        public void bind(User expert) {
            textExpertName.setText(expert.getUsername());
            textExpertRole.setText(expert.getRole());
            textExpertEmail.setText(expert.getEmail());

            // Load expert photo if available
            if (expert.getPhoto() != null && !expert.getPhoto().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(expert.getPhoto())
                        .centerCrop()
                        .placeholder(R.drawable.ic_person)
                        .into(imageExpert);
            } else {
                imageExpert.setImageResource(R.drawable.ic_person);
            }
        }
    }

    /**
     * Check if an article already exists in the adapter.
     * @param article The article to check.
     * @return True if the article exists, false otherwise.
     */
    public boolean containsArticle(Article article) {
        if (article == null || article.getId() == null) return false;
        for (Article a : articles) {
            if (article.getId().equals(a.getId())) {
                return true;
            }
        }
        return false;
    }
}
