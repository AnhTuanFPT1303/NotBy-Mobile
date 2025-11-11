package com.example.notby.ui.baby;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.notby.R;
import com.example.notby.data.TokenManager;
import com.example.notby.data.model.ApiResponse;
import com.example.notby.data.model.Baby;
import com.example.notby.data.remote.ApiClient;
import com.example.notby.data.remote.BabiesApi;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddBabyActivity extends AppCompatActivity {

    private static final String TAG = "AddBabyActivity";

    private EditText firstNameEditText, lastNameEditText, dobEditText;
    private AutoCompleteTextView genderSpinner;
    private Button saveButton, cancelButton;
    private TokenManager tokenManager;
    private String babyId;

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
        TextInputLayout dobInputLayout = findViewById(R.id.dob_input_layout);
        TextView titleText = findViewById(R.id.title_text);

        tokenManager = new TokenManager(this);

        // Set up gender spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.gender_array, android.R.layout.simple_list_item_1);
        genderSpinner.setAdapter(adapter);

        babyId = getIntent().getStringExtra("baby_id");

        if (babyId != null) {
            titleText.setText("Cập nhật thông tin");
            loadBabyData();
        } else {
            titleText.setText("Thêm bé mới");
        }

        dobInputLayout.setEndIconOnClickListener(v -> showDatePickerDialog());

        saveButton.setOnClickListener(v -> {
            if (babyId != null) {
                updateBaby();
            } else {
                createBaby();
            }
        });

        cancelButton.setOnClickListener(v -> finish());
        findViewById(R.id.close_button).setOnClickListener(v -> finish());
    }

    private void showDatePickerDialog() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year1, monthOfYear, dayOfMonth);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
                    dobEditText.setText(sdf.format(selectedDate.getTime()));
                }, year, month, day);
        datePickerDialog.show();
    }

    private String formatDateForDisplay(String apiDate) {
        if (apiDate == null || apiDate.isEmpty()) {
            return "";
        }
        try {
            SimpleDateFormat apiFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            apiFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = apiFormat.parse(apiDate);
            SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
            return displayFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return apiDate; // Return original if parsing fails
        }
    }

    private String formatDateForApi(String displayDate) {
        if (displayDate == null || displayDate.isEmpty()) {
            return "";
        }
        try {
            SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
            Date date = displayFormat.parse(displayDate);
            SimpleDateFormat apiFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            apiFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            return apiFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return displayDate; // Return original if parsing fails
        }
    }

    private void loadBabyData() {
        BabiesApi apiService = ApiClient.getBabiesApi();
        Call<ApiResponse<Baby>> call = apiService.getById(babyId);

        call.enqueue(new Callback<ApiResponse<Baby>>() {
            @Override
            public void onResponse(Call<ApiResponse<Baby>> call, Response<ApiResponse<Baby>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Baby baby = response.body().getData();
                    if (baby != null) {
                        firstNameEditText.setText(baby.getFirstName());
                        lastNameEditText.setText(baby.getLastName());
                        dobEditText.setText(formatDateForDisplay(baby.getDob()));
                        // Set gender spinner selection
                        String[] genders = getResources().getStringArray(R.array.gender_array);
                        for (int i = 0; i < genders.length; i++) {
                            if (genders[i].equalsIgnoreCase(baby.getGender())) {
                                genderSpinner.setText(genders[i], false);
                                break;
                            }
                        }
                    }
                } else {
                    Toast.makeText(AddBabyActivity.this, "Failed to load baby data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Baby>> call, Throwable t) {
                Toast.makeText(AddBabyActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createBaby() {
        String firstName = firstNameEditText.getText().toString();
        String lastName = lastNameEditText.getText().toString();
        String dob = dobEditText.getText().toString();
        String gender = genderSpinner.getText().toString();

        if (firstName.isEmpty() || lastName.isEmpty() || dob.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Baby baby = new Baby();
        baby.setFirstName(firstName);
        baby.setLastName(lastName);
        baby.setDob(formatDateForApi(dob));
        baby.setGender(gender);
        baby.setParentId(tokenManager.getUserId());

        BabiesApi apiService = ApiClient.getBabiesApi();
        Call<ApiResponse<Baby>> call = apiService.createBaby(baby);

        call.enqueue(new Callback<ApiResponse<Baby>>() {
            @Override
            public void onResponse(Call<ApiResponse<Baby>> call, Response<ApiResponse<Baby>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AddBabyActivity.this, "Baby created successfully", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    String errorMsg = "Failed to create baby. Code: " + response.code() + "\nBody: " + errorBody;
                    Toast.makeText(AddBabyActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    Log.e(TAG, errorMsg);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Baby>> call, Throwable t) {
                Log.e(TAG, "Create baby failed", t);
                Toast.makeText(AddBabyActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateBaby() {
        String firstName = firstNameEditText.getText().toString();
        String lastName = lastNameEditText.getText().toString();
        String dob = dobEditText.getText().toString();
        String gender = genderSpinner.getText().toString();

        if (firstName.isEmpty() || lastName.isEmpty() || dob.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Baby baby = new Baby();
        baby.setFirstName(firstName);
        baby.setLastName(lastName);
        baby.setDob(formatDateForApi(dob));
        baby.setGender(gender);
        baby.setParentId(tokenManager.getUserId()); // Keep parentId on update

        BabiesApi apiService = ApiClient.getBabiesApi();
        Call<ApiResponse<Baby>> call = apiService.updateBaby(babyId, baby);

        call.enqueue(new Callback<ApiResponse<Baby>>() {
            @Override
            public void onResponse(Call<ApiResponse<Baby>> call, Response<ApiResponse<Baby>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AddBabyActivity.this, "Baby updated successfully", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    String errorMsg = "Failed to update baby. Code: " + response.code() + "\nBody: " + errorBody;
                    Toast.makeText(AddBabyActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    Log.e(TAG, errorMsg);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Baby>> call, Throwable t) {
                Log.e(TAG, "Update baby failed", t);
                Toast.makeText(AddBabyActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
