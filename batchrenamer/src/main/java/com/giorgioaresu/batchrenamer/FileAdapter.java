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
        holder.currentName.setText(file.oldName);
        holder.newName.setText(file.newName);
        View status = holder.statusView;
        if (status != null) {
            int res;
            switch (Math.round((float) Math.random() * 3)) {
                case 1:
                    res = R.drawable.bg_card_green;
                    break;
                case 2:
                    res = R.drawable.bg_card_orange;
                    break;
                default:
                    res = R.drawable.bg_card_red;
            }
            status.setBackgroundResource(res);
        }

        return v;
    }

    /*private int getColorForStatus(File.RENAME status) {
        int color;
        switch (status) {
            case SUCCESSFUL:
                // Correctly renamed file
                color = context.getResources().getColor(R.color.file_status0);
                break;
            case FAILED_GENERIC:
                // Failed with unknown cause
                color = context.getResources().getColor(R.color.file_status1);
                break;
            case FAILED_PERMISSION:
                // Insufficient permission on folder or file
                color = context.getResources().getColor(R.color.file_status2);
                break;
            case FAILED_NOSOURCEFILE:
                // Source file doesn't exist
                color = context.getResources().getColor(R.color.file_status3);
                break;
            default: //FAILED_DESTINATIONEXISTS
                // Destination file already exists
                color = context.getResources().getColor(R.color.file_status4);
                break;
        }
        return color;
    }*/

    static class FileHolder {
        TextView currentName;
        TextView newName;
        View statusView;

        public FileHolder(View v) {
            currentName = (TextView) v.findViewById(R.id.file_list_item_old);
            newName = (TextView) v.findViewById(R.id.file_list_item_new);
            statusView = ((v.getId() == R.id.file_list_item_status) ? v : null);
        }
    }
}
