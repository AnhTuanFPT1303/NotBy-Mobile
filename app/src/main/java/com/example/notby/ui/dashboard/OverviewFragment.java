package com.example.notby.ui.dashboard;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.notby.R;
import com.google.android.material.button.MaterialButton;

public class OverviewFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_overview, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MaterialButton addEventButton = view.findViewById(R.id.button_add_event);
        ImageButton editEventButton = view.findViewById(R.id.button_edit_event);
        ImageButton deleteEventButton = view.findViewById(R.id.button_delete_event);

        addEventButton.setOnClickListener(v -> showEventDialog(false));
        editEventButton.setOnClickListener(v -> showEventDialog(true));
        deleteEventButton.setOnClickListener(v -> showDeleteConfirmationDialog());
    }

    private void showEventDialog(boolean isEditing) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_event, null);

        // Customize dialog for editing or adding
        TextView dialogTitle = dialogView.findViewById(R.id.dialog_title);
        MaterialButton actionButton = dialogView.findViewById(R.id.button_action);

        if (isEditing) {
            dialogTitle.setText("Sửa sự kiện");
            actionButton.setText("Lưu");
        } else {
            dialogTitle.setText("Thêm sự kiện mới");
            actionButton.setText("Thêm");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        MaterialButton cancelButton = dialogView.findViewById(R.id.button_cancel);
        cancelButton.setOnClickListener(v -> dialog.dismiss());
        actionButton.setOnClickListener(v -> {
            // Handle adding or editing the event here
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa sự kiện này không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    // Handle delete logic here
                    Toast.makeText(getContext(), "Sự kiện đã được xóa", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
