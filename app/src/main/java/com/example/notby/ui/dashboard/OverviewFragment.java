package com.example.notby.ui.dashboard;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notby.R;
import com.example.notby.data.model.ApiResponse;
import com.example.notby.data.model.Event;
import com.example.notby.data.model.EventsResponse;
import com.example.notby.data.remote.ApiClient;
import com.example.notby.data.remote.EventApi;
import com.example.notby.ui.events.EventActivity;
import com.google.gson.Gson;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OverviewFragment extends Fragment implements EventAdapter.OnEventListener {

    private static final String ARG_BABY_NAME = "baby_name";
    private static final String ARG_BABY_DOB = "baby_dob";
    private static final String ARG_CHILD_ID = "child_id";

    private RecyclerView eventsRecyclerView;
    private LinearLayout emptyEventsView;
    private EventAdapter eventAdapter;
    private ActivityResultLauncher<Intent> eventActivityLauncher;

    public static OverviewFragment newInstance(String babyName, String babyDob, String childId) {
        OverviewFragment fragment = new OverviewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_BABY_NAME, babyName);
        args.putString(ARG_BABY_DOB, babyDob);
        args.putString(ARG_CHILD_ID, childId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        eventActivityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // Event was created or updated, refresh the list
                    if (getArguments() != null) {
                        String childId = getArguments().getString(ARG_CHILD_ID);
                        if (childId != null && !childId.isEmpty()) {
                            loadEvents(childId);
                        }
                    }
                }
            });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_overview, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        eventsRecyclerView = view.findViewById(R.id.events_recycler_view);
        emptyEventsView = view.findViewById(R.id.empty_events_view);
        eventsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        Button addEventButton = view.findViewById(R.id.button_add_event);
        Button addFirstEventButton = view.findViewById(R.id.button_add_first_event);

        if (getArguments() != null) {
            String babyName = getArguments().getString(ARG_BABY_NAME);
            String babyDob = getArguments().getString(ARG_BABY_DOB);
            String childId = getArguments().getString(ARG_CHILD_ID);

            updateWelcomeCard(view, babyName, babyDob);

            if (childId != null && !childId.isEmpty()) {
                loadEvents(childId);
                View.OnClickListener addListener = v -> openEventActivity(null, childId);
                addEventButton.setOnClickListener(addListener);
                addFirstEventButton.setOnClickListener(addListener);
            } else {
                showEmptyState(true);
            }
        } else {
            showEmptyState(true);
        }
    }
    
    private void openEventActivity(@Nullable Event event, @NonNull String childId) {
        Intent intent = new Intent(getActivity(), EventActivity.class);
        if (event != null) { // Edit mode
            intent.putExtra(EventActivity.EXTRA_MODE, EventActivity.MODE_EDIT);
            intent.putExtra(EventActivity.EXTRA_EVENT_JSON, new Gson().toJson(event));
        } else { // Create mode
            intent.putExtra(EventActivity.EXTRA_MODE, EventActivity.MODE_CREATE);
            intent.putExtra(EventActivity.EXTRA_CHILD_ID, childId);
        }
        eventActivityLauncher.launch(intent);
    }

    private void loadEvents(String childId) {
        EventApi eventApi = ApiClient.getEventApi(requireContext());
        Call<ApiResponse<EventsResponse>> call = eventApi.findAll(childId);

        call.enqueue(new Callback<ApiResponse<EventsResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<EventsResponse>> call, Response<ApiResponse<EventsResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    EventsResponse eventsResponse = response.body().getData();
                    if (eventsResponse != null && eventsResponse.getEvents() != null) {
                        List<Event> events = eventsResponse.getEvents();
                        if (!events.isEmpty()) {
                            eventAdapter = new EventAdapter(events, OverviewFragment.this);
                            eventsRecyclerView.setAdapter(eventAdapter);
                            showEmptyState(false);
                        } else {
                            showEmptyState(true);
                        }
                    } else {
                        showEmptyState(true);
                    }
                } else {
                    showEmptyState(true);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<EventsResponse>> call, Throwable t) {
                showEmptyState(true);
            }
        });
    }

    private void showEmptyState(boolean show) {
        emptyEventsView.setVisibility(show ? View.VISIBLE : View.GONE);
        eventsRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onEditClick(Event event) {
        String childId = getArguments() != null ? getArguments().getString(ARG_CHILD_ID) : null;
        if (childId != null) {
            openEventActivity(event, childId);
        } else {
            Toast.makeText(getContext(), "Không thể sửa, thiếu thông tin bé.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDeleteClick(final Event event) {
        new AlertDialog.Builder(requireContext())
            .setTitle("Xóa sự kiện")
            .setMessage("Bạn có chắc chắn muốn xóa sự kiện '" + event.getTitle() + "'?")
            .setPositiveButton("Xóa", (dialog, which) -> {
                deleteEvent(event.getId());
            })
            .setNegativeButton("Hủy", null)
            .show();
    }

    private void deleteEvent(String eventId) {
        EventApi eventApi = ApiClient.getEventApi(requireContext());
        Call<ApiResponse<Void>> call = eventApi.delete(eventId);

        call.enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Sự kiện đã được xóa", Toast.LENGTH_SHORT).show();
                    if (getArguments() != null) {
                        String childId = getArguments().getString(ARG_CHILD_ID);
                        if (childId != null && !childId.isEmpty()) {
                            loadEvents(childId);
                        }
                    }
                } else {
                    Toast.makeText(getContext(), "Lỗi khi xóa sự kiện.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi mạng.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateWelcomeCard(View view, String babyName, String babyDob) {
        TextView profileInitial = view.findViewById(R.id.profile_initial);
        TextView welcomeSubtitle = view.findViewById(R.id.welcome_subtitle);
        TextView welcomeBirthday = view.findViewById(R.id.welcome_birthday);

        if (babyName != null && !babyName.isEmpty()) {
            profileInitial.setText(String.valueOf(babyName.charAt(0)));
            welcomeBirthday.setText("Sinh ngày " + formatDateForDisplay(babyDob));
            String age = calculateAge(babyDob);
            welcomeSubtitle.setText("Hôm nay " + babyName + " đã " + age);
        } else {
            profileInitial.setText("?");
            welcomeSubtitle.setText("Chào mừng!");
            welcomeBirthday.setText("Vui lòng chọn một bé để bắt đầu");
        }
    }

    private String calculateAge(String dob) {
        if (dob == null) return "";
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date birthDate = sdf.parse(dob);
            if (birthDate == null) return "";

            Calendar birthCal = Calendar.getInstance();
            birthCal.setTime(birthDate);
            Calendar todayCal = Calendar.getInstance();

            int years = todayCal.get(Calendar.YEAR) - birthCal.get(Calendar.YEAR);
            int months = todayCal.get(Calendar.MONTH) - birthCal.get(Calendar.MONTH);
            int days = todayCal.get(Calendar.DAY_OF_MONTH) - birthCal.get(Calendar.DAY_OF_MONTH);

            if (days < 0) {
                months--;
                days += todayCal.getActualMaximum(Calendar.DAY_OF_MONTH);
            }
            if (months < 0) {
                years--;
                months += 12;
            }

            StringBuilder ageString = new StringBuilder();
            if (years > 0) ageString.append(years).append(" năm ");
            if (months > 0) ageString.append(months).append(" tháng ");
            if (days > 0) ageString.append(days).append(" ngày tuổi");

            return ageString.toString().trim();
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }

    private String formatDateForDisplay(String apiDate) {
        if (apiDate == null || apiDate.isEmpty()) return "";
        try {
            SimpleDateFormat apiFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            apiFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = apiFormat.parse(apiDate);
            SimpleDateFormat displayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
            return displayFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return apiDate;
        }
    }
}
