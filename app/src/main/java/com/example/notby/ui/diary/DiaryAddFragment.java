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
        View view = inflater.inflate(R.layout.fragment_diary_add, container, false);

        titleEditText = view.findViewById(R.id.title_edit_text);
        descriptionEditText = view.findViewById(R.id.description_edit_text);
        categoryAutoComplete = view.findViewById(R.id.category_auto_complete);
        saveButton = view.findViewById(R.id.save_button);

        tokenManager = new TokenManager(requireContext());

        // Set up category dropdown
        String[] categories = {"Vận động", "Ngôn ngữ", "Dinh dưỡng", "Xã hội", "Khác"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, categories);
        categoryAutoComplete.setAdapter(adapter);

        saveButton.setOnClickListener(v -> createDiaryEntry());

        return view;
    }

    private void createDiaryEntry() {
        String title = titleEditText.getText().toString();
        String description = descriptionEditText.getText().toString();
        String category = categoryAutoComplete.getText().toString();
        String childId = tokenManager.getChildId();

        if (title.isEmpty() || category.isEmpty() || childId == null) {
            Toast.makeText(getContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        DiaryEntry diaryEntry = new DiaryEntry();
        diaryEntry.setTitle(title);
        diaryEntry.setContent(description);
        diaryEntry.setCategory(category);
        diaryEntry.setChildId(childId);

        DiaryEntriesApi apiService = ApiClient.getDiaryEntriesApi(requireContext());
        Call<ApiResponse<DiaryEntry>> call = apiService.createDiaryEntry(diaryEntry);

        call.enqueue(new Callback<ApiResponse<DiaryEntry>>() {
            @Override
            public void onResponse(Call<ApiResponse<DiaryEntry>> call, Response<ApiResponse<DiaryEntry>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(getContext(), "Diary entry created successfully", Toast.LENGTH_SHORT).show();
                    // Optionally, navigate back or clear the form
                } else {
                    Toast.makeText(getContext(), "Failed to create diary entry", Toast.LENGTH_SHORT).show();
                    Log.e("DiaryAddFragment", "API call failed: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<DiaryEntry>> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("DiaryAddFragment", "API call failed", t);
            }
        });
    }
}
