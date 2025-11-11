package com.example.notby.ui.diary;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.notby.R;
import com.example.notby.data.TokenManager;
import com.example.notby.data.model.ApiResponse;
import com.example.notby.data.model.DiaryEntry;
import com.example.notby.data.remote.ApiClient;
import com.example.notby.data.remote.DiaryEntriesApi;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DiaryAddFragment extends Fragment {

    private TextInputEditText titleEditText, descriptionEditText;
    private AutoCompleteTextView categoryAutoComplete;
    private Button saveButton;
    private TokenManager tokenManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        try {
            Log.d("DiaryAddFragment", "onCreateView started");
            View view = inflater.inflate(R.layout.fragment_diary_add, container, false);

            titleEditText = view.findViewById(R.id.title_edit_text);
            descriptionEditText = view.findViewById(R.id.description_edit_text);
            categoryAutoComplete = view.findViewById(R.id.category_auto_complete);
            saveButton = view.findViewById(R.id.save_button);

            if (titleEditText == null || descriptionEditText == null ||
                categoryAutoComplete == null || saveButton == null) {
                Log.e("DiaryAddFragment", "One or more views not found in layout");
                return view;
            }

            tokenManager = new TokenManager(requireContext());
            Log.d("DiaryAddFragment", "TokenManager initialized");

            // Set up category dropdown
            String[] categories = {"Vận động", "Ngôn ngữ", "Dinh dưỡng", "Xã hội", "Khác"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, categories);
            categoryAutoComplete.setAdapter(adapter);

            saveButton.setOnClickListener(v -> createDiaryEntry());

            return view;
        } catch (Exception e) {
            Log.e("DiaryAddFragment", "Exception in onCreateView: " + e.getMessage(), e);
            // Return a simple view to prevent complete crash
            return inflater.inflate(android.R.layout.simple_list_item_1, container, false);
        }
    }

    private void createDiaryEntry() {
        try {
            Log.d("DiaryAddFragment", "createDiaryEntry started");

            String title = titleEditText != null ? titleEditText.getText().toString().trim() : "";
            String description = descriptionEditText != null ? descriptionEditText.getText().toString().trim() : "";
            String category = categoryAutoComplete != null ? categoryAutoComplete.getText().toString().trim() : "";
            String childId = tokenManager.getChildId();

            Log.d("DiaryAddFragment", "Form data - Title: " + title + ", Category: " + category + ", ChildId: " + childId);

            if (title.isEmpty() || category.isEmpty() || childId == null) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            DiaryEntry diaryEntry = new DiaryEntry();
            diaryEntry.setTitle(title);
            diaryEntry.setContent(description);
            diaryEntry.setCategory(category);
            diaryEntry.setChildId(childId);

            Log.d("DiaryAddFragment", "Creating diary entry for child: " + childId);
            DiaryEntriesApi apiService = ApiClient.getDiaryEntriesApi(requireContext());
            Call<ApiResponse<DiaryEntry>> call = apiService.createDiaryEntry(diaryEntry);

            call.enqueue(new Callback<ApiResponse<DiaryEntry>>() {
                @Override
                public void onResponse(@NonNull Call<ApiResponse<DiaryEntry>> call, @NonNull Response<ApiResponse<DiaryEntry>> response) {
                    try {
                        Log.d("DiaryAddFragment", "API response received - Success: " + response.isSuccessful() + ", Code: " + response.code());

                        if (response.isSuccessful() && response.body() != null) {
                            Log.d("DiaryAddFragment", "Diary entry created successfully");
                            if (getContext() != null) {
                                Toast.makeText(getContext(), "Diary entry saved successfully", Toast.LENGTH_SHORT).show();
                            }

                            // Clear form
                            if (titleEditText != null) titleEditText.setText("");
                            if (descriptionEditText != null) descriptionEditText.setText("");
                            if (categoryAutoComplete != null) categoryAutoComplete.setText("");

                        } else {
                            Log.e("DiaryAddFragment", "Failed to create diary entry: " + response.message());
                            if (getContext() != null) {
                                Toast.makeText(getContext(), "Failed to save diary entry", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } catch (Exception e) {
                        Log.e("DiaryAddFragment", "Exception in onResponse: " + e.getMessage(), e);
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Error saving diary entry: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ApiResponse<DiaryEntry>> call, @NonNull Throwable t) {
                    Log.e("DiaryAddFragment", "API call failed: " + t.getMessage(), t);
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (Exception e) {
            Log.e("DiaryAddFragment", "Exception in createDiaryEntry: " + e.getMessage(), e);
            if (getContext() != null) {
                Toast.makeText(getContext(), "Error creating diary entry: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
