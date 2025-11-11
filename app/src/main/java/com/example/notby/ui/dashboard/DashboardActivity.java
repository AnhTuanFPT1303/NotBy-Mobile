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
import java.util.List;

public class DashboardActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private TokenManager tokenManager;
    private String currentBabyId = null;

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

        // Handle system back (replace deprecated onBackPressed)
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    // default behavior
                    finish();
                }
            }
        });

        tokenManager = new TokenManager(this);

        // Try to load child's data and populate nav header
        loadChildForCurrentUser();

        // Set OverviewFragment as the default screen
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new OverviewFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_overview);
        }

        // --- Handle Footer Click ---
        View navFooter = navigationView.findViewById(R.id.nav_footer_root);
        navFooter.setOnClickListener(this::showFooterPopupMenu);

        // Wire header clicks (if header already populated it will be overwritten later)
        View header = navigationView.getHeaderView(0);
        if (header != null) {
            View childCard = header.findViewById(R.id.child_card_root);
            View childAction = header.findViewById(R.id.child_action);
            View.OnClickListener headerClick = v -> {
                Intent intent = new Intent(DashboardActivity.this, BabyListActivity.class);
                startActivity(intent);
            };
            if (childCard != null) childCard.setOnClickListener(headerClick);
            if (childAction != null) childAction.setOnClickListener(headerClick);
        }
    }

    private void loadChildForCurrentUser() {
        // Get JWT token and extract user ID from it
        String token = tokenManager.getToken();
        if (token == null || token.isEmpty()) {
            Log.w("DashboardActivity", "No JWT token stored - user not logged in");
            return;
        }

        // Extract user ID from JWT token
        String userId = tokenManager.getUserIdFromToken();
        if (userId == null) {
            Log.e("DashboardActivity", "Failed to extract user ID from JWT token");
            return;
        }

        Log.d("DashboardActivity", "Loading babies for parentId=" + userId + " (extracted from JWT)");

        // Log token presence (masked) for debug
        String tail = token.length() > 8 ? token.substring(token.length() - 8) : token;
        Log.d("DashboardActivity", "JWT present (masked) ****" + tail);

        // Use non-authenticated API client for baby operations
        BabiesApi babiesApi = ApiClient.getBabiesApi();

        // If we have a saved childId, try fetching it directly

        String savedChildId = tokenManager.getChildId();
        if (savedChildId != null) {
            Log.d("DashboardActivity", "Found saved childId=" + savedChildId + ", fetching via /babies/{id}");

            Call<ApiResponse<Baby>> byIdCall = babiesApi.getById(savedChildId);

            byIdCall.enqueue(new Callback<ApiResponse<Baby>>() {
                @Override
                public void onResponse(@NonNull Call<ApiResponse<Baby>> call, @NonNull Response<ApiResponse<Baby>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        ApiResponse<Baby> apiResponse = response.body();
                        Baby baby = apiResponse.getData();

                        if (baby != null) {
                            currentBabyId = baby.getId();
                            updateNavHeaderWithBaby(baby);
                            Log.d("DashboardActivity", "Loaded baby by id=" + currentBabyId);
                        } else {
                            Log.w("DashboardActivity", "Response body missing 'data' field, falling back to findAll");
                            fetchBabiesByParent(babiesApi, userId);
                        }
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


    // Trong file: DashboardActivity.java

    private void fetchBabiesByParent(BabiesApi babiesApi, String parentId) {
        Log.d("DashboardActivity", "Fetching babies for parentId: " + parentId);

        // Use non-authenticated API client to fetch babies directly
        Call<ApiResponse<BabiesResponse>> call = babiesApi.findAll(parentId);

        call.enqueue(new Callback<ApiResponse<BabiesResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<BabiesResponse>> call, @NonNull Response<ApiResponse<BabiesResponse>> response) {
                try {
                    Log.d("DashboardActivity", "Response received - Status: " + response.isSuccessful() + ", Code: " + response.code());

                    if (response.isSuccessful() && response.body() != null) {
                        ApiResponse<BabiesResponse> apiResponse = response.body();
                        Log.d("DashboardActivity", "ApiResponse status: " + apiResponse.isStatus());

                        BabiesResponse babiesResponse = apiResponse.getData();

                        if (babiesResponse != null) {
                            Log.d("DashboardActivity", "BabiesResponse received, babies count: " +
                                (babiesResponse.getBabies() != null ? babiesResponse.getBabies().size() : "null"));

                            if (babiesResponse.getBabies() != null && !babiesResponse.getBabies().isEmpty()) {
                                List<Baby> babies = babiesResponse.getBabies();
                                Baby baby = babies.get(0);

                                Log.d("DashboardActivity", "Baby received - ID: " + baby.getId() +
                                    ", FirstName: " + baby.getFirstName() +
                                    ", LastName: " + baby.getLastName());

                                // persist child id
                                tokenManager.saveChildId(baby.getId());
                                currentBabyId = baby.getId();
                                updateNavHeaderWithBaby(baby);

                                Log.d("DashboardActivity", "Successfully updated nav header with baby");
                            } else {
                                Log.w("DashboardActivity", "Babies list is null or empty");
                                attemptRawInspect(babiesApi, parentId);
                            }
                        } else {
                            Log.w("DashboardActivity", "BabiesResponse data is null");
                            attemptRawInspect(babiesApi, parentId);
                        }
                    } else {
                        // Server returned error (4xx, 5xx)
                        try (ResponseBody responseBody = response.errorBody()) {
                            String err = responseBody != null ? responseBody.string() : "<empty>";
                            Log.e("DashboardActivity", "API error fetching babies: code=" + response.code() + " body=" + err);
                        } catch (IOException e) {
                            Log.e("DashboardActivity", "Error reading response body", e);
                        }
                        attemptRawInspect(babiesApi, parentId);
                    }
                } catch (Exception e) {
                    Log.e("DashboardActivity", "Exception in onResponse: " + e.getClass().getSimpleName() + " - " + e.getMessage(), e);
                    Toast.makeText(DashboardActivity.this, "Error processing baby data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<BabiesResponse>> call, @NonNull Throwable t) {
                Log.e("DashboardActivity", "API call failure: " + t.getClass().getSimpleName() + " - " + t.getMessage(), t);
                Toast.makeText(DashboardActivity.this, "Network error loading baby data: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void attemptRawInspect(BabiesApi babiesApi, String userId) {
        Log.d("DashboardActivity", "Attempting fallback raw call to inspect response shape");
        try {
            Call<Object> raw = babiesApi.findAllRaw(userId);
            raw.enqueue(new Callback<Object>() {
                @Override
                public void onResponse(@NonNull Call<Object> call, @NonNull Response<Object> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Log.d("DashboardActivity", "Raw response body: " + response.body());
                        // parsing handled elsewhere previously; keep simple here
                    } else {
                        // Ensure proper use of try-with-resources for ResponseBody in all instances
                        try (ResponseBody responseBody = response.errorBody()) {
                            String err = responseBody != null ? responseBody.string() : "<empty>";
                            Log.e("DashboardActivity", "Raw API error code=" + response.code() + " body=" + err);
                        } catch (IOException e) {
                            Log.e("DashboardActivity", "Error reading raw errorBody", e);
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Object> call, @NonNull Throwable t) {
                    Log.e("DashboardActivity", "Raw API call failed", t);
                }
            });
        } catch (Exception ex) {
            Log.e("DashboardActivity", "Fallback raw call failed", ex);
        }
    }

    private void updateNavHeaderWithBaby(Baby baby) {
        try {
            Log.d("DashboardActivity", "Updating nav header with baby: " + baby.getId());

            NavigationView navigationView = findViewById(R.id.nav_view);
            if (navigationView == null) {
                Log.w("DashboardActivity", "NavigationView is null");
                return;
            }

            View header = navigationView.getHeaderView(0);
            if (header == null) {
                Log.w("DashboardActivity", "Navigation header view is null");
                return;
            }

            TextView childName = header.findViewById(R.id.child_name);
            TextView childBirthday = header.findViewById(R.id.child_birthday);
            TextView childInitial = header.findViewById(R.id.child_initial);

            // Safely handle baby data
            String firstName = baby.getFirstName();
            String lastName = baby.getLastName();
            String dob = baby.getDob();

            Log.d("DashboardActivity", "Baby data - FirstName: " + firstName + ", LastName: " + lastName + ", DOB: " + dob);

            if (childName != null) {
                try {
                    String fullName;
                    if (firstName != null && lastName != null) {
                        fullName = getString(R.string.child_name_template, firstName, lastName);
                    } else if (firstName != null) {
                        fullName = firstName;
                    } else {
                        fullName = "Unknown";
                    }
                    childName.setText(fullName);
                    Log.d("DashboardActivity", "Set child name: " + fullName);
                } catch (Exception e) {
                    Log.e("DashboardActivity", "Error setting child name: " + e.getMessage(), e);
                    childName.setText(firstName != null ? firstName : "Unknown");
                }
            }

            if (childBirthday != null) {
                String birthday = dob != null ? dob : "";
                childBirthday.setText(birthday);
                Log.d("DashboardActivity", "Set child birthday: " + birthday);
            }

            if (childInitial != null) {
                String initial = "";
                if (firstName != null && !firstName.isEmpty()) {
                    initial = firstName.substring(0, 1).toUpperCase();
                }
                childInitial.setText(initial);
                Log.d("DashboardActivity", "Set child initial: " + initial);
            }

            // update current fields
            currentBabyId = baby.getId();

            // ensure header actions use current id
            View childCard = header.findViewById(R.id.child_card_root);
            View childAction = header.findViewById(R.id.child_action);
            View.OnClickListener headerClick = v -> {
                Intent intent = new Intent(DashboardActivity.this, BabyListActivity.class);
                startActivity(intent);
            };
            if (childCard != null) childCard.setOnClickListener(headerClick);
            if (childAction != null) childAction.setOnClickListener(headerClick);

            Log.d("DashboardActivity", "Successfully updated nav header");

        } catch (Exception e) {
            Log.e("DashboardActivity", "Exception in updateNavHeaderWithBaby: " + e.getClass().getSimpleName() + " - " + e.getMessage(), e);
            Toast.makeText(this, "Error updating baby information: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
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
        } else if (itemId == R.id.nav_add_photo) {
            // Quick action: Add photo - pass child id
            if (currentBabyId != null) {
                Toast.makeText(this, "Add photo for baby: " + currentBabyId, Toast.LENGTH_SHORT).show();
                // Intent intent = new Intent(this, AddPhotoActivity.class);
                // intent.putExtra("childId", currentBabyId);
                // startActivity(intent);
            } else {
                Toast.makeText(this, "Chưa chọn bé", Toast.LENGTH_SHORT).show();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        } else if (itemId == R.id.nav_add_note) {
            if (currentBabyId != null) {
                Toast.makeText(this, "Add note for baby: " + currentBabyId, Toast.LENGTH_SHORT).show();
                // start note activity with childId
            } else {
                Toast.makeText(this, "Chưa chọn bé", Toast.LENGTH_SHORT).show();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        } else if (itemId == R.id.nav_share) {
            if (currentBabyId != null) {
                Toast.makeText(this, "Share memory for baby: " + currentBabyId, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Chưa chọn bé", Toast.LENGTH_SHORT).show();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        } // Add other else-if blocks for other menu items

        if (selectedFragment != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}
