package com.example.attendencemarker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.AttendanceViewHolder> {

    private List<AttendanceRecord> recordList;

    public AttendanceAdapter(List<AttendanceRecord> recordList) {
        this.recordList = recordList;
    }

    @NonNull
    @Override
    public AttendanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_attendance, parent, false);
        return new AttendanceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AttendanceViewHolder holder, int position) {
        AttendanceRecord record = recordList.get(position);
        holder.date.setText(record.date);
        holder.status.setText("Status: " + record.status);
        holder.location.setText("Location: " + record.location);
    }

    @Override
    public int getItemCount() {
        return recordList.size();
    }

    static class AttendanceViewHolder extends RecyclerView.ViewHolder {
        TextView date, status, location;

        public AttendanceViewHolder(@NonNull View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.dateTextView);
            status = itemView.findViewById(R.id.statusTextView);
            location = itemView.findViewById(R.id.locationTextView);
        }
    }
}
