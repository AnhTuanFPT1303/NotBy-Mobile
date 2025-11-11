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

import java.util.Locale;

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

        String[] categories = {"Vận động", "Ngôn ngữ", "Dinh dưỡng", "Xã hội", "Khác"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, categories);
        categoryAutoComplete.setAdapter(adapter);

        saveButton.setOnClickListener(v -> createDiaryEntry());

        return view;
    }

    private void createDiaryEntry() {
        String title = titleEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        String category = categoryAutoComplete.getText().toString().trim();
        String childId = tokenManager.getChildId();

        if (title.isEmpty() || category.isEmpty() || childId == null) {
            Toast.makeText(getContext(), "Vui lòng điền các trường bắt buộc", Toast.LENGTH_SHORT).show();
            return;
        }

        DiaryEntry diaryEntry = new DiaryEntry();
        diaryEntry.setTitle(title);
        diaryEntry.setContent(description);
        diaryEntry.setCategory(category.toLowerCase(Locale.ROOT)); // FIX: Convert to lowercase
        diaryEntry.setChildId(childId);

        DiaryEntriesApi apiService = ApiClient.getDiaryEntriesApi(requireContext());
        Call<ApiResponse<DiaryEntry>> call = apiService.createDiaryEntry(diaryEntry);
        saveButton.setEnabled(false);

        call.enqueue(new Callback<ApiResponse<DiaryEntry>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<DiaryEntry>> call, @NonNull Response<ApiResponse<DiaryEntry>> response) {
                saveButton.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(getContext(), "Đã lưu thành công", Toast.LENGTH_SHORT).show();
                    // Clear form
                    titleEditText.setText("");
                    descriptionEditText.setText("");
                    categoryAutoComplete.setText("", false);
                } else {
                    Toast.makeText(getContext(), "Lỗi khi lưu", Toast.LENGTH_SHORT).show();
                    Log.e("DIARY_ADD", "Failed to create entry. Code: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<DiaryEntry>> call, @NonNull Throwable t) {
                saveButton.setEnabled(true);
                Toast.makeText(getContext(), "Lỗi mạng", Toast.LENGTH_SHORT).show();
                Log.e("DIARY_ADD", "API call failed", t);
            }
        });
    }
}
