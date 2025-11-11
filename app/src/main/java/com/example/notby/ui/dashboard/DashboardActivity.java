package com.example.notby.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.example.notby.R;
import com.example.notby.ui.diary.DiaryFragment; // Import DiaryFragment
import com.example.notby.ui.library.LibraryActivity;
import com.example.notby.ui.forumpost.ForumPostActivity; // Import ForumPostActivity
import com.google.android.material.navigation.NavigationView;

public class DashboardActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Handle incoming intent
        if (getIntent() != null && getIntent().getBooleanExtra("open_diary", false)) {
            // Open diary fragment
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new DiaryFragment())
                .commit();
            navigationView.setCheckedItem(R.id.nav_development_diary);
        } else if (savedInstanceState == null) {
            // Set default fragment
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new OverviewFragment())
                .commit();
            navigationView.setCheckedItem(R.id.nav_overview);
        }

        // --- Handle Footer Click ---
        View navFooter = navigationView.findViewById(R.id.nav_footer_root);
        navFooter.setOnClickListener(this::showFooterPopupMenu);
    }

    private void showFooterPopupMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenuInflater().inflate(R.menu.footer_popup_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_logout) {
                // Handle logout
                Toast.makeText(this, "Đăng xuất", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.action_settings) {
                // Handle settings
                Toast.makeText(this, "Cài đặt", Toast.LENGTH_SHORT).show();
                return true;
            }
            // Add other cases for other menu items
            return false;
        });
        popup.show();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment selectedFragment = null;
        int itemId = item.getItemId();

        if (itemId == R.id.nav_overview) {
            selectedFragment = new OverviewFragment();
        } else if (itemId == R.id.nav_stats) {
            // selectedFragment = new StatsFragment(); // Create and use your other fragments here
            Toast.makeText(this, "Thống kê", Toast.LENGTH_SHORT).show();
        } else if (itemId == R.id.nav_development_diary) {
            selectedFragment = new DiaryFragment(); // Load the DiaryFragment
        }  else if (itemId == R.id.nav_library) {
            startActivity(new Intent(this, LibraryActivity.class));
            return true;
        } else if (itemId == R.id.nav_forum) {
            startActivity(new Intent(this, ForumPostActivity.class));
            return true;
        }

        if (selectedFragment != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}