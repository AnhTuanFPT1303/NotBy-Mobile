package com.example.notby.ui.diary;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.notby.R;
import com.example.notby.data.model.ApiResponse;
import com.example.notby.data.model.DiaryEntry;
import com.example.notby.data.model.MediaFile;
import com.example.notby.data.remote.ApiClient;
import com.example.notby.data.remote.DiaryEntriesApi;
import com.google.android.material.chip.Chip;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DiaryDetailActivity extends AppCompatActivity {

    public static final String EXTRA_DIARY_ID = "extra_diary_id";

    private TextView title, content;
    private ImageView image;
    private Chip dateChip, babyNameChip, categoryChip;
    private Button editButton, deleteButton;
    private String diaryId;
    private ActivityResultLauncher<Intent> editLauncher;
    private DiaryEntry mCurrentDiaryEntry;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        title = findViewById(R.id.detail_title);
        content = findViewById(R.id.detail_content);
        image = findViewById(R.id.detail_image);
        dateChip = findViewById(R.id.detail_date_chip);
        babyNameChip = findViewById(R.id.detail_baby_name_chip);
        categoryChip = findViewById(R.id.detail_category_chip);
        editButton = findViewById(R.id.edit_button);
        deleteButton = findViewById(R.id.delete_button);

        editLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    setResult(Activity.RESULT_OK);
                    if (diaryId != null) {
                        loadDiaryEntry(diaryId);
                    }
                }
            });

        diaryId = getIntent().getStringExtra(EXTRA_DIARY_ID);
        if (diaryId == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy ID của mục nhật ký.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        loadDiaryEntry(diaryId);
        
        editButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, DiaryEditActivity.class);
            intent.putExtra(DiaryEditActivity.EXTRA_DIARY_ID, diaryId);
            editLauncher.launch(intent);
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

    private void loadDiaryEntry(String id) {
        DiaryEntriesApi apiService = ApiClient.getDiaryEntriesApi(this);
        Call<ApiResponse<DiaryEntry>> call = apiService.getDiaryEntryById(id);

        call.enqueue(new Callback<ApiResponse<DiaryEntry>>() {
            @Override
            public void onResponse(Call<ApiResponse<DiaryEntry>> call, Response<ApiResponse<DiaryEntry>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    mCurrentDiaryEntry = response.body().getData();
                    title.setText(mCurrentDiaryEntry.getTitle());
                    content.setText(mCurrentDiaryEntry.getContent());
                    dateChip.setText(formatDate(mCurrentDiaryEntry.getCreatedAt()));
                    categoryChip.setText(mCurrentDiaryEntry.getCategory());

                    String childName = mCurrentDiaryEntry.getChildName();
                    babyNameChip.setText(childName);

                    if (mCurrentDiaryEntry.getImageUrls() != null && !mCurrentDiaryEntry.getImageUrls().isEmpty()) {
                        MediaFile firstImage = mCurrentDiaryEntry.getImageUrls().get(0);
                        if (firstImage != null && firstImage.getFileUrl() != null) {
                            image.setVisibility(View.VISIBLE);
                            Glide.with(DiaryDetailActivity.this)
                                    .load(firstImage.getFileUrl())
                                    .into(image);
                        } else {
                            image.setVisibility(View.GONE);
                        }
                    } else {
                        image.setVisibility(View.GONE);
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
                    setResult(RESULT_OK);
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

    private String formatDate(String dateString) {
        if (dateString == null) return "";
        try {
            SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            originalFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            SimpleDateFormat targetFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
            Date date = originalFormat.parse(dateString);
            return targetFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return dateString; // Fallback
        }
    }
}
