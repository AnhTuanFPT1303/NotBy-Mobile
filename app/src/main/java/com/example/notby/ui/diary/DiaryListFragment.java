package com.example.notby.ui.diary;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notby.R;
import com.example.notby.data.TokenManager;
import com.example.notby.data.model.ApiResponse;
import com.example.notby.data.model.DiaryEntry;
import com.example.notby.data.remote.ApiClient;
import com.example.notby.data.remote.DiaryEntriesApi;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DiaryListFragment extends Fragment {

    private RecyclerView recyclerView;
    private DiaryAdapter adapter;
    private TokenManager tokenManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_diary_list, container, false);

        recyclerView = view.findViewById(R.id.milestones_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        tokenManager = new TokenManager(requireContext());

        loadDiaryEntries();

        return view;
    }

    private void loadDiaryEntries() {
        String childId = tokenManager.getChildId();
        if (childId == null) {
            Toast.makeText(getContext(), "No child selected", Toast.LENGTH_SHORT).show();
            return;
        }

        DiaryEntriesApi apiService = ApiClient.getDiaryEntriesApi(requireContext());
        Call<ApiResponse<List<DiaryEntry>>> call = apiService.getDiaryEntries(childId);

        call.enqueue(new Callback<ApiResponse<List<DiaryEntry>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<DiaryEntry>>> call, Response<ApiResponse<List<DiaryEntry>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<DiaryEntry> diaryEntries = response.body().getData();
                    if (diaryEntries != null && !diaryEntries.isEmpty()) {
                        adapter = new DiaryAdapter(diaryEntries);
                        recyclerView.setAdapter(adapter);
                    } else {
                        Toast.makeText(getContext(), "No diary entries found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Failed to load diary entries", Toast.LENGTH_SHORT).show();
                    Log.e("DiaryListFragment", "API call failed: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<DiaryEntry>>> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("DiaryListFragment", "API call failed", t);
            }
        });
    }
}
