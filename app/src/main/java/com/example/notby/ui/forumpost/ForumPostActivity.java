package com.example.notby.ui.forumpost;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
// Import cần thiết cho Menu
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notby.R;
import com.example.notby.data.TokenManager;
import com.example.notby.data.model.ApiResponse;
import com.example.notby.data.model.ForumPost;
import com.example.notby.data.model.MediaFile;
import com.example.notby.data.remote.ApiClient;
import com.example.notby.ui.dashboard.DashboardActivity;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForumPostActivity extends AppCompatActivity {

    private static final String TAG = "ForumPostActivity";
    private static final int REQUEST_PICK_FILE = 1001;

    private RecyclerView recyclerView;
    private ForumPostAdapter adapter;

    // New UI elements
    private EditText newPostTitle;
    private EditText newPostContent;
    private Button attachImageButton;
    private Button postButton;

    private Uri selectedFileUri = null;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forum_post);

        // Initialize TokenManager
        tokenManager = new TokenManager(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Thêm nút quay lại (Back) trên Toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ForumPostAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        // wire new UI
        newPostTitle = findViewById(R.id.newPostTitle);
        newPostContent = findViewById(R.id.newPostContent);
        attachImageButton = findViewById(R.id.attachImageButton);
        postButton = findViewById(R.id.postButton);

        attachImageButton.setOnClickListener(v -> pickFile());
        postButton.setOnClickListener(v -> handlePostSubmit());

        loadForumPosts();
    }

    // ===================================================================
    // BƯỚC 1: NẠP FILE MENU VÀO TOOLBAR (PHẦN BẠN BỊ THIẾU)
    // ===================================================================
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.drawer_nav_menu, menu); // Dùng file menu của bạn
        return true; // Trả về true để menu được hiển thị
    }

    // ===================================================================
    // BƯỚC 2: XỬ LÝ KHI CLICK VÀO TỪNG MỤC MENU (PHẦN BẠN ĐÃ CÓ)
    // ===================================================================
    // Trong file ForumPostActivity.java

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        // Xử lý nút quay lại (nút home/up)
        if (itemId == android.R.id.home) {
            finish(); // Đóng activity hiện tại và quay về màn hình trước
            return true;
        }

        // ======================================================
        // === PHẦN SỬA LỖI - THAY TOAST BẰNG INTENT ===
        // ======================================================

        if (itemId == R.id.nav_overview) {
            // TẠO Ý ĐỊNH (INTENT) ĐỂ MỞ DASHBOARD ACTIVITY
            Intent intent = new Intent(ForumPostActivity.this, DashboardActivity.class);
            startActivity(intent); // BẮT ĐẦU ACTIVITY MỚI
            return true; // Trả về true để báo rằng sự kiện đã được xử lý

        } else if (itemId == R.id.nav_stats) {
            // Tương tự, nếu bạn có StatsActivity
            // Intent intent = new Intent(ForumPostActivity.this, StatsActivity.class);
            // startActivity(intent);
            Toast.makeText(this, "Chức năng Thống kê sẽ được phát triển sau!", Toast.LENGTH_SHORT).show();
            return true;


        } else if (itemId == R.id.nav_development_diary) {
            // Nếu bạn có DevelopmentDiaryActivity
            // Intent intent = new Intent(ForumPostActivity.this, DevelopmentDiaryActivity.class);
            // startActivity(intent);
            Toast.makeText(this, "Chức năng Nhật ký sẽ được phát triển sau!", Toast.LENGTH_SHORT).show();
            return true;

        } else if (itemId == R.id.nav_health_tracking) {
            // Nếu bạn có HealthTrackingActivity
            // Intent intent = new Intent(ForumPostActivity.this, HealthTrackingActivity.class);
            // startActivity(intent);
            Toast.makeText(this, "Chức năng Theo dõi sức khỏe sẽ được phát triển sau!", Toast.LENGTH_SHORT).show();
            return true;
        }
        // ... các mục menu khác

        return super.onOptionsItemSelected(item);
    }

    // ===================================================================
    // CÁC HÀM CÒN LẠI CỦA BẠN (GIỮ NGUYÊN)
    // ===================================================================

    private void pickFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Select file"), REQUEST_PICK_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_FILE && resultCode == RESULT_OK && data != null) {
            selectedFileUri = data.getData();
            if (selectedFileUri != null) {
                attachImageButton.setText("File selected");
                Toast.makeText(this, "File selected", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void handlePostSubmit() {
        final String title = newPostTitle.getText() != null ? newPostTitle.getText().toString().trim() : "";
        final String content = newPostContent.getText() != null ? newPostContent.getText().toString().trim() : "";

        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "Tiêu đề và nội dung không được để trống", Toast.LENGTH_SHORT).show();
            return;
        }

        postButton.setEnabled(false);
        postButton.setText("Đang đăng...");

        if (selectedFileUri != null) {
            try {
                final File uploadFile = createFileFromUri(selectedFileUri);
                final String mimeTemp = getContentResolver().getType(selectedFileUri);
                final String mime = mimeTemp == null ? "application/octet-stream" : mimeTemp;

                RequestBody reqFile = RequestBody.create(MediaType.parse(mime), uploadFile);
                MultipartBody.Part body = MultipartBody.Part.createFormData("file", uploadFile.getName(), reqFile);

                ApiClient.getCloudinaryApi().uploadFile(body).enqueue(new Callback<ApiResponse<Object>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                            Object data = response.body().getData();
                            String fileUrl = extractFileUrlFromData(data);
                            if (fileUrl == null) {
                                deleteTempFile(uploadFile);
                                onFailure(call, new Throwable("Unable to extract file URL from cloudinary response"));
                                return;
                            }

                            MediaFile mf = new MediaFile();
                            mf.setFileUrl(fileUrl);
                            mf.setFileName(uploadFile.getName());
                            mf.setFileType(determineFileType(mime));
                            mf.setAuthor(getAuthorId());

                            ApiClient.getMediafileApi().create(mf).enqueue(new Callback<ApiResponse<MediaFile>>() {
                                @Override
                                public void onResponse(Call<ApiResponse<MediaFile>> call2, Response<ApiResponse<MediaFile>> resp2) {
                                    deleteTempFile(uploadFile);
                                    if (resp2.isSuccessful() && resp2.body() != null && resp2.body().isStatus()) {
                                        MediaFile created = resp2.body().getData();
                                        String fileId = created != null ? created.getId() : null;
                                        createForumPostAfterFile(title, content, fileId);
                                    } else {
                                        Toast.makeText(ForumPostActivity.this, "Failed to create media file", Toast.LENGTH_SHORT).show();
                                        resetPostButton();
                                    }
                                }

                                @Override
                                public void onFailure(Call<ApiResponse<MediaFile>> call2, Throwable t2) {
                                    deleteTempFile(uploadFile);
                                    Toast.makeText(ForumPostActivity.this, "Media file creation failed: " + t2.getMessage(), Toast.LENGTH_SHORT).show();
                                    resetPostButton();
                                }
                            });

                        } else {
                            deleteTempFile(uploadFile);
                            Toast.makeText(ForumPostActivity.this, "Cloud upload failed: " + response.message(), Toast.LENGTH_SHORT).show();
                            resetPostButton();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                        deleteTempFile(uploadFile);
                        Toast.makeText(ForumPostActivity.this, "Upload failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        resetPostButton();
                    }
                });

            } catch (IOException e) {
                Toast.makeText(this, "File error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                resetPostButton();
            }
        } else {
            createForumPostAfterFile(title, content, null);
        }
    }

    private void deleteTempFile(File file) {
        if (file == null) return;
        try {
            if (file.exists()) {
                if (!file.delete()) {
                    file.deleteOnExit();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createForumPostAfterFile(String title, String content, String fileId) {
        String authorId = getAuthorId();
        if (authorId == null || authorId.isEmpty()) {
            Toast.makeText(this, "Please log in to create a post", Toast.LENGTH_SHORT).show();
            resetPostButton();
            return;
        }

        ForumPost post = new ForumPost(title, content, authorId);
        if (fileId != null) post.setFileElement(new com.google.gson.JsonPrimitive(fileId));

        ApiClient.getForumPostApi().create(post).enqueue(new Callback<ApiResponse<ForumPost>>() {
            @Override
            public void onResponse(Call<ApiResponse<ForumPost>> call, Response<ApiResponse<ForumPost>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    newPostTitle.setText("");
                    newPostContent.setText("");
                    selectedFileUri = null;
                    attachImageButton.setText("Ảnh");
                    loadForumPosts();
                    Toast.makeText(ForumPostActivity.this, "Đăng bài thành công", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ForumPostActivity.this, "Post creation failed: " + response.message(), Toast.LENGTH_SHORT).show();
                }
                resetPostButton();
            }

            @Override
            public void onFailure(Call<ApiResponse<ForumPost>> call, Throwable t) {
                Toast.makeText(ForumPostActivity.this, "Post creation failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                resetPostButton();
            }
        });
    }

    private void resetPostButton() {
        postButton.setEnabled(true);
        postButton.setText("Đăng bài");
    }

    private String extractFileUrlFromData(Object data) {
        if (data == null) return null;
        if (data instanceof String) return (String) data;
        if (data instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) data;
            if (map.containsKey("url")) return String.valueOf(map.get("url"));
            if (map.containsKey("secure_url")) return String.valueOf(map.get("secure_url"));
            if (map.containsKey("data")) {
                Object inner = map.get("data");
                if (inner instanceof String) return (String) inner;
                if (inner instanceof Map) {
                    Map<?, ?> innerMap = (Map<?, ?>) inner;
                    if (innerMap.containsKey("url")) return String.valueOf(innerMap.get("url"));
                }
            }
        }
        return null;
    }

    private String determineFileType(String mime) {
        if (mime == null) return "other";
        if (mime.startsWith("image/")) return "image";
        if (mime.startsWith("video/")) return "video";
        return "other";
    }

    private String getAuthorId() {
        String userId = tokenManager.getUserId();
        Log.d(TAG, "getAuthorId() - Retrieved userId: " + userId);
        if (userId == null || userId.isEmpty()) {
            Log.e(TAG, "getAuthorId() - User ID is null or empty!");
            return null;
        }
        Log.d(TAG, "getAuthorId() - Returning userId: " + userId);
        return userId;
    }

    private File createFileFromUri(Uri uri) throws IOException {
        InputStream is = getContentResolver().openInputStream(uri);
        if (is == null) throw new IOException("Unable to open input stream");
        String fileName = "upload";
        String[] parts = uri.getLastPathSegment() != null ? uri.getLastPathSegment().split("/") : null;
        if (parts != null && parts.length > 0) fileName = parts[parts.length - 1];

        File temp = File.createTempFile("upload_", fileName, getCacheDir());
        OutputStream os = new FileOutputStream(temp);
        byte[] buffer = new byte[8192];
        int len;
        while ((len = is.read(buffer)) > 0) {
            os.write(buffer, 0, len);
        }
        os.flush();
        os.close();
        is.close();
        return temp;
    }

    private void loadForumPosts() {
        ApiClient.getForumPostApi().getAll().enqueue(new Callback<ApiResponse<List<ForumPost>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<ForumPost>>> call, Response<ApiResponse<List<ForumPost>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<ForumPost>> apiResponse = response.body();
                    if (apiResponse.getData() != null) {
                        adapter.updatePosts(apiResponse.getData());
                    } else {
                        Toast.makeText(ForumPostActivity.this,
                                "No posts available",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ForumPostActivity.this,
                            "Error loading posts: " + response.message(),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<ForumPost>>> call, Throwable t) {
                Toast.makeText(ForumPostActivity.this,
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
