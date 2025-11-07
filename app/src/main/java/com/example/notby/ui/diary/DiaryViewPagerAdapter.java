package com.example.notby.ui.diary;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class DiaryViewPagerAdapter extends FragmentStateAdapter {

    public DiaryViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 1) {
            return new DiaryAddFragment();
        }
        return new DiaryListFragment();
    }

    @Override
    public int getItemCount() {
        return 2; // We have two tabs
    }
}
