package com.giorgioaresu.batchrenamer;

import android.app.Activity;
import android.content.Context;
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
        View v = convertView;
        FileHolder holder;

        if (v == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            v = inflater.inflate(layoutResourceId, parent, false);

            holder = new FileHolder(v);
            v.setTag(holder);
        } else {
            holder = (FileHolder) v.getTag();
        }

        File file = files.get(position);
        holder.currentName.setText(file.currentName);
        holder.newName.setText(file.newName);

        return v;
    }

    static class FileHolder {
        TextView currentName;
        TextView newName;

        public FileHolder(View v) {
            currentName = (TextView) v.findViewById(R.id.file_list_item_current);
            newName = (TextView) v.findViewById(R.id.file_list_item_preview);
        }
    }
}
