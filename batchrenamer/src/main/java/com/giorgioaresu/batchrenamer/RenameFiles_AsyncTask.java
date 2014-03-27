package com.giorgioaresu.batchrenamer;

import android.content.Context;
import android.os.AsyncTask;

import java.util.ArrayList;

public class RenameFiles_AsyncTask extends AsyncTask<ArrayList<File>, Integer, Void> {
    private renameFiles_Callbacks mListener;

    private Context context;
    private RenamingNotification notification = new RenamingNotification();

    private int completed = 0;
    private int failed = 0;

    /**
     * Syncronize failed writing on this object to guarantee consistence
     */
    private Object mLock = new Object();

    public RenameFiles_AsyncTask(renameFiles_Callbacks mListener) {
        super();
        this.mListener = mListener;
        context = mListener.getContext();
    }

    @Override
    protected void onPreExecute() {
        notification.notifyIndeterminate(context);
        mListener.setUiLoading();
    }

    @Override
    protected Void doInBackground(ArrayList<File>... arrayLists) {
        int size = arrayLists[0].size();
        // Allow asking the user to give superuser permission even if
        // the previous time it declined the request
        SuHelper.resetSuStatus();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        publishProgress(0, size);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < size; i++) {
            File f = arrayLists[0].get(i);
            File.RENAME result = f.rename();
            switch (result) {
                case SUCCESSFUL:
                    // Correctly renamed file
                    Debug.log("Rename successful for file: " + f.oldName + " to: " + f.newName);
                    break;
                case FAILED_GENERIC:
                    // Failed with unknown cause
                    Debug.logError("Rename failed for file: " + f.oldName);
                    break;
                case FAILED_PERMISSION:
                    // Insufficient permission on folder or file
                    Debug.logError("Rename failed (insufficient permissions) for file: " + f.oldName);
                    break;
                case FAILED_NOSOURCEFILE:
                    // Source file doesn't exist
                    Debug.logError("Rename failed (file doesn't exist) for file: " + f.oldName);
                    break;
                case FAILED_DESTINATIONEXISTS:
                    // Destination file already exists
                    Debug.logError("Rename failed (file already exists) for file: " + f.oldName);
                    break;
            }
            publishProgress(i + 1, size, result.getID());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        if (values.length > 2) {
            synchronized (mLock) {
                if (File.RENAME.SUCCESSFUL.compare(values[2])) {
                    completed++;
                } else {
                    failed++;
                }
            }
        }
        notification.notify(context, values[0], values[1], completed, failed, 0);

        if (values.length > 2) {
            mListener.updateProgressInUI(values[0], values[1], File.RENAME.getValue(values[2]));
        } else {
            mListener.updateProgressInUI(values[0], values[1], null);
        }
    }

    @Override
    protected void onCancelled() {
        notification.cancel(context);
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        notification.notifyCompleted(context, completed, failed);
        mListener.setUiResult();
    }

    public interface renameFiles_Callbacks {
        public void updateProgressInUI(Integer progress, Integer elements, File.RENAME result);

        public void setUiLoading();

        public void setUiResult();

        public Context getContext();
    }
}
