package com.giorgioaresu.batchrenamer;

import android.app.Activity;
import android.os.AsyncTask;

import java.util.ArrayList;

public class UpdateFileNames_AsyncTask extends AsyncTask<ArrayList<File>, Integer, Void> {
    private updateFileNames_Callbacks mListener;

    public UpdateFileNames_AsyncTask(Activity listener) {
        super();
        mListener = (updateFileNames_Callbacks) listener;
    }

    public UpdateFileNames_AsyncTask(updateFileNames_Callbacks listener) {
        super();
        mListener = listener;
    }

    @Override
    protected void onPreExecute() {
        mListener.setUiLoading();
    }

    @Override
    protected Void doInBackground(ArrayList<File>... arrayLists) {
        ActionList_Fragment actionList_fragment = mListener.getActionListFragment();
        int counter = 0;
        int oldPerc = 0;
        int newPerc = 0;

        if (arrayLists.length > 0) {
            for (File file : arrayLists[0]) {
                file.newName = actionList_fragment.getNewName(file.currentName);

                // Check progress
                newPerc = (int) (++counter * 100f / arrayLists[0].size());
                if (newPerc > oldPerc) {
                    oldPerc = newPerc;
                    publishProgress(newPerc);
                }
                // Check if AsyncTask has been cancelled
                if (isCancelled()) {
                    break;
                }
            }
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        mListener.updateProgressInUI(values[0]);
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        mListener.updateFileNamesInUI();
        mListener.setUiResult();
    }

    public interface updateFileNames_Callbacks {
        public ActionList_Fragment getActionListFragment();

        public void updateFileNamesInUI();

        public void updateProgressInUI(Integer progress);

        public void setUiLoading();

        public void setUiResult();
    }
}
