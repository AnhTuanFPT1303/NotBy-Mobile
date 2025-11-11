package com.example.notby.ui.diary;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.notby.R;
import com.example.notby.data.model.ApiResponse;
import com.example.notby.data.model.DiaryEntry;
import com.example.notby.data.remote.ApiClient;
import com.example.notby.data.remote.DiaryEntriesApi;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DiaryEditActivity extends AppCompatActivity {

    public static final String EXTRA_DIARY_ID = "extra_diary_id";

    private TextInputEditText titleEditText, descriptionEditText;
    private AutoCompleteTextView categoryAutoComplete;
    private Button saveButton;
    private String diaryId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_edit);

        titleEditText = findViewById(R.id.edit_title_edit_text);
        descriptionEditText = findViewById(R.id.edit_description_edit_text);
        categoryAutoComplete = findViewById(R.id.edit_category_auto_complete);
        saveButton = findViewById(R.id.edit_save_button);

        // Set up category dropdown
        String[] categories = {"Vận động", "Ngôn ngữ", "Dinh dưỡng", "Xã hội", "Khác"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, categories);
        categoryAutoComplete.setAdapter(adapter);

        diaryId = getIntent().getStringExtra(EXTRA_DIARY_ID);
        if (diaryId != null) {
            loadDiaryEntry(diaryId);
        }

        saveButton.setOnClickListener(v -> updateDiaryEntry());
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
                        titleEditText.setText(diaryEntry.getTitle());
                        descriptionEditText.setText(diaryEntry.getContent());
                        categoryAutoComplete.setText(diaryEntry.getCategory(), false);
                    }
                } else {
                    Toast.makeText(DiaryEditActivity.this, "Failed to load diary entry", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<DiaryEntry>> call, Throwable t) {
                Toast.makeText(DiaryEditActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateDiaryEntry() {
        String title = titleEditText.getText().toString();
        String description = descriptionEditText.getText().toString();
        String category = categoryAutoComplete.getText().toString();

        if (title.isEmpty() || category.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        DiaryEntry diaryEntry = new DiaryEntry();
        diaryEntry.setTitle(title);
        diaryEntry.setContent(description);
        diaryEntry.setCategory(category);

        DiaryEntriesApi apiService = ApiClient.getDiaryEntriesApi(this);
        Call<ApiResponse<DiaryEntry>> call = apiService.updateDiaryEntry(diaryId, diaryEntry);

        call.enqueue(new Callback<ApiResponse<DiaryEntry>>() {
            @Override
            public void onResponse(Call<ApiResponse<DiaryEntry>> call, Response<ApiResponse<DiaryEntry>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(DiaryEditActivity.this, "Diary entry updated successfully", Toast.LENGTH_SHORT).show();
                    finish(); // Close the activity
                } else {
                    Toast.makeText(DiaryEditActivity.this, "Failed to update diary entry", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<DiaryEntry>> call, Throwable t) {
                Toast.makeText(DiaryEditActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
