package com.giorgioaresu.batchrenamer;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;

public class RenameFiles_AsyncTask extends AsyncTask<ArrayList<File>, Integer, Void> {
    private renameFiles_Callbacks mListener;

    private Context context;
    private RenamingNotification notification = new RenamingNotification();

    public RenameFiles_AsyncTask(renameFiles_Callbacks mListener) {
        super();
        this.mListener = mListener;
        context = mListener.getContext();
    }

    @Override
    protected void onPreExecute() {
        notification.notify(context, 0, 0);
        mListener.setUiLoading();
    }

    @Override
    protected Void doInBackground(ArrayList<File>... arrayLists) {
        int size = arrayLists[0].size();

        for (int i = 0; i < size; i++) {
            Log.v("Task", "Processing file " + (i + 1) + " of " + size);
            publishProgress(i + 1, size);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        notification.notify(context, values[0], values[1]);
        mListener.updateProgressInUI(values[0]);
    }

    @Override
    protected void onCancelled() {
        notification.cancel(context);
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        Log.d("Rename task", "Hiding notification");
        notification.notify(context, -1, 0);
        mListener.setUiResult();
        // TODO : Update current file names with new ones
    }

    public interface renameFiles_Callbacks {
        public void updateProgressInUI(Integer progress);

        public void setUiLoading();

        public void setUiResult();

        public Context getContext();
    }
}
