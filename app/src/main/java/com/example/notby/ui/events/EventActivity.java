package com.example.notby.ui.events;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.notby.R;
import com.example.notby.data.model.ApiResponse;
import com.example.notby.data.model.Event;
import com.example.notby.data.remote.ApiClient;
import com.example.notby.data.remote.EventApi;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventActivity extends AppCompatActivity {

    public static final String EXTRA_MODE = "EXTRA_MODE";
    public static final String EXTRA_EVENT_JSON = "EXTRA_EVENT_JSON";
    public static final String EXTRA_CHILD_ID = "EXTRA_CHILD_ID";
    public static final String MODE_CREATE = "CREATE";
    public static final String MODE_EDIT = "EDIT";

    private TextInputEditText titleEditText, startDateEditText, endDateEditText, notesEditText;
    private RadioGroup eventTypeRadioGroup;
    private Button saveButton;

    private String currentMode;
    private Event existingEvent;
    private String childId;
    private Calendar startCalendar = Calendar.getInstance();
    private Calendar endCalendar = Calendar.getInstance();
    private final SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
    private final SimpleDateFormat apiFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);
        apiFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        findViews();
        handleIntent();
        setupListeners();
    }

    private void findViews() {
        titleEditText = findViewById(R.id.edit_text_event_title);
        startDateEditText = findViewById(R.id.edit_text_start_date);
        endDateEditText = findViewById(R.id.edit_text_end_date);
        notesEditText = findViewById(R.id.edit_text_notes);
        eventTypeRadioGroup = findViewById(R.id.radio_group_event_type);
        saveButton = findViewById(R.id.button_save_event);
    }

    private void handleIntent() {
        Intent intent = getIntent();
        currentMode = intent.getStringExtra(EXTRA_MODE);

        if (MODE_EDIT.equals(currentMode)) {
            setTitle("Chỉnh sửa sự kiện");
            String eventJson = intent.getStringExtra(EXTRA_EVENT_JSON);
            existingEvent = new Gson().fromJson(eventJson, Event.class);
            childId = existingEvent.getChildIdString();
            populateFields();
        } else {
            setTitle("Thêm sự kiện mới");
            childId = intent.getStringExtra(EXTRA_CHILD_ID);
        }
    }

    private void populateFields() {
        if (existingEvent == null) return;

        titleEditText.setText(existingEvent.getTitle());
        notesEditText.setText(existingEvent.getNotes());

        // Set radio button
        switch (existingEvent.getEventType()) {
            case "sport":
                ((RadioButton)findViewById(R.id.radio_sport)).setChecked(true);
                break;
            case "school":
                ((RadioButton)findViewById(R.id.radio_school)).setChecked(true);
                break;
            case "extraClass":
                 ((RadioButton)findViewById(R.id.radio_extra_class)).setChecked(true);
                break;
            case "other":
                 ((RadioButton)findViewById(R.id.radio_other)).setChecked(true);
                break;
        }
        
        // Dates
        try {
            startCalendar.setTime(apiFormat.parse(existingEvent.getStartAt()));
            endCalendar.setTime(apiFormat.parse(existingEvent.getEndAt()));
            startDateEditText.setText(displayFormat.format(startCalendar.getTime()));
            endDateEditText.setText(displayFormat.format(endCalendar.getTime()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupListeners() {
        startDateEditText.setOnClickListener(v -> showDatePicker(startCalendar, startDateEditText));
        endDateEditText.setOnClickListener(v -> showDatePicker(endCalendar, endDateEditText));
        saveButton.setOnClickListener(v -> saveEvent());
    }

    private void showDatePicker(final Calendar calendar, final TextInputEditText editText) {
        new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    editText.setText(displayFormat.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private String getSelectedEventType() {
        int selectedId = eventTypeRadioGroup.getCheckedRadioButtonId();
        if (selectedId == R.id.radio_sport) return "sport";
        if (selectedId == R.id.radio_school) return "school";
        if (selectedId == R.id.radio_extra_class) return "extraClass";
        if (selectedId == R.id.radio_other) return "other";
        return "other";
    }

    private void saveEvent() {
        String title = titleEditText.getText().toString().trim();
        if (title.isEmpty()) {
            Toast.makeText(this, "Tiêu đề không được để trống", Toast.LENGTH_SHORT).show();
            return;
        }

        Event event = new Event();
        event.setTitle(title);
        event.setNotes(notesEditText.getText().toString());
        event.setEventType(getSelectedEventType());
        event.setStartAt(apiFormat.format(startCalendar.getTime()));
        event.setEndAt(apiFormat.format(endCalendar.getTime()));

        EventApi eventApi = ApiClient.getEventApi(this);
        Call<ApiResponse<Event>> call;

        if (MODE_CREATE.equals(currentMode)) {
            event.setChildId(childId); // Set the child ID as a String
            call = eventApi.create(event);
        } else { // MODE_EDIT
            event.setId(existingEvent.getId());
            event.setChildId(childId); // Also set the child ID for update
            call = eventApi.update(event.getId(), event);
        }

        saveButton.setEnabled(false);

        call.enqueue(new Callback<ApiResponse<Event>>() {
            @Override
            public void onResponse(Call<ApiResponse<Event>> call, Response<ApiResponse<Event>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    Toast.makeText(EventActivity.this, "Lưu thành công!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    saveButton.setEnabled(true);
                    try {
                         String error = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                         Toast.makeText(EventActivity.this, "Lỗi khi lưu: " + error, Toast.LENGTH_LONG).show();
                         Log.e("EVENT_SAVE", "Error: " + error);
                    } catch(Exception e) {
                         Toast.makeText(EventActivity.this, "Lỗi khi lưu.", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Event>> call, Throwable t) {
                saveButton.setEnabled(true);
                Toast.makeText(EventActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("EVENT_SAVE", "Failure: ", t);
            }
        });
    }
}
