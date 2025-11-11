package com.example.notby.ui.diary;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notby.R;
import com.example.notby.data.model.DiaryEntry;

import java.util.List;

public class DiaryAdapter extends RecyclerView.Adapter<DiaryAdapter.DiaryViewHolder> {

    private List<DiaryEntry> diaryEntries;

    public DiaryAdapter(List<DiaryEntry> diaryEntries) {
        this.diaryEntries = diaryEntries;
    }

    @NonNull
    @Override
    public DiaryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_diary_milestone, parent, false);
        return new DiaryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DiaryViewHolder holder, int position) {
        DiaryEntry diaryEntry = diaryEntries.get(position);
        holder.title.setText(diaryEntry.getTitle());
        holder.date.setText(diaryEntry.getCreatedAt());
        holder.notes.setText(diaryEntry.getContent());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), DiaryDetailActivity.class);
            intent.putExtra(DiaryDetailActivity.EXTRA_DIARY_ID, diaryEntry.getId());
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return diaryEntries.size();
    }

    static class DiaryViewHolder extends RecyclerView.ViewHolder {
        TextView title, date, notes;

        public DiaryViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.milestone_title);
            date = itemView.findViewById(R.id.milestone_date);
            notes = itemView.findViewById(R.id.milestone_notes);
        }
    }
}
