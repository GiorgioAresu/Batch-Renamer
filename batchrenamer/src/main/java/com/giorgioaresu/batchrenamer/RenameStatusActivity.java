package com.giorgioaresu.batchrenamer;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

import java.util.ArrayList;

public class RenameStatusActivity extends Activity implements File_ListFragment.FileFragmentInterface {
    ArrayList<File> files;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_renamestatus);
    }

    @Override
    public void onFileSelected(File file) {
        // TODO: Implement interface
        Toast.makeText(this, file.status.name(), Toast.LENGTH_LONG).show();
    }
}
