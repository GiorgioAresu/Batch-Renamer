package com.giorgioaresu.batchrenamer;

import android.app.Activity;
import android.os.AsyncTask;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class UpdateFileNames_AsyncTask extends AsyncTask<List<File>,Integer,Void> {
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
    protected Void doInBackground(List<File>... arrayLists) {
        Rule_ListFragment ruleList_fragment = mListener.getRuleListFragment();
        File_ListFragment fileList_fragment = mListener.getFileListFragment();
        int counter = 0;
        int oldPerc = 0;
        int newPerc = 0;

        if (arrayLists.length > 0) {
            // Warn user if some rule is not valid
            if (!ruleList_fragment.areAllRulesValid()) {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mActivity, R.string.rule_generic_invalidrules, Toast.LENGTH_LONG).show();
                    }
                });
            }

            File.prepareForConflictFreeName();

            for (int i=0; i<arrayLists[0].size(); i++) {
                File file = arrayLists[0].get(i);
                // Get new name from result
                String ruleResult = ruleList_fragment.getNewName(file.oldName, i, fileList_fragment.getListAdapter().getCount());
                Debug.log(ruleResult);
                // Get conflict-free name
                file.newName = File.conflictFreeName(ruleResult);
                Debug.log(file.newName);
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
        public Rule_ListFragment getRuleListFragment();
        public File_ListFragment getFileListFragment();

        public void updateProgressInUI(Integer progress);

        public void setUiLoading();

        public void setUiResult();
    }
}
