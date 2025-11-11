package com.example.notby.ui.diary;

import android.os.Bundle;
import android.util.Log;
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

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DiaryEditActivity extends AppCompatActivity {

    public static final String EXTRA_DIARY_ID = "extra_diary_id";

    private TextInputEditText titleEditText, descriptionEditText;
    private AutoCompleteTextView categoryAutoComplete;
    private Button saveButton;
    private String diaryId;
    private DiaryEntry mCurrentDiaryEntry; // To hold the loaded entry

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_edit);

        titleEditText = findViewById(R.id.edit_title_edit_text);
        descriptionEditText = findViewById(R.id.edit_description_edit_text);
        categoryAutoComplete = findViewById(R.id.edit_category_auto_complete);
        saveButton = findViewById(R.id.edit_save_button);

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
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    mCurrentDiaryEntry = response.body().getData();
                    if (mCurrentDiaryEntry != null) {
                        titleEditText.setText(mCurrentDiaryEntry.getTitle());
                        descriptionEditText.setText(mCurrentDiaryEntry.getContent());
                        categoryAutoComplete.setText(mCurrentDiaryEntry.getCategory(), false);
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
        if (mCurrentDiaryEntry == null) {
            Toast.makeText(this, "Cannot save, original entry not loaded.", Toast.LENGTH_SHORT).show();
            return;
        }

        String title = titleEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        String category = categoryAutoComplete.getText().toString().trim();

        if (title.isEmpty() || category.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Modify the existing entry
        mCurrentDiaryEntry.setTitle(title);
        mCurrentDiaryEntry.setContent(description);
        mCurrentDiaryEntry.setCategory(category.toLowerCase(Locale.ROOT)); // FIX: Convert to lowercase

        // Before sending, ensure childId is just the ID string, not the populated object.
        String childIdString = mCurrentDiaryEntry.getChildIdString();
        mCurrentDiaryEntry.setChildId(childIdString);

        DiaryEntriesApi apiService = ApiClient.getDiaryEntriesApi(this);
        Call<ApiResponse<DiaryEntry>> call = apiService.updateDiaryEntry(diaryId, mCurrentDiaryEntry);

        saveButton.setEnabled(false);

        call.enqueue(new Callback<ApiResponse<DiaryEntry>>() {
            @Override
            public void onResponse(Call<ApiResponse<DiaryEntry>> call, Response<ApiResponse<DiaryEntry>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(DiaryEditActivity.this, "Diary entry updated successfully", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK); 
                    finish();
                } else {
                    saveButton.setEnabled(true);
                    Toast.makeText(DiaryEditActivity.this, "Failed to update diary entry", Toast.LENGTH_SHORT).show();
                     Log.e("DIARY_UPDATE", "Update failed with code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<DiaryEntry>> call, Throwable t) {
                saveButton.setEnabled(true);
                Toast.makeText(DiaryEditActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("DIARY_UPDATE", "Update failed on network", t);
            }
        });
    }
}
