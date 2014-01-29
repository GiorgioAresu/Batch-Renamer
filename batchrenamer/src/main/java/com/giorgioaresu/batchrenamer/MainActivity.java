package com.giorgioaresu.batchrenamer;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;

public class MainActivity extends Activity implements FileList_Fragment.FileFragmentInterface, ActionList_Fragment.OnActionSelectedListener, ActionEdit_Fragment.actionEditFragment_Callbacks {

    private ArrayList<File> mFiles;
    private ActionList_Fragment actionList_fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {

        }


        FragmentManager mFragmentManager = getFragmentManager();
        actionList_fragment = (ActionList_Fragment) mFragmentManager.findFragmentById(R.id.action_fragment);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        actionList_fragment.populateActionMenu(menu.findItem(R.id.action_newAction).getSubMenu());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
            default:
                if (actionList_fragment.onNewActionSelected(item)) {
                    return true;
                } else {
                    return super.onOptionsItemSelected(item);
                }
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        //menu.setGroupVisible(R.id.menugroup_edit_action, ciao);

        //menu.findItem(R.id.action_start).setVisible(!ciao);
        //menu.findItem(R.id.action_settings).setVisible(!ciao);
        //ciao=!ciao;
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onFileSelected(File file) {
        // TODO: Implement interface
    }

