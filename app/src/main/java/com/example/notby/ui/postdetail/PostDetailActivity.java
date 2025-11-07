package com.example.notby.ui.postdetail;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.notby.R;
import com.example.notby.data.TokenManager;
import com.example.notby.data.model.ApiResponse;
import com.example.notby.data.model.Comment;
import com.example.notby.data.model.ForumPost;
import com.example.notby.data.model.User;
import com.example.notby.data.remote.ApiClient;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PostDetailActivity extends AppCompatActivity {
    public static final String EXTRA_POST_ID = "post_id";

    private TextView postTitle;
    private TextView postAuthor;
    private TextView postContent;
    private TextView likeCount;
    private TextView viewCount;
    private TextView commentCount;
    private View likeButton;
    private ImageView likeIcon;
    private EditText commentInput;
    private ImageView sendCommentButton;
    private RecyclerView commentsRecyclerView;
    private TextView emptyCommentsText;
    private ProgressBar progressBar;

    private CommentAdapter commentAdapter;
    private ForumPost currentPost;
    private String postId;
    private String currentUserId;
    private boolean isLiked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        // Get post ID from intent
        postId = getIntent().getStringExtra(EXTRA_POST_ID);
        if (postId == null || postId.isEmpty()) {
            Toast.makeText(this, "Invalid post ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Get current user ID from TokenManager
        TokenManager tokenManager = new TokenManager(this);
        currentUserId = tokenManager.getUserId();

        initViews();
        setupRecyclerView();
        setupListeners();

        // Load post details and comments
        loadPostDetail();
        loadComments();
    }

    private void initViews() {
        postTitle = findViewById(R.id.postTitle);
        postAuthor = findViewById(R.id.postAuthor);
        postContent = findViewById(R.id.postContent);
        likeCount = findViewById(R.id.likeCount);
        viewCount = findViewById(R.id.viewCount);
        commentCount = findViewById(R.id.commentCount);
        likeButton = findViewById(R.id.likeButton);
        likeIcon = findViewById(R.id.likeIcon);
        commentInput = findViewById(R.id.commentInput);
        sendCommentButton = findViewById(R.id.sendCommentButton);
        commentsRecyclerView = findViewById(R.id.commentsRecyclerView);
        emptyCommentsText = findViewById(R.id.emptyCommentsText);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupRecyclerView() {
        commentAdapter = new CommentAdapter((comment, position) -> {
            // Handle comment like
            likeComment(comment, position);
        });
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentsRecyclerView.setAdapter(commentAdapter);
    }

    private void setupListeners() {
        likeButton.setOnClickListener(v -> toggleLike());
        sendCommentButton.setOnClickListener(v -> postComment());
    }

    private void loadPostDetail() {
        progressBar.setVisibility(View.VISIBLE);

        ApiClient.getForumPostApi().getById(postId).enqueue(new Callback<ApiResponse<ForumPost>>() {
            @Override
            public void onResponse(Call<ApiResponse<ForumPost>> call, Response<ApiResponse<ForumPost>> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<ForumPost> apiResponse = response.body();
                    if (apiResponse.isStatus() && apiResponse.getData() != null) {
                        currentPost = apiResponse.getData();
                        displayPostDetail();
                    } else {
                        Toast.makeText(PostDetailActivity.this,
                            "Failed to load post: " + apiResponse.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(PostDetailActivity.this,
                        "Failed to load post",
                        Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<ForumPost>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(PostDetailActivity.this,
                    "Error: " + t.getMessage(),
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayPostDetail() {
        if (currentPost == null) return;

        postTitle.setText(currentPost.getTitle() != null ? currentPost.getTitle() : "");
        postContent.setText(currentPost.getContent() != null ? currentPost.getContent() : "");

        // Display author
        User author = currentPost.getAuthorUser();
        if (author != null && author.getUsername() != null) {
            postAuthor.setText("By " + author.getUsername());
        } else {
            postAuthor.setText("By Anonymous");
        }

        // Display stats
        likeCount.setText(String.valueOf(currentPost.getLikes()));
        viewCount.setText(String.valueOf(currentPost.getViews()));

        // Update like button state
        updateLikeButton();
    }

    private void loadComments() {
        ApiClient.getForumCommentApi().findByPost(postId).enqueue(new Callback<ApiResponse<List<Comment>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Comment>>> call, Response<ApiResponse<List<Comment>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Comment>> apiResponse = response.body();
                    if (apiResponse.isStatus() && apiResponse.getData() != null) {
                        List<Comment> comments = apiResponse.getData();
                        commentAdapter.setComments(comments);

                        // Update comment count
                        commentCount.setText(String.valueOf(comments.size()));

                        // Show/hide empty state
                        if (comments.isEmpty()) {
                            emptyCommentsText.setVisibility(View.VISIBLE);
                            commentsRecyclerView.setVisibility(View.GONE);
                        } else {
                            emptyCommentsText.setVisibility(View.GONE);
                            commentsRecyclerView.setVisibility(View.VISIBLE);
                        }
                    } else {
                        commentCount.setText("0");
                        emptyCommentsText.setVisibility(View.VISIBLE);
                        commentsRecyclerView.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Comment>>> call, Throwable t) {
                Toast.makeText(PostDetailActivity.this,
                    "Failed to load comments",
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toggleLike() {
        if (currentPost == null) return;

        // Update UI optimistically
        int newLikes = isLiked ? currentPost.getLikes() - 1 : currentPost.getLikes() + 1;
        currentPost.setLikes(newLikes);
        isLiked = !isLiked;
        likeCount.setText(String.valueOf(newLikes));
        updateLikeButton();

        // TODO: Make API call to update like on backend
        // For now, we'll just update the UI
        Toast.makeText(this, isLiked ? "Liked!" : "Unliked", Toast.LENGTH_SHORT).show();
    }

    private void updateLikeButton() {
        if (isLiked) {
            likeIcon.setColorFilter(ContextCompat.getColor(this, android.R.color.holo_red_dark));
        } else {
            likeIcon.setColorFilter(ContextCompat.getColor(this, android.R.color.darker_gray));
        }
    }

    private void postComment() {
        String content = commentInput.getText().toString().trim();

        if (content.isEmpty()) {
            Toast.makeText(this, "Please enter a comment", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentUserId == null || currentUserId.isEmpty()) {
            Toast.makeText(this, "Please login to comment", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create comment object
        Comment comment = new Comment(content, currentUserId, postId);

        progressBar.setVisibility(View.VISIBLE);
        sendCommentButton.setEnabled(false);

        ApiClient.getForumCommentApi().create(comment).enqueue(new Callback<ApiResponse<Comment>>() {
            @Override
            public void onResponse(Call<ApiResponse<Comment>> call, Response<ApiResponse<Comment>> response) {
                progressBar.setVisibility(View.GONE);
                sendCommentButton.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Comment> apiResponse = response.body();
                    if (apiResponse.isStatus() && apiResponse.getData() != null) {
                        Toast.makeText(PostDetailActivity.this, "Comment posted!", Toast.LENGTH_SHORT).show();
                        commentInput.setText("");

                        // Reload comments
                        loadComments();
                    } else {
                        Toast.makeText(PostDetailActivity.this,
                            "Failed to post comment: " + apiResponse.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(PostDetailActivity.this,
                        "Failed to post comment",
                        Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Comment>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                sendCommentButton.setEnabled(true);
                Toast.makeText(PostDetailActivity.this,
                    "Error: " + t.getMessage(),
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void likeComment(Comment comment, int position) {
        // Update UI optimistically
        int newLikes = comment.getLikes() + 1;
        comment.setLikes(newLikes);
        commentAdapter.updateComment(position, comment);

        // TODO: Make API call to update comment like on backend
        Toast.makeText(this, "Comment liked!", Toast.LENGTH_SHORT).show();
    }
}