package com.example.notby.ui.library;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.example.notby.R;
import com.example.notby.data.TokenManager;
import com.example.notby.data.model.ApiResponse;
import com.example.notby.data.model.Article;
import com.example.notby.data.model.MediaFile;
import com.example.notby.data.remote.ApiClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.gson.JsonPrimitive;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LibraryActivity extends AppCompatActivity {
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private LibraryPagerAdapter pagerAdapter;
    private FloatingActionButton fabCreateArticle;

    private static final int REQUEST_PICK_IMAGE = 1010;
    private Uri selectedImageUri = null;
    private Button currentDialogAddImageButton = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);

        fabCreateArticle = findViewById(R.id.fabCreateArticle);

        setupToolbar();
        setupViewPager();
        setupTabs();
        setupFab();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Thư viện");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupViewPager() {
        viewPager = findViewById(R.id.viewPager);
        pagerAdapter = new LibraryPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (position == 0) {
                    fabCreateArticle.setVisibility(View.VISIBLE);
                } else {
                    fabCreateArticle.setVisibility(View.GONE);
                }
            }
        });
    }

    private void setupTabs() {
        tabLayout = findViewById(R.id.tabLayout);
        String[] tabTitles = new String[]{"Bài viết", "Video", "Chuyên gia", "Tài liệu"};

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(tabTitles[position]);
        }).attach();
    }

    private void setupFab() {
        fabCreateArticle.setOnClickListener(v -> showCreateArticleDialog());
    }

    private void showCreateArticleDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.layout_create_article);

        EditText editTextTitle = dialog.findViewById(R.id.editTextTitle);
        EditText editTextDescription = dialog.findViewById(R.id.editTextDescription);
        EditText editTextContent = dialog.findViewById(R.id.editTextContent);
        Button buttonAddImage = dialog.findViewById(R.id.buttonAddImage);
        Button buttonSubmit = dialog.findViewById(R.id.buttonSubmit);
        currentDialogAddImageButton = buttonAddImage;

        buttonAddImage.setOnClickListener(v -> pickImage());

        buttonSubmit.setOnClickListener(v -> {
            String title = editTextTitle.getText().toString();
            String description = editTextDescription.getText().toString();
            String content = editTextContent.getText().toString();

            if (title.isEmpty() || description.isEmpty() || content.isEmpty()) {
                Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            Article article = new Article();
            article.setTitle(title);
            article.setDescription(description);
            article.setContent(content);
            article.setCategoryId("68dd571d5a1978661c574b2d"); // Default category ID for "Sức khỏe"
            String authorId = getAuthorId();
            if (authorId == null) {
                Toast.makeText(LibraryActivity.this, "Không thể lấy thông tin người dùng. Vui lòng đăng nhập lại.", Toast.LENGTH_SHORT).show();
                return;
            }
            article.setAuthor(new JsonPrimitive(authorId));
            article.setLikes(0);
            article.setViews(0);
            article.setReadTime(5); // Default read time in minutes
            article.setTags(new ArrayList<>()); // Empty tags list for now

            if (selectedImageUri != null) {
                try {
                    final File uploadFile = createFileFromUri(selectedImageUri);
                    final String mimeTemp = getContentResolver().getType(selectedImageUri);
                    final String mime = mimeTemp == null ? "image/*" : mimeTemp;
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
                                    Toast.makeText(LibraryActivity.this, "Không lấy được URL ảnh", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                MediaFile mf = new MediaFile();
                                mf.setFileUrl(fileUrl);
                                mf.setFileName(uploadFile.getName());
                                mf.setFileType("image");
                                mf.setAuthor(getAuthorId());
                                ApiClient.getMediafileApi().create(mf).enqueue(new Callback<ApiResponse<MediaFile>>() {
                                    @Override
                                    public void onResponse(Call<ApiResponse<MediaFile>> call2, Response<ApiResponse<MediaFile>> resp2) {
                                        deleteTempFile(uploadFile);
                                        if (resp2.isSuccessful() && resp2.body() != null && resp2.body().isStatus()) {
                                            MediaFile created = resp2.body().getData();
                                            String fileId = created != null ? created.getId() : null;
                                            if (fileId != null) article.setFileId(fileId);
                                            createArticle(article, dialog);
                                        } else {
                                            Toast.makeText(LibraryActivity.this, "Không tạo được MediaFile", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                    @Override
                                    public void onFailure(Call<ApiResponse<MediaFile>> call2, Throwable t2) {
                                        deleteTempFile(uploadFile);
                                        Toast.makeText(LibraryActivity.this, "Lỗi tạo MediaFile: " + t2.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                deleteTempFile(uploadFile);
                                Toast.makeText(LibraryActivity.this, "Lỗi upload ảnh: " + response.message(), Toast.LENGTH_SHORT).show();
                            }
                        }
                        @Override
                        public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                            deleteTempFile(uploadFile);
                            Toast.makeText(LibraryActivity.this, "Lỗi upload: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (IOException e) {
                    Toast.makeText(this, "Lỗi file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else {
                createArticle(article, dialog);
            }
        });
        dialog.setOnDismissListener(d -> {
            selectedImageUri = null;
            currentDialogAddImageButton = null;
        });
        dialog.show();
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Chọn ảnh"), REQUEST_PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            if (currentDialogAddImageButton != null) {
                currentDialogAddImageButton.setText("Ảnh đã chọn");
            }
            Toast.makeText(this, "Đã chọn ảnh", Toast.LENGTH_SHORT).show();
        }
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

    private String extractFileUrlFromData(Object data) {
        if (data == null) return null;
        if (data instanceof String) return (String) data;
        if (data instanceof java.util.Map) {
            java.util.Map<?, ?> map = (java.util.Map<?, ?>) data;
            if (map.containsKey("url")) return String.valueOf(map.get("url"));
            if (map.containsKey("secure_url")) return String.valueOf(map.get("secure_url"));
            if (map.containsKey("data")) {
                Object inner = map.get("data");
                if (inner instanceof String) return (String) inner;
                if (inner instanceof java.util.Map) {
                    java.util.Map<?, ?> innerMap = (java.util.Map<?, ?>) inner;
                    if (innerMap.containsKey("url")) return String.valueOf(innerMap.get("url"));
                }
            }
        }
        return null;
    }

    private String getAuthorId() {
        TokenManager tokenManager = new TokenManager(this);
        return tokenManager.getUserId();
    }

    private void createArticle(Article article, Dialog dialog) {
        ApiClient.getArticleApi().createArticle(article).enqueue(new Callback<ApiResponse<Article>>() {
            @Override
            public void onResponse(Call<ApiResponse<Article>> call, Response<ApiResponse<Article>> response) {
                try {
                    if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                        try {
                            // Switch to Articles tab
                            if (viewPager != null) {
                                viewPager.setCurrentItem(0, false);
                            }

                            // Try to directly call addArticle on the fragment if present
                            androidx.fragment.app.Fragment target = null;
                            for (androidx.fragment.app.Fragment f : getSupportFragmentManager().getFragments()) {
                                if (f instanceof LibraryContentFragment) {
                                    androidx.fragment.app.Fragment frag = f;
                                    android.os.Bundle args = frag.getArguments();
                                    if (args != null && args.getInt("position", -1) == 0) {
                                        target = f;
                                        break;
                                    }
                                }
                            }
                            com.example.notby.data.model.Article created = null;
                            if (response.body() != null) {
                                created = response.body().getData();
                            }
                            if (target != null && created != null) {
                                try {
                                    ((LibraryContentFragment) target).addArticle(created);
                                    android.util.Log.d("LibraryActivity", "Directly added created article id=" + (created.getId() != null ? created.getId() : "<no-id>") + " to fragment");
                                } catch (Exception ignored) { }
                            }

                            // Send a fragment result that any LibraryContentFragment can listen to and react by reloading articles.
                            try {
                                androidx.fragment.app.FragmentManager fm = getSupportFragmentManager();
                                android.os.Bundle resultBundle = new android.os.Bundle();
                                resultBundle.putBoolean("refresh", true);
                                if (created != null) {
                                    try {
                                        com.google.gson.Gson gson = new com.google.gson.Gson();
                                        resultBundle.putString("created_article_json", gson.toJson(created));
                                    } catch (Exception se) {
                                        // ignore serialization issues
                                    }
                                }
                                fm.setFragmentResult("article_created", resultBundle);
                                // Schedule a delayed retry in case fragment wasn't ready to receive the result.
                                android.os.Handler h = new android.os.Handler(android.os.Looper.getMainLooper());
                                h.postDelayed(() -> {
                                    try {
                                        fm.setFragmentResult("article_created", resultBundle);
                                    } catch (Exception retryEx) {
                                        // ignore
                                    }
                                }, 700);
                            } catch (Exception inner) {
                                // ignore
                            }
                        } catch (Exception innerOuter) {
                            // ignore
                        }
                        Toast.makeText(LibraryActivity.this, "Tạo bài viết thành công", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(LibraryActivity.this, "Lỗi khi tạo bài viết", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception ex) {
                    Toast.makeText(LibraryActivity.this, "Lỗi nội bộ: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Article>> call, Throwable t) {
                Toast.makeText(LibraryActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
