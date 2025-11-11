package com.example.notby.ui.diary;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.notby.R;
import com.example.notby.data.model.DiaryEntry;
import com.example.notby.data.model.MediaFile;
import com.google.android.material.chip.Chip;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

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
        holder.bind(diaryEntry);
    }

    @Override
    public int getItemCount() {
        return diaryEntries.size();
    }

    static class DiaryViewHolder extends RecyclerView.ViewHolder {
        TextView title, date, notes;
        ImageView image;
        Chip babyNameChip, categoryChip;

        public DiaryViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.milestone_title);
            date = itemView.findViewById(R.id.milestone_date);
            notes = itemView.findViewById(R.id.milestone_notes);
            image = itemView.findViewById(R.id.milestone_image);
            babyNameChip = itemView.findViewById(R.id.milestone_baby_name_chip);
            categoryChip = itemView.findViewById(R.id.milestone_category_chip);
        }

        public void bind(DiaryEntry diaryEntry) {
            title.setText(diaryEntry.getTitle());
            date.setText(formatDate(diaryEntry.getCreatedAt()));
            notes.setText(diaryEntry.getContent());
            categoryChip.setText(diaryEntry.getCategory());

            // Use the new getChildName() method
            String childName = diaryEntry.getChildName();
            if(childName != null) {
                 babyNameChip.setText(childName);
                 babyNameChip.setVisibility(View.VISIBLE);
            } else {
                 babyNameChip.setVisibility(View.GONE);
            }
           

            if (diaryEntry.getImageUrls() != null && !diaryEntry.getImageUrls().isEmpty()) {
                MediaFile firstImage = diaryEntry.getImageUrls().get(0);
                if (firstImage != null && firstImage.getFileUrl() != null) {
                    image.setVisibility(View.VISIBLE);
                    Glide.with(itemView.getContext())
                         .load(firstImage.getFileUrl())
                         .into(image);
                } else {
                    image.setVisibility(View.GONE);
                }
            } else {
                image.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), DiaryDetailActivity.class);
                intent.putExtra(DiaryDetailActivity.EXTRA_DIARY_ID, diaryEntry.getId());
                v.getContext().startActivity(intent);
            });
        }

        private String formatDate(String dateString) {
            if (dateString == null) return "";
            try {
                SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
                originalFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                SimpleDateFormat targetFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
                Date date = originalFormat.parse(dateString);
                return targetFormat.format(date);
            } catch (ParseException e) {
                return dateString; // Fallback
            }
        }
    }
}
