package com.example.notby.ui.library;

import android.app.Dialog;
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
import com.example.notby.data.remote.ApiClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.gson.JsonPrimitive;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LibraryActivity extends AppCompatActivity {
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private LibraryPagerAdapter pagerAdapter;
    private FloatingActionButton fabCreateArticle;

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
            article.setAuthor(new JsonPrimitive(getAuthorId()));
            article.setLikes(0);
            article.setViews(0);
            article.setReadTime(5); // Default read time in minutes
            article.setTags(new ArrayList<>()); // Empty tags list for now

            createArticle(article, dialog);
        });

        dialog.show();
    }

    private String getAuthorId() {
        TokenManager tokenManager = new TokenManager(this);
        return tokenManager.getUserIdFromToken();
    }

    private void createArticle(Article article, Dialog dialog) {
        ApiClient.getArticleApi().createArticle(article).enqueue(new Callback<ApiResponse<Article>>() {
            @Override
            public void onResponse(Call<ApiResponse<Article>> call, Response<ApiResponse<Article>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    Toast.makeText(LibraryActivity.this, "Tạo bài viết thành công", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    // Reload the list in the fragment
                    pagerAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(LibraryActivity.this, "Lỗi khi tạo bài viết", Toast.LENGTH_SHORT).show();
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
