package com.example.notby.ui.baby;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notby.R;
import com.example.notby.data.model.Baby;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BabyAdapter extends RecyclerView.Adapter<BabyAdapter.BabyViewHolder> {

    private List<Baby> babies;
    private OnBabyDeletedListener onBabyDeletedListener;
    private OnBabyEditedListener onBabyEditedListener;
    private OnBabySelectedListener onBabySelectedListener;

    public interface OnBabyDeletedListener {
        void onBabyDeleted(Baby baby);
    }

    public interface OnBabyEditedListener {
        void onBabyEdited(Baby baby);
    }

    public interface OnBabySelectedListener {
        void onBabySelected(Baby baby);
    }

    public BabyAdapter(List<Baby> babies, OnBabyDeletedListener onBabyDeletedListener, OnBabyEditedListener onBabyEditedListener, OnBabySelectedListener onBabySelectedListener) {
        this.babies = babies;
        this.onBabyDeletedListener = onBabyDeletedListener;
        this.onBabyEditedListener = onBabyEditedListener;
        this.onBabySelectedListener = onBabySelectedListener;
    }

    @NonNull
    @Override
    public BabyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_baby, parent, false);
        return new BabyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BabyViewHolder holder, int position) {
        Baby baby = babies.get(position);
        holder.babyName.setText(baby.getFirstName() + " " + baby.getLastName());
        holder.babyDob.setText(formatDate(baby.getDob()));
        holder.babyInitial.setText(String.valueOf(baby.getFirstName().charAt(0)));

        holder.itemView.setOnClickListener(v -> {
            if (onBabySelectedListener != null) {
                onBabySelectedListener.onBabySelected(baby);
            }
        });

        holder.deleteButton.setOnClickListener(v -> {
            if (onBabyDeletedListener != null) {
                onBabyDeletedListener.onBabyDeleted(baby);
            }
        });

        holder.editButton.setOnClickListener(v -> {
            if (onBabyEditedListener != null) {
                onBabyEditedListener.onBabyEdited(baby);
            }
        });
    }

    @Override
    public int getItemCount() {
        return babies.size();
    }

    private String formatDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return "";
        }
        try {
            SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            SimpleDateFormat targetFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date date = originalFormat.parse(dateString);
            return targetFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return dateString; // return original string if parsing fails
        }
    }

    static class BabyViewHolder extends RecyclerView.ViewHolder {
        TextView babyName, babyDob, babyInitial;
        ImageView deleteButton, editButton;

        public BabyViewHolder(@NonNull View itemView) {
            super(itemView);
            babyName = itemView.findViewById(R.id.baby_name);
            babyDob = itemView.findViewById(R.id.baby_dob);
            babyInitial = itemView.findViewById(R.id.baby_initial);
            deleteButton = itemView.findViewById(R.id.delete_baby_button);
            editButton = itemView.findViewById(R.id.edit_baby_button);
        }
    }
}