    @Override
    public ArrayList<File> provideFiles() {
        mFiles = new ArrayList<>();
        mFiles.add(new File("Lost.3x01.Storia.Di.Due.Citta.ITA.DVDRip.XviD-NovaRip"));
        mFiles.add(new File("Lost.3x01.Storia.Di.Due.Citta.ITA.DVDRip.XviD-NovaRip"));
        mFiles.add(new File("Lost.3x01.Storia.Di.Due.Citta.ITA.DVDRip.XviD-NovaRip"));
        mFiles.add(new File("Lost.3x01.Storia.Di.Due.Citta.ITA.DVDRip.XviD-NovaRip"));
        mFiles.add(new File("Lost.3x01.Storia.Di.Due.Citta.ITA.DVDRip.XviD-NovaRip"));
        mFiles.add(new File("Lost.3x01.Storia.Di.Due.Citta.ITA.DVDRip.XviD-NovaRip"));
        mFiles.add(new File("Lost.3x01.Storia.Di.Due.Citta.ITA.DVDRip.XviD-NovaRip"));
        mFiles.add(new File("Lost.3x01.Storia.Di.Due.Citta.ITA.DVDRip.XviD-NovaRip"));
        mFiles.add(new File("Lost.3x01.Storia.Di.Due.Citta.ITA.DVDRip.XviD-NovaRip"));
        mFiles.add(new File("Lost.3x01.Storia.Di.Due.Citta.ITA.DVDRip.XviD-NovaRip"));
        mFiles.add(new File("Lost.3x01.Storia.Di.Due.Citta.ITA.DVDRip.XviD-NovaRip"));
        mFiles.add(new File("Lost.3x01.Storia.Di.Due.Citta.ITA.DVDRip.XviD-NovaRip"));
        mFiles.add(new File("Lost.3x01.Storia.Di.Due.Citta.ITA.DVDRip.XviD-NovaRip"));
        mFiles.add(new File("Lost.3x01.Storia.Di.Due.Citta.ITA.DVDRip.XviD-NovaRip"));
        mFiles.add(new File("Lost.3x01.Storia.Di.Due.Citta.ITA.DVDRip.XviD-NovaRip"));
        mFiles.add(new File("Lost.3x01.Storia.Di.Due.Citta.ITA.DVDRip.XviD-NovaRip"));
        mFiles.add(new File("Lost.3x01.Storia.Di.Due.Citta.ITA.DVDRip.XviD-NovaRip"));
        mFiles.add(new File("Lost.3x01.Storia.Di.Due.Citta.ITA.DVDRip.XviD-NovaRip"));
        mFiles.add(new File("Lost.3x01.Storia.Di.Due.Citta.ITA.DVDRip.XviD-NovaRip"));
        mFiles.add(new File("Lost.3x01.Storia.Di.Due.Citta.ITA.DVDRip.XviD-NovaRip"));
        mFiles.add(new File("Lost.3x01.Storia.Di.Due.Citta.ITA.DVDRip.XviD-NovaRip"));
        mFiles.add(new File("Lost.3x01.Storia.Di.Due.Citta.ITA.DVDRip.XviD-NovaRip"));
        mFiles.add(new File("Lost.3x01.Storia.Di.Due.Citta.ITA.DVDRip.XviD-NovaRip"));
        mFiles.add(new File("Lost.3x01.Storia.Di.Due.Citta.ITA.DVDRip.XviD-NovaRip"));
        mFiles.add(new File("Lost.3x01.Storia.Di.Due.Citta.ITA.DVDRip.XviD-NovaRip"));
        mFiles.add(new File("Lost.3x01.Storia.Di.Due.Citta.ITA.DVDRip.XviD-NovaRip"));
        mFiles.add(new File("Lost.3x01.Storia.Di.Due.Citta.ITA.DVDRip.XviD-NovaRip"));
        mFiles.add(new File("Lost.3x01.Storia.Di.Due.Citta.ITA.DVDRip.XviD-NovaRip"));
        mFiles.add(new File("Lost.3x01.Storia.Di.Due.Citta.ITA.DVDRip.XviD-NovaRip"));
        mFiles.add(new File("Lost.3x01.Storia.Di.Due.Citta.ITA.DVDRip.XviD-NovaRip"));
        mFiles.add(new File("Lost.3x01.Storia.Di.Due.Citta.ITA.DVDRip.XviD-NovaRip"));
        mFiles.add(new File("Lost.3x01.Storia.Di.Due.Citta.ITA.DVDRip.XviD-NovaRip"));
        mFiles.add(new File("Lost.3x01.Storia.Di.Due.Citta.ITA.DVDRip.XviD-NovaRip"));
        mFiles.add(new File("Lost.3x01.Storia.Di.Due.Citta.ITA.DVDRip.XviD-NovaRip"));
        mFiles.add(new File("Lost.3x01.Storia.Di.Due.Citta.ITA.DVDRip.XviD-NovaRip"));
        mFiles.add(new File("Lost.3x01.Storia.Di.Due.Citta.ITA.DVDRip.XviD-NovaRip"));
        mFiles.add(new File("Lost.3x01.Storia.Di.Due.Citta.ITA.DVDRip.XviD-NovaRip"));
        mFiles.add(new File("Lost.3x01.Storia.Di.Due.Citta.ITA.DVDRip.XviD-NovaRip"));
        mFiles.add(new File("Lost.3x01.Storia.Di.Due.Citta.ITA.DVDRip.XviD-NovaRip"));
        mFiles.add(new File("Lost.3x01.Storia.Di.Due.Citta.ITA.DVDRip.XviD-NovaRip"));
        mFiles.add(new File("Lost.3x01.Storia.Di.Due.Citta.ITA.DVDRip.XviD-NovaRip"));
        mFiles.add(new File("Lost.3x01.Storia.Di.Due.Citta.ITA.DVDRip.XviD-NovaRip"));
        mFiles.add(new File("Lost.3x01.Storia.Di.Due.Citta.ITA.DVDRip.XviD-NovaRip"));
        mFiles.add(new File("Lost.3x01.Storia.Di.Due.Citta.ITA.DVDRip.XviD-NovaRip"));
        mFiles.add(new File("Lost.3x01.Storia.Di.Due.Citta.ITA.DVDRip.XviD-NovaRip"));
        mFiles.add(new File("Lost.3x01.Storia.Di.Due.Citta.ITA.DVDRip.XviD-NovaRip"));

        return mFiles;
    }

    @Override
    public void onActionSelected(Action action) {
        showDialog(action);
    }

    public void showDialog(Action action) {
        DialogFragment mDialogFragment = ActionEdit_Fragment.newInstance(action);
        mDialogFragment.show(getFragmentManager(), "editAction");
    }

    @Override
    public void notifyDataSetChanged() {
        FragmentManager mFragmentManager = getFragmentManager();
        ActionList_Fragment actionListFragment = (ActionList_Fragment) mFragmentManager.findFragmentById(R.id.action_fragment);
        ActionAdapter actionAdapter = (ActionAdapter) actionListFragment.getListAdapter();
        actionAdapter.notifyDataSetChanged();
    }
}
