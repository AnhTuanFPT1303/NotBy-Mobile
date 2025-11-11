package com.example.notby.ui.baby;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.notby.R;
import com.example.notby.data.TokenManager;
import com.example.notby.data.model.ApiResponse;
import com.example.notby.data.model.Baby;
import com.example.notby.data.remote.ApiClient;
import com.example.notby.data.remote.BabiesApi;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddBabyActivity extends AppCompatActivity {

    private EditText firstNameEditText, lastNameEditText, dobEditText;
    private Spinner genderSpinner;
    private Button saveButton, cancelButton;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_baby);

        firstNameEditText = findViewById(R.id.first_name_edit_text);
        lastNameEditText = findViewById(R.id.last_name_edit_text);
        dobEditText = findViewById(R.id.dob_edit_text);
        genderSpinner = findViewById(R.id.gender_spinner);
        saveButton = findViewById(R.id.save_button);
        cancelButton = findViewById(R.id.cancel_button);

        tokenManager = new TokenManager(this);

        // Set up gender spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.gender_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(adapter);

        saveButton.setOnClickListener(v -> createBaby());

        cancelButton.setOnClickListener(v -> finish());
    }

    private void createBaby() {
        String firstName = firstNameEditText.getText().toString();
        String lastName = lastNameEditText.getText().toString();
        String dob = dobEditText.getText().toString();
        String gender = genderSpinner.getSelectedItem().toString();
        String parentId = tokenManager.getUserId();

        if (firstName.isEmpty() || lastName.isEmpty() || dob.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Baby baby = new Baby();
        baby.setFirstName(firstName);
        baby.setLastName(lastName);
        baby.setDob(dob);
        baby.setGender(gender);
        baby.setParentId(parentId);

        BabiesApi apiService = ApiClient.getBabiesApi(this);
        Call<ApiResponse<Baby>> call = apiService.createBaby(baby);

        call.enqueue(new Callback<ApiResponse<Baby>>() {
            @Override
            public void onResponse(Call<ApiResponse<Baby>> call, Response<ApiResponse<Baby>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AddBabyActivity.this, "Baby created successfully", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(AddBabyActivity.this, "Failed to create baby", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Baby>> call, Throwable t) {
                Toast.makeText(AddBabyActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
