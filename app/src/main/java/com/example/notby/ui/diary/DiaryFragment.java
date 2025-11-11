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
import androidx.viewpager2.widget.ViewPager2;

import com.example.notby.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class DiaryFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        try {
            Log.d("DiaryFragment", "onCreateView started");
            View view = inflater.inflate(R.layout.fragment_diary, container, false);

            TabLayout tabLayout = view.findViewById(R.id.diary_tabs);
            ViewPager2 viewPager = view.findViewById(R.id.diary_viewpager);

            if (tabLayout == null || viewPager == null) {
                Log.e("DiaryFragment", "TabLayout or ViewPager not found in layout");
                Toast.makeText(getContext(), "Error loading diary interface", Toast.LENGTH_SHORT).show();
                return view;
            }

            if (getActivity() == null) {
                Log.e("DiaryFragment", "Activity is null when creating adapter");
                return view;
            }

            DiaryViewPagerAdapter adapter = new DiaryViewPagerAdapter(getActivity());
            viewPager.setAdapter(adapter);

            new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
                if (position == 1) {
                    tab.setText("Thêm mới");
                } else {
                    tab.setText("Cột mốc phát triển");
                }
            }).attach();

            Log.d("DiaryFragment", "DiaryFragment setup completed successfully");
            return view;
        } catch (Exception e) {
            Log.e("DiaryFragment", "Exception in onCreateView: " + e.getMessage(), e);
            if (getContext() != null) {
                Toast.makeText(getContext(), "Error loading diary: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            // Return a basic view to prevent complete crash
            return inflater.inflate(android.R.layout.simple_list_item_1, container, false);
        }
    }
}
