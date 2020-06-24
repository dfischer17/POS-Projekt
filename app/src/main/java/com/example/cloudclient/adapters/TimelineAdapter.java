package com.example.cloudclient.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.cloudclient.R;
import com.example.cloudclient.data.TimelineItem;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class TimelineAdapter extends BaseAdapter {

    private List<TimelineItem> timeline = new ArrayList<>();
    private int layoutId;
    private LayoutInflater inflater;
    DateTimeFormatter formatter;


    public TimelineAdapter(List<TimelineItem> timeline, int layoutId, Context context) {
        this.timeline = timeline;
        this.layoutId = layoutId;
        this.inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    }

    @Override
    public int getCount() {
        return timeline.size();
    }

    @Override
    public Object getItem(int position) {
        return timeline.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TimelineItem timelineItem = timeline.get(position);
        View listItem = (convertView == null) ? inflater.inflate(layoutId, null) : convertView;

        String desc = timelineItem.getDescription();
        String curDate = timelineItem.getCurrentDate().format(formatter);
        String driveAction = timelineItem.getDriveAction().name();

        ((TextView) listItem.findViewById(R.id.timelineDescription)).setText("Name: " + desc);
        ((TextView) listItem.findViewById(R.id.timelineDate)).setText("Datum: " + curDate);
        ((TextView) listItem.findViewById(R.id.timelineAction)).setText("Action: " + driveAction);

        return listItem;
    }
}
