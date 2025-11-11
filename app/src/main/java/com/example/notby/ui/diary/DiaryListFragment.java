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
        try {
            Log.d("DiaryListFragment", "onCreateView started");
            View view = inflater.inflate(R.layout.fragment_diary_list, container, false);

            recyclerView = view.findViewById(R.id.milestones_recycler_view);
            if (recyclerView == null) {
                Log.e("DiaryListFragment", "RecyclerView not found in layout");
                return view;
            }

            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

            tokenManager = new TokenManager(requireContext());
            Log.d("DiaryListFragment", "TokenManager initialized");

            loadDiaryEntries();

            return view;
        } catch (Exception e) {
            Log.e("DiaryListFragment", "Exception in onCreateView: " + e.getMessage(), e);
            // Return a simple view to prevent complete crash
            return inflater.inflate(android.R.layout.simple_list_item_1, container, false);
        }
    }

    private void loadDiaryEntries() {
        try {
            Log.d("DiaryListFragment", "loadDiaryEntries started");

            String childId = tokenManager.getChildId();
            Log.d("DiaryListFragment", "Child ID from TokenManager: " + childId);

            if (childId == null) {
                Log.w("DiaryListFragment", "No child ID found, showing message");
                if (getContext() != null) {
                    Toast.makeText(getContext(), "No child selected", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            Log.d("DiaryListFragment", "Getting DiaryEntriesApi");
            DiaryEntriesApi apiService = ApiClient.getDiaryEntriesApi(requireContext());
            Call<ApiResponse<List<DiaryEntry>>> call = apiService.getDiaryEntries(childId);

            Log.d("DiaryListFragment", "Making API call for child: " + childId);
            call.enqueue(new Callback<ApiResponse<List<DiaryEntry>>>() {
                @Override
                public void onResponse(@NonNull Call<ApiResponse<List<DiaryEntry>>> call, @NonNull Response<ApiResponse<List<DiaryEntry>>> response) {
                    try {
                        Log.d("DiaryListFragment", "API response received - Success: " + response.isSuccessful() + ", Code: " + response.code());

                        if (response.isSuccessful() && response.body() != null) {
                            List<DiaryEntry> diaryEntries = response.body().getData();
                            Log.d("DiaryListFragment", "Diary entries count: " + (diaryEntries != null ? diaryEntries.size() : "null"));

                            if (diaryEntries != null && !diaryEntries.isEmpty()) {
                                adapter = new DiaryAdapter(diaryEntries);
                                recyclerView.setAdapter(adapter);
                                Log.d("DiaryListFragment", "Adapter set successfully");
                            } else {
                                Log.d("DiaryListFragment", "No diary entries found");
                                if (getContext() != null) {
                                    Toast.makeText(getContext(), "No diary entries found", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } else {
                            Log.e("DiaryListFragment", "API response unsuccessful: " + response.message());
                            if (getContext() != null) {
                                Toast.makeText(getContext(), "Failed to load diary entries", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } catch (Exception e) {
                        Log.e("DiaryListFragment", "Exception in onResponse: " + e.getMessage(), e);
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Error processing diary data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ApiResponse<List<DiaryEntry>>> call, @NonNull Throwable t) {
                    Log.e("DiaryListFragment", "API call failed: " + t.getMessage(), t);
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (Exception e) {
            Log.e("DiaryListFragment", "Exception in loadDiaryEntries: " + e.getMessage(), e);
            if (getContext() != null) {
                Toast.makeText(getContext(), "Error loading diary: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
