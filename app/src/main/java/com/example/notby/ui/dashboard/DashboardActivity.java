package com.example.notby.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.example.notby.R;
import com.example.notby.data.TokenManager;
import com.example.notby.data.model.Baby;
import com.example.notby.data.model.ApiResponse;
import com.example.notby.data.model.BabiesResponse;
import com.example.notby.data.remote.ApiClient;
import com.example.notby.data.remote.BabiesApi;
import com.example.notby.ui.baby.BabyListActivity;
import com.example.notby.ui.diary.DiaryFragment;
import com.google.android.material.navigation.NavigationView;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class DashboardActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private TokenManager tokenManager;
    private String currentBabyId = null;
    private ActivityResultLauncher<Intent> babyListLauncher;

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

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    finish();
                }
            }
        });

        tokenManager = new TokenManager(this);

        babyListLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String selectedBabyId = result.getData().getStringExtra("selected_baby_id");
                    if (selectedBabyId != null) {
                        tokenManager.saveChildId(selectedBabyId);
                        loadChildForCurrentUser(); // Reload data for the new baby
                    }
                }
            });

        loadChildForCurrentUser();
        updateNavFooter();

        View navFooter = navigationView.findViewById(R.id.nav_footer_root);
        navFooter.setOnClickListener(this::showFooterPopupMenu);

        View header = navigationView.getHeaderView(0);
        if (header != null) {
            View childCard = header.findViewById(R.id.child_card_root);
            View childAction = header.findViewById(R.id.child_action);
            View.OnClickListener headerClick = v -> {
                Intent intent = new Intent(DashboardActivity.this, BabyListActivity.class);
                babyListLauncher.launch(intent);
            };
            if (childCard != null) childCard.setOnClickListener(headerClick);
            if (childAction != null) childAction.setOnClickListener(headerClick);
        }
    }

    private void showOverviewForBaby(Baby baby) {
        if (baby == null || baby.getId() == null) {
            Log.e("DashboardActivity", "Cannot show overview, baby or baby ID is null");
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new Fragment()) // Show a blank fragment
                .commit();
            return;
        }

        Log.d("DashboardActivity", "Showing overview for baby: " + baby.getFirstName() + " (ID: " + baby.getId() + ")");

        OverviewFragment overviewFragment = OverviewFragment.newInstance(
            baby.getFirstName() + " " + baby.getLastName(),
            baby.getDob(),
            baby.getId()
        );

        getSupportFragmentManager().beginTransaction()
            .replace(R.id.fragment_container, overviewFragment)
            .commit();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setCheckedItem(R.id.nav_overview);
    }

    private void loadChildForCurrentUser() {
        String token = tokenManager.getToken();
        if (token == null || token.isEmpty()) {
            Log.w("DashboardActivity", "No JWT token stored - user not logged in");
            return;
        }

        String userId = tokenManager.getUserIdFromToken();
        if (userId == null) {
            Log.e("DashboardActivity", "Failed to extract user ID from JWT token");
            return;
        }

        Log.d("DashboardActivity", "Loading babies for parentId=" + userId);

        BabiesApi babiesApi = ApiClient.getBabiesApi();
        String savedChildId = tokenManager.getChildId();

        if (savedChildId != null) {
            Log.d("DashboardActivity", "Found saved childId=" + savedChildId + ", fetching via /babies/{id}");
            babiesApi.getById(savedChildId).enqueue(new Callback<ApiResponse<Baby>>() {
                @Override
                public void onResponse(@NonNull Call<ApiResponse<Baby>> call, @NonNull Response<ApiResponse<Baby>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                        Baby baby = response.body().getData();
                        updateNavHeaderWithBaby(baby);
                        Log.d("DashboardActivity", "Loaded baby by id=" + baby.getId());
                    } else {
                        Log.w("DashboardActivity", "getById didn't return valid baby, falling back to findAll");
                        fetchBabiesByParent(babiesApi, userId);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ApiResponse<Baby>> call, @NonNull Throwable t) {
                    Log.e("DashboardActivity", "getById failed", t);
                    fetchBabiesByParent(babiesApi, userId);
                }
            });
        } else {
            fetchBabiesByParent(babiesApi, userId);
        }
    }

    private void fetchBabiesByParent(BabiesApi babiesApi, String parentId) {
        Log.d("DashboardActivity", "Fetching babies for parentId: " + parentId);
        babiesApi.findAll(parentId).enqueue(new Callback<ApiResponse<BabiesResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<BabiesResponse>> call, @NonNull Response<ApiResponse<BabiesResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<Baby> babies = response.body().getData().getBabies();
                    if (babies != null && !babies.isEmpty()) {
                        Baby firstBaby = babies.get(0);
                        tokenManager.saveChildId(firstBaby.getId());
                        updateNavHeaderWithBaby(firstBaby);
                    } else {
                        Log.w("DashboardActivity", "Babies list is empty for this user.");
                    }
                } else {
                     try (ResponseBody responseBody = response.errorBody()) {
                        String err = responseBody != null ? responseBody.string() : "<empty>";
                        Log.e("DashboardActivity", "API error fetching babies: code=" + response.code() + " body=" + err);
                    } catch (IOException e) {
                        Log.e("DashboardActivity", "Error reading response body", e);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<BabiesResponse>> call, @NonNull Throwable t) {
                Log.e("DashboardActivity", "API call to fetch babies failed", t);
                Toast.makeText(DashboardActivity.this, "Network error loading baby data: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateNavHeaderWithBaby(Baby baby) {
        // First, show the main content
        showOverviewForBaby(baby);

        // Then, update the navigation UI. This is less critical and can fail gracefully.
        try {
            Log.d("DashboardActivity", "Updating nav header with baby: " + baby.getId());
            NavigationView navigationView = findViewById(R.id.nav_view);
            if (navigationView == null) return;
            
            View header = navigationView.getHeaderView(0);
            if (header == null) return;

            TextView childName = header.findViewById(R.id.child_name);
            TextView childBirthday = header.findViewById(R.id.child_birthday);
            TextView childInitial = header.findViewById(R.id.child_initial);

            if(childName != null && baby.getFirstName() != null) {
                childName.setText("Bé " + baby.getFirstName());
            }

            if(childBirthday != null) {
                 childBirthday.setText(formatDate(baby.getDob()));
            }
           
            if(childInitial != null && baby.getFirstName() != null && !baby.getFirstName().isEmpty()) {
                childInitial.setText(baby.getFirstName().substring(0, 1).toUpperCase());
            }

            currentBabyId = baby.getId();

            View childCard = header.findViewById(R.id.child_card_root);
            View childAction = header.findViewById(R.id.child_action);
            View.OnClickListener headerClick = v -> {
                Intent intent = new Intent(DashboardActivity.this, BabyListActivity.class);
                babyListLauncher.launch(intent);
            };
            if (childCard != null) childCard.setOnClickListener(headerClick);
            if (childAction != null) childAction.setOnClickListener(headerClick);

            Log.d("DashboardActivity", "Successfully updated nav header");

        } catch (Exception e) {
            Log.e("DashboardActivity", "Non-critical error updating nav header", e);
        }
    }

    private void updateNavFooter() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        View navFooter = navigationView.findViewById(R.id.nav_footer_root);
        TextView parentName = navFooter.findViewById(R.id.parent_name);
        TextView parentInitial = navFooter.findViewById(R.id.parent_initial);
        String userName = tokenManager.getUserName();
        if (userName != null && !userName.isEmpty()) {
            parentName.setText(userName);
            parentInitial.setText(String.valueOf(userName.charAt(0)));
        }
    }

    private String formatDate(String dateString) {
        if (dateString == null) return "";
        try {
            SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            originalFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            SimpleDateFormat targetFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
            Date date = originalFormat.parse(dateString);
            return targetFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return dateString; // Return original string if parsing fails
        }
    }

    private void showFooterPopupMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenuInflater().inflate(R.menu.footer_popup_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_logout) {
                Toast.makeText(this, "Đăng xuất", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.action_settings) {
                Toast.makeText(this, "Cài đặt", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
        popup.show();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment selectedFragment = null;
        int itemId = item.getItemId();

        if (itemId == R.id.nav_overview) {
            loadChildForCurrentUser(); // This will reload the baby and show the overview
        } else if (itemId == R.id.nav_stats) {
            // selectedFragment = new StatsFragment();
            Toast.makeText(this, "Thống kê", Toast.LENGTH_SHORT).show();
        } else if (itemId == R.id.nav_development_diary) {
            selectedFragment = new DiaryFragment();
        } else {
            // Handle other items or quick actions
            if (currentBabyId == null) {
                Toast.makeText(this, "Chưa chọn bé", Toast.LENGTH_SHORT).show();
            } else {
                if (itemId == R.id.nav_add_photo) {
                    Toast.makeText(this, "Add photo for baby: " + currentBabyId, Toast.LENGTH_SHORT).show();
                } else if (itemId == R.id.nav_add_note) {
                    Toast.makeText(this, "Add note for baby: " + currentBabyId, Toast.LENGTH_SHORT).show();
                } else if (itemId == R.id.nav_share) {
                    Toast.makeText(this, "Share memory for baby: " + currentBabyId, Toast.LENGTH_SHORT).show();
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true; // Return early for quick actions
        }

        if (selectedFragment != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}
