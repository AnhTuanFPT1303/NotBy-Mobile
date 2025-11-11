package com.example.notby.ui.library;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class LibraryPagerAdapter extends FragmentStateAdapter {
    private static final int TAB_COUNT = 4;

    public LibraryPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Create a new fragment based on the tab position
        return LibraryContentFragment.newInstance(position);
    }

    @Override
    public int getItemCount() {
        return TAB_COUNT;
    }
}
