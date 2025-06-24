package com.example.attendencemarker;

public class AttendanceRecord {
    public String date;
    public String status;
    public String location;

    public AttendanceRecord() {}

    public AttendanceRecord(String date, String status, String location) {
        this.date = date;
        this.status = status;
        this.location = location;
    }
}
