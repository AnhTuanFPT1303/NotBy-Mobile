package com.example.notby.ui.forumpost;

import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notby.R;
import com.example.notby.data.model.ApiResponse;
import com.example.notby.data.model.ForumPost;
import com.example.notby.data.remote.ApiClient;

import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForumPostActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ForumPostAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forum_post);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ForumPostAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        loadForumPosts();
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
