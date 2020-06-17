package com.example.cloudclient;

import android.content.Context;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.api.services.drive.model.File;
import java.util.List;
import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class DriveContentAdapter extends BaseAdapter {
    private List<File> curDirectory;
    private int layoutId;
    private LayoutInflater inflater;
    ImageView iv;

    public DriveContentAdapter(List<File> curDirectory, int layoutId, Context context) {
        this.curDirectory = curDirectory;
        this.layoutId = layoutId;
        this.inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return curDirectory.size();
    }

    @Override
    public Object getItem(int position) {
        return curDirectory.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        File file = curDirectory.get(position);
        View listItem = (convertView == null) ? inflater.inflate(this.layoutId, null) : convertView;
        ((TextView) listItem.findViewById(R.id.fileNameListitem)).setText(file.getName());
        if(file.getName().equals("Back")){
            listItem.findViewById(R.id.iconListItem).setBackgroundResource(R.drawable.ic_arrow_back);
        }
        else if (file.getMimeType().equals(DriveExplorer.folderMimeType)){
            listItem.findViewById(R.id.iconListItem).setBackgroundResource(R.drawable.ic_folder);
        }
        else {
            listItem.findViewById(R.id.iconListItem).setBackgroundResource(R.drawable.ic_file);
        }
        return listItem;
    }
}
