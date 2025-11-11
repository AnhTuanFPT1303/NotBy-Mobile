package com.example.notby.ui.baby;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notby.R;
import com.example.notby.data.TokenManager;
import com.example.notby.data.model.ApiResponse;
import com.example.notby.data.model.Baby;
import com.example.notby.data.model.BabiesResponse;

import com.example.notby.data.remote.ApiClient;
import com.example.notby.data.remote.BabiesApi;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BabyListActivity extends AppCompatActivity implements BabyAdapter.OnBabyDeletedListener {

    private static final int ADD_BABY_REQUEST = 1;

    private RecyclerView recyclerView;
    private BabyAdapter adapter;
    private TokenManager tokenManager;
    private List<Baby> babyList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_baby_list);

        recyclerView = findViewById(R.id.babies_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        tokenManager = new TokenManager(this);

        loadBabies();

        findViewById(R.id.add_baby_button).setOnClickListener(v -> {
            Intent intent = new Intent(this, AddBabyActivity.class);
            startActivityForResult(intent, ADD_BABY_REQUEST);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_BABY_REQUEST && resultCode == RESULT_OK) {
            loadBabies();
        }
    }

    private void loadBabies() {
        // Get JWT token and extract user ID from it
        String token = tokenManager.getToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "User not logged in - No token", Toast.LENGTH_SHORT).show();
            return;
        }

        // Extract user ID from JWT token
        String parentId = tokenManager.getUserIdFromToken();
        if (parentId == null) {
            Toast.makeText(this, "User not logged in - Invalid token", Toast.LENGTH_SHORT).show();
            return;
        }

        BabiesApi apiService = ApiClient.getBabiesApi(); // Use non-authenticated client
        Call<ApiResponse<BabiesResponse>> call = apiService.findAll(parentId);

        call.enqueue(new Callback<ApiResponse<BabiesResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<BabiesResponse>> call, Response<ApiResponse<BabiesResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    BabiesResponse babiesResponse = response.body().getData();
                    if (babiesResponse != null && babiesResponse.getBabies() != null) {
                        babyList = babiesResponse.getBabies();
                        if (!babyList.isEmpty()) {
                            adapter = new BabyAdapter(babyList, BabyListActivity.this);
                            recyclerView.setAdapter(adapter);
                        }
                    }
                } else {
                    Toast.makeText(BabyListActivity.this, "Failed to load babies", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<BabiesResponse>> call, Throwable t) {
                Toast.makeText(BabyListActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBabyDeleted(Baby baby) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa bé")
                .setMessage("Bạn có chắc chắn muốn xóa bé " + baby.getFirstName() + "?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteBaby(baby))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteBaby(Baby baby) {
        BabiesApi apiService = ApiClient.getBabiesApi(); // Use non-authenticated client
        Call<ApiResponse<Void>> call = apiService.deleteBaby(baby.getId());

        call.enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(BabyListActivity.this, "Xóa bé thành công", Toast.LENGTH_SHORT).show();
                    babyList.remove(baby);
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(BabyListActivity.this, "Xóa bé thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                Toast.makeText(BabyListActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
