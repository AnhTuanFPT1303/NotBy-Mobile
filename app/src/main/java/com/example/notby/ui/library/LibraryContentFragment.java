package com.example.notby.ui.library;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notby.R;
import com.example.notby.data.model.ApiResponse;
import com.example.notby.data.model.Article;
import com.example.notby.data.model.MediaFile;
import com.example.notby.data.model.User;
import com.example.notby.data.remote.ApiClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LibraryContentFragment extends Fragment {
    private static final String ARG_POSITION = "position";
    private int position;
    private RecyclerView recyclerView;
    private LibraryAdapter adapter;
    private boolean pendingReload = false;
    private java.util.List<com.example.notby.data.model.Article> pendingAddedArticles = new java.util.ArrayList<>();
    // Buffer of locally created articles to merge with server results to avoid overwriting them
    private java.util.List<com.example.notby.data.model.Article> createdArticlesBuffer = new java.util.ArrayList<>();

    // Video upload functionality
    private VideoUploadManager videoUploadManager;
    private ActivityResultLauncher<Intent> videoPickerLauncher;
    private Uri selectedVideoUri;
    private String selectedVideoName;

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

        // Initialize video upload functionality
        videoUploadManager = new VideoUploadManager(requireContext());
        videoPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        selectedVideoUri = uri;
                        selectedVideoName = getFileName(uri);
                        // Notify that video was selected
                        Bundle resultBundle = new Bundle();
                        resultBundle.putBoolean("selected", true);
                        getParentFragmentManager().setFragmentResult("video_selected", resultBundle);
                    }
                }
            }
        );

        // Ensure the listener is set only once
        getParentFragmentManager().setFragmentResultListener("article_created", this, (requestKey, bundle) -> {
            boolean refresh = bundle.getBoolean("refresh", false);
            if (refresh && position == 0) {
                // If created article JSON exists, try to deserialize and add it immediately
                String createdJson = bundle.getString("created_article_json", null);
                if (createdJson != null) {
                    try {
                        com.google.gson.Gson gson = new com.google.gson.Gson();
                        com.example.notby.data.model.Article created = gson.fromJson(createdJson, com.example.notby.data.model.Article.class);
                        if (created != null) addArticle(created);
                    } catch (Exception e) {
                        // ignore JSON parsing issues and fallback to full reload
                        reloadArticles();
                    }
                } else {
                    reloadArticles();
                }
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_library_content, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        FloatingActionButton fabUploadVideo = view.findViewById(R.id.fabUploadVideo);

        setupRecyclerView();
        adapter.setOnVideoClickListener(video -> openVideo(video));
        adapter.setOnDocumentClickListener(document -> downloadDocument(document));
        adapter.setOnExpertClickListener(expert -> openExpertProfile(expert));

        // Setup FAB for video upload (only show in video tab)
        if (position == 1) {
            fabUploadVideo.setVisibility(View.VISIBLE);
            fabUploadVideo.setOnClickListener(v -> showVideoUploadDialog());
        } else {
            fabUploadVideo.setVisibility(View.GONE);
        }

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
        // If a reload request arrived before the view was created, perform it now
        if (pendingReload) {
            pendingReload = false;
            loadArticles();
        }
        // If any articles were added before adapter initialization, insert them now
        if (!pendingAddedArticles.isEmpty()) {
            for (int i = pendingAddedArticles.size() - 1; i >= 0; i--) {
                com.example.notby.data.model.Article a = pendingAddedArticles.get(i);
                adapter.addArticleAtFront(a);
                createdArticlesBuffer.add(0, a);
            }
            pendingAddedArticles.clear();
        }
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
                adapter.setOnArticleClickListener(null);
                loadExperts();
                break;
            case 3: // Documents
                adapter.setOnArticleClickListener(null);
                loadDocuments();
                break;
        }
    }

    private void loadArticles() {
        ApiClient.getArticleApi().getAllArticles().enqueue(new Callback<ApiResponse<List<Article>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Article>>> call, Response<ApiResponse<List<Article>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    java.util.List<Article> serverList = response.body().getData();
                    if (serverList == null) serverList = new java.util.ArrayList<>();
                    // Merge local created articles on top if not already present
                    if (!createdArticlesBuffer.isEmpty()) {
                        for (int i = createdArticlesBuffer.size() - 1; i >= 0; i--) {
                            Article created = createdArticlesBuffer.get(i);
                            boolean exists = false;
                            if (created.getId() != null) {
                                for (Article s : serverList) {
                                    if (created.getId().equals(s.getId())) { exists = true; break; }
                                }
                            }
                            if (!exists) serverList.add(0, created);
                        }
                        // Clear the buffer after merging
                        createdArticlesBuffer.clear();
                    }
                    adapter.setArticles(serverList);
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

    private void loadDocuments() {
        ApiClient.getMediafileApi().findByFileType("other").enqueue(new Callback<ApiResponse<List<MediaFile>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<MediaFile>>> call, Response<ApiResponse<List<MediaFile>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setDocuments(response.body().getData());
                } else {
                    Toast.makeText(getContext(), "Lỗi khi tải tài liệu", Toast.LENGTH_SHORT).show();
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

    private void loadExperts() {
        ApiClient.getUserApi().getByRole("Admin").enqueue(new Callback<ApiResponse<List<User>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<User>>> call, Response<ApiResponse<List<User>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<User> experts = response.body().getData();
                    if (experts != null) {
                        adapter.setExperts(experts);
                    } else {
                        adapter.setExperts(new ArrayList<>());
                    }
                } else {
                    Toast.makeText(getContext(), "Lỗi khi tải danh sách chuyên gia", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<User>>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openExpertProfile(User expert) {
        // For now, show a simple toast with expert information
        // This can be expanded to open a detailed profile or contact dialog
        String message = String.format("Chuyên gia: %s\nEmail: %s\nVai trò: %s",
            expert.getUsername(), expert.getEmail(), expert.getRole());
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }

    private void downloadDocument(MediaFile document) {
        if (document == null || document.getFileUrl() == null) {
            Toast.makeText(getContext(), "Tệp không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = document.getFileUrl();
        // Try to open in external app/browser first
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception e) {
            // Fallback to DownloadManager to save the file
            try {
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                request.setTitle(document.getFileName());
                request.setDescription("Downloading document...");
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, document.getFileName());

                Context ctx = getContext();
                if (ctx != null) {
                    DownloadManager dm = (DownloadManager) ctx.getSystemService(Context.DOWNLOAD_SERVICE);
                    if (dm != null) dm.enqueue(request);
                }
                Toast.makeText(getContext(), "Đã bắt đầu tải xuống", Toast.LENGTH_SHORT).show();
            } catch (Exception ex) {
                Toast.makeText(getContext(), "Không thể tải xuống tệp: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Public method to allow the activity to request articles reload.
     * Only reloads when this fragment represents the Articles tab (position == 0).
     */
    public void reloadArticles() {
        if (position != 0) return;
        // If adapter or view not ready, set pending flag so it reloads in onCreateView/setupRecyclerView
        if (adapter == null || getView() == null) {
            pendingReload = true;
            return;
        }
        loadArticles();
    }

    /**
     * Add a newly created article to the top of the list. If adapter isn't ready, queue it.
     */
    public void addArticle(com.example.notby.data.model.Article article) {
        if (article == null || position != 0) return;
        if (adapter == null || getView() == null) {
            pendingAddedArticles.add(article);
            return;
        }
        // ensure we run on UI thread
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                android.util.Log.d("LibraryContentFragment", "Adding article id=" + (article.getId() != null ? article.getId() : "<no-id>"));
                // Check if the article already exists in the adapter
                if (adapter != null && !adapter.containsArticle(article)) {
                    adapter.addArticleAtFront(article);
                    createdArticlesBuffer.add(0, article);
                }
                if (recyclerView != null) recyclerView.scrollToPosition(0);
            });
        } else {
            android.util.Log.d("LibraryContentFragment", "Adding article (no activity) id=" + (article.getId() != null ? article.getId() : "<no-id>"));
        }
    }

    private void showVideoUploadDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_upload_video, null);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create();

        // Get dialog controls
        android.widget.Button btnSelectVideo = dialogView.findViewById(R.id.btnSelectVideo);
        android.widget.TextView tvSelectedFile = dialogView.findViewById(R.id.tvSelectedFile);
        android.widget.ProgressBar progressBar = dialogView.findViewById(R.id.progressBar);
        android.widget.TextView tvProgress = dialogView.findViewById(R.id.tvProgress);
        android.widget.Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        android.widget.Button btnUpload = dialogView.findViewById(R.id.btnUpload);

        // Reset state
        selectedVideoUri = null;
        selectedVideoName = null;

        btnSelectVideo.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("video/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            videoPickerLauncher.launch(Intent.createChooser(intent, "Chọn video"));
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnUpload.setOnClickListener(v -> {
            if (selectedVideoUri != null && selectedVideoName != null) {
                btnUpload.setEnabled(false);
                btnSelectVideo.setEnabled(false);
                progressBar.setVisibility(View.VISIBLE);
                tvProgress.setVisibility(View.VISIBLE);

                videoUploadManager.uploadVideo(selectedVideoUri, selectedVideoName, new VideoUploadManager.VideoUploadCallback() {
                    @Override
                    public void onUploadStart() {
                        requireActivity().runOnUiThread(() -> {
                            tvProgress.setText("Bắt đầu tải lên...");
                        });
                    }

                    @Override
                    public void onUploadProgress(String message) {
                        requireActivity().runOnUiThread(() -> {
                            tvProgress.setText(message);
                        });
                    }

                    @Override
                    public void onUploadSuccess(MediaFile mediaFile) {
                        requireActivity().runOnUiThread(() -> {
                            dialog.dismiss();
                            Toast.makeText(getContext(), "Tải lên video thành công!", Toast.LENGTH_SHORT).show();
                            // Refresh the videos list
                            loadVideos();
                        });
                    }

                    @Override
                    public void onUploadError(String error) {
                        requireActivity().runOnUiThread(() -> {
                            btnUpload.setEnabled(true);
                            btnSelectVideo.setEnabled(true);
                            progressBar.setVisibility(View.GONE);
                            tvProgress.setVisibility(View.GONE);
                            Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_LONG).show();
                        });
                    }
                });
            }
        });

        // Listen for video selection result
        getParentFragmentManager().setFragmentResultListener("video_selected", this, (requestKey, bundle) -> {
            if (selectedVideoUri != null && selectedVideoName != null) {
                tvSelectedFile.setText("Đã chọn: " + selectedVideoName);
                tvSelectedFile.setVisibility(View.VISIBLE);
                btnUpload.setEnabled(true);
            }
        });

        dialog.show();

        // Trigger result listener when video is selected
        if (selectedVideoUri != null && selectedVideoName != null) {
            tvSelectedFile.setText("Đã chọn: " + selectedVideoName);
            tvSelectedFile.setVisibility(View.VISIBLE);
            btnUpload.setEnabled(true);
        }
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (android.database.Cursor cursor = requireContext().getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                    if (nameIndex >= 0) {
                        result = cursor.getString(nameIndex);
                    }
                }
            } catch (Exception e) {
                // Ignore
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result != null ? result : "video_" + System.currentTimeMillis() + ".mp4";
    }
}

