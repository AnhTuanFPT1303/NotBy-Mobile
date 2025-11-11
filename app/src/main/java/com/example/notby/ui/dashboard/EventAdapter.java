package com.example.notby.ui.dashboard;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notby.R;
import com.example.notby.data.model.Event;
import com.google.android.material.chip.Chip;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<Event> events;
    private OnEventListener listener;

    public interface OnEventListener {
        void onEditClick(Event event);
        void onDeleteClick(Event event);
    }

    public EventAdapter(List<Event> events, OnEventListener listener) {
        this.events = events;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);
        holder.bind(event, listener);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        private TextView eventTitle, eventDates;
        private Chip eventTypeChip;
        private ImageButton editButton, deleteButton;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            eventTitle = itemView.findViewById(R.id.event_title);
            eventDates = itemView.findViewById(R.id.event_dates);
            eventTypeChip = itemView.findViewById(R.id.event_type_chip);
            editButton = itemView.findViewById(R.id.button_edit_event);
            deleteButton = itemView.findViewById(R.id.button_delete_event);
        }

        public void bind(final Event event, final OnEventListener listener) {
            eventTitle.setText(event.getTitle());
            String dateRange = formatDateForDisplay(event.getStartAt()) + " - " + formatDateForDisplay(event.getEndAt());
            eventDates.setText(dateRange);
            eventTypeChip.setText(event.getEventType());

            editButton.setOnClickListener(v -> listener.onEditClick(event));
            deleteButton.setOnClickListener(v -> listener.onDeleteClick(event));
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
}
