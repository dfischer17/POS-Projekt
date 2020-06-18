package com.example.cloudclient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimelineItem {
    private String description;
    private LocalDateTime currentDate;
    private DriveAction driveAction;
    private DateTimeFormatter formatter;

    public TimelineItem(String description, LocalDateTime currentDate, DriveAction driveAction) {
        this.description = description;
        this.currentDate = currentDate;
        this.driveAction = driveAction;
        formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getCurrentDate() {
        return currentDate;
    }

    public DriveAction getDriveAction() {
        return driveAction;
    }

    public String toCSVString(){
        return description + ";" + currentDate.format(formatter) + ";" + driveAction.name();
    }
}
