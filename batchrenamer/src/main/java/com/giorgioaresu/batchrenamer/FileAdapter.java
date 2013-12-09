package com.giorgioaresu.batchrenamer;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class FileAdapter extends ArrayAdapter<File> {
    Context context;
    int layoutResourceId;
    ArrayList<File> files;

    public FileAdapter(Context context, int layoutResourceId, ArrayList<File> files) {
        super(context, layoutResourceId, files);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.files = files;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        FileHolder holder;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new FileHolder();
            try {
                holder.currentName = (TextView) row.findViewById(R.id.file_list_item_current);
                holder.newName = (TextView) row.findViewById(R.id.file_list_item_preview);
                row.setTag(holder);
            } catch (Exception e) {
                Log.e(getClass().getCanonicalName(), "Exception: " + e.getMessage() + ". Printing stack trace...");
                e.printStackTrace();
            }
        } else {
            holder = (FileHolder) row.getTag();
        }

        File file = files.get(position);
        holder.currentName.setText(file.currentName);
        holder.newName.setText(file.newName);

        return row;
    }

    static class FileHolder {
        TextView currentName;
        TextView newName;
    }
}
