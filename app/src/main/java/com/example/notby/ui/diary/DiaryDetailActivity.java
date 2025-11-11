package com.example.notby.ui.diary;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.notby.R;
import com.example.notby.data.model.ApiResponse;
import com.example.notby.data.model.DiaryEntry;
import com.example.notby.data.remote.ApiClient;
import com.example.notby.data.remote.DiaryEntriesApi;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DiaryDetailActivity extends AppCompatActivity {

    public static final String EXTRA_DIARY_ID = "extra_diary_id";

    private TextView title, date, content;
    private ImageView image;
    private Button editButton, deleteButton;
    private String diaryId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_detail);

        title = findViewById(R.id.detail_title);
        date = findViewById(R.id.detail_date);
        content = findViewById(R.id.detail_content);
        image = findViewById(R.id.detail_image);
        editButton = findViewById(R.id.edit_button);
        deleteButton = findViewById(R.id.delete_button);

        diaryId = getIntent().getStringExtra(EXTRA_DIARY_ID);
        if (diaryId != null) {
            loadDiaryEntry(diaryId);
        }

        editButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, DiaryEditActivity.class);
            intent.putExtra(DiaryEditActivity.EXTRA_DIARY_ID, diaryId);
            startActivity(intent);
        });

        deleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Xóa mục nhật ký")
                    .setMessage("Bạn có chắc chắn muốn xóa mục này không?")
                    .setPositiveButton("Xóa", (dialog, which) -> deleteDiaryEntry())
                    .setNegativeButton("Hủy", null)
                    .show();
        });
    }

    private void loadDiaryEntry(String diaryId) {
        DiaryEntriesApi apiService = ApiClient.getDiaryEntriesApi(this);
        Call<ApiResponse<DiaryEntry>> call = apiService.getDiaryEntryById(diaryId);

        call.enqueue(new Callback<ApiResponse<DiaryEntry>>() {
            @Override
            public void onResponse(Call<ApiResponse<DiaryEntry>> call, Response<ApiResponse<DiaryEntry>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    DiaryEntry diaryEntry = response.body().getData();
                    if (diaryEntry != null) {
                        title.setText(diaryEntry.getTitle());
                        date.setText(diaryEntry.getCreatedAt());
                        content.setText(diaryEntry.getContent());

                        if (diaryEntry.getImageUrls() != null && !diaryEntry.getImageUrls().isEmpty()) {
                            Glide.with(DiaryDetailActivity.this)
                                    .load(diaryEntry.getImageUrls().get(0))
                                    .into(image);
                        }
                    }
                } else {
                    Toast.makeText(DiaryDetailActivity.this, "Failed to load diary entry", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<DiaryEntry>> call, Throwable t) {
                Toast.makeText(DiaryDetailActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteDiaryEntry() {
        DiaryEntriesApi apiService = ApiClient.getDiaryEntriesApi(this);
        Call<ApiResponse<Void>> call = apiService.deleteDiaryEntry(diaryId);

        call.enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(DiaryDetailActivity.this, "Diary entry deleted successfully", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(DiaryDetailActivity.this, "Failed to delete diary entry", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                Toast.makeText(DiaryDetailActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
