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

import java.util.List;

public class BabyAdapter extends RecyclerView.Adapter<BabyAdapter.BabyViewHolder> {

    private List<Baby> babies;
    private OnBabyDeletedListener onBabyDeletedListener;

    public interface OnBabyDeletedListener {
        void onBabyDeleted(Baby baby);
    }

    public BabyAdapter(List<Baby> babies, OnBabyDeletedListener onBabyDeletedListener) {
        this.babies = babies;
        this.onBabyDeletedListener = onBabyDeletedListener;
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
        holder.babyDob.setText(baby.getDob());
        holder.babyInitial.setText(String.valueOf(baby.getFirstName().charAt(0)));

        holder.deleteButton.setOnClickListener(v -> {
            if (onBabyDeletedListener != null) {
                onBabyDeletedListener.onBabyDeleted(baby);
            }
        });
    }

    @Override
    public int getItemCount() {
        return babies.size();
    }

    static class BabyViewHolder extends RecyclerView.ViewHolder {
        TextView babyName, babyDob, babyInitial;
        ImageView deleteButton;

        public BabyViewHolder(@NonNull View itemView) {
            super(itemView);
            babyName = itemView.findViewById(R.id.baby_name);
            babyDob = itemView.findViewById(R.id.baby_dob);
            babyInitial = itemView.findViewById(R.id.baby_initial);
            deleteButton = itemView.findViewById(R.id.delete_baby_button);
        }
    }
}
