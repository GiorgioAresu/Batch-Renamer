package com.giorgioaresu.batchrenamer;

import android.app.Activity;
import android.os.AsyncTask;
import android.widget.Toast;

import java.util.ArrayList;

public class UpdateFileNames_AsyncTask extends AsyncTask<ArrayList<File>, Integer, Void> {
    Activity mActivity;
    private updateFileNames_Callbacks mListener;

    public UpdateFileNames_AsyncTask(Activity activity, updateFileNames_Callbacks listener) {
        super();
        mActivity = activity;
        mListener = listener;
    }

    @Override
    protected void onPreExecute() {
        mListener.setUiLoading();
    }

    @Override
    protected Void doInBackground(ArrayList<File>... arrayLists) {
        Action_ListFragment actionList_fragment = mListener.getActionListFragment();
        File_ListFragment fileList_fragment = mListener.getFileListFragment();
        int counter = 0;
        int oldPerc = 0;
        int newPerc = 0;

        if (arrayLists.length > 0) {
            // Warn user if some action is not valid
            if (!actionList_fragment.areAllActionsValid()) {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mActivity, R.string.some_action_invalid, Toast.LENGTH_LONG).show();
                    }
                });
            }

            for (int i=0; i<arrayLists[0].size(); i++) {
                File file = arrayLists[0].get(i);
                file.newName = actionList_fragment.getNewName(file.oldName, i, fileList_fragment.getListAdapter().getCount());

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
        mListener.setUiResult();
    }

    public interface updateFileNames_Callbacks {
        public Action_ListFragment getActionListFragment();
        public File_ListFragment getFileListFragment();

        public void updateProgressInUI(Integer progress);

        public void setUiLoading();

        public void setUiResult();
    }
}
