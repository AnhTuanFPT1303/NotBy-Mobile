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
import com.example.notby.data.model.DiaryEntriesResponse;
import com.example.notby.data.model.DiaryEntry;
import com.example.notby.data.remote.ApiClient;
import com.example.notby.data.remote.DiaryEntriesApi;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DiaryListFragment extends Fragment {

    private RecyclerView recyclerView;
    private DiaryAdapter adapter;
    private TokenManager tokenManager;
    private TabLayout tabLayout;
    private List<DiaryEntry> allEntries = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_diary_list, container, false);

        recyclerView = view.findViewById(R.id.milestones_recycler_view);
        tabLayout = view.findViewById(R.id.filter_tabs);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        tokenManager = new TokenManager(requireContext());
        
        setupTabs();
        
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDiaryEntries();
    }

    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("Tất cả"));
        tabLayout.addTab(tabLayout.newTab().setText("Vận động"));
        tabLayout.addTab(tabLayout.newTab().setText("Ngôn ngữ"));
        tabLayout.addTab(tabLayout.newTab().setText("Dinh dưỡng"));
        tabLayout.addTab(tabLayout.newTab().setText("Xã hội"));
        tabLayout.addTab(tabLayout.newTab().setText("Khác"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                filterEntries(tab.getText().toString());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });
    }

    private void filterEntries(String category) {
        if (category.equals("Tất cả")) {
            adapter = new DiaryAdapter(allEntries);
            recyclerView.setAdapter(adapter);
            return;
        }

        List<DiaryEntry> filteredList = new ArrayList<>();
        for (DiaryEntry entry : allEntries) {
            // This assumes the category mapping is direct. Adjust if needed.
            if (entry.getCategory() != null && entry.getCategory().equalsIgnoreCase(category)) {
                filteredList.add(entry);
            }
        }
        adapter = new DiaryAdapter(filteredList);
        recyclerView.setAdapter(adapter);
    }

    private void loadDiaryEntries() {
        String childId = tokenManager.getChildId();
        if (childId == null) {
            Toast.makeText(getContext(), "Chưa chọn bé", Toast.LENGTH_SHORT).show();
            return;
        }

        DiaryEntriesApi apiService = ApiClient.getDiaryEntriesApi(requireContext());
        Call<ApiResponse<DiaryEntriesResponse>> call = apiService.getDiaryEntries(childId);

        call.enqueue(new Callback<ApiResponse<DiaryEntriesResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<DiaryEntriesResponse>> call, @NonNull Response<ApiResponse<DiaryEntriesResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    DiaryEntriesResponse diaryResponse = response.body().getData();
                    allEntries = diaryResponse.getDiaryEntries();

                    if (allEntries != null && !allEntries.isEmpty()) {
                        // Initially show all entries
                        filterEntries("Tất cả");
                    } else {
                        allEntries.clear();
                        filterEntries("Tất cả"); // Clear the adapter
                        Toast.makeText(getContext(), "Chưa có mục nhật ký nào", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Lỗi khi tải nhật ký", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<DiaryEntriesResponse>> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("DIARY_DEBUG", "API Call Failed: ", t);
            }
        });
    }
}
