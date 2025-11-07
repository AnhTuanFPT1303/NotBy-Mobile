package com.example.notby.ui.library;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notby.R;
import com.example.notby.data.model.ApiResponse;
import com.example.notby.data.model.Article;
import com.example.notby.data.model.MediaFile;
import com.example.notby.data.remote.ApiClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LibraryContentFragment extends Fragment {
    private static final String ARG_POSITION = "position";
    private int position;
    private RecyclerView recyclerView;
    private LibraryAdapter adapter;

    public static LibraryContentFragment newInstance(int position) {
        LibraryContentFragment fragment = new LibraryContentFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            position = getArguments().getInt(ARG_POSITION);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_library_content, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        setupRecyclerView();
        adapter.setOnVideoClickListener(video -> openVideo(video));
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadContent();
    }

    private void setupRecyclerView() {
        adapter = new LibraryAdapter();
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerView.setAdapter(adapter);
    }

    private void loadContent() {
        switch (position) {
            case 0: // Articles
                adapter.setOnArticleClickListener(null); // Only enable article click for articles
                loadArticles();
                break;
            case 1: // Videos
                adapter.setOnArticleClickListener(null);
                loadVideos();
                break;
            case 2: // Experts
                // TODO: Implement experts loading
                break;
            case 3: // Documents
                // TODO: Implement documents loading
                break;
        }
    }

    private void loadArticles() {
        ApiClient.getArticleApi().getAllArticles().enqueue(new Callback<ApiResponse<List<Article>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Article>>> call, Response<ApiResponse<List<Article>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setArticles(response.body().getData());
                } else {
                    Toast.makeText(getContext(), "Lỗi khi tải bài viết", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Article>>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadVideos() {
        ApiClient.getMediafileApi().findByFileType("video").enqueue(new Callback<ApiResponse<List<MediaFile>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<MediaFile>>> call, Response<ApiResponse<List<MediaFile>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setVideos(response.body().getData());
                } else {
                    Toast.makeText(getContext(), "Lỗi khi tải video", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<MediaFile>>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openVideo(MediaFile video) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(video.getFileUrl()), "video/*");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
