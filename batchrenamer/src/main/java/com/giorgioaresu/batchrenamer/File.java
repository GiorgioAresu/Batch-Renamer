package com.giorgioaresu.batchrenamer;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class File implements Parcelable {
    Uri fileUri;
    String currentName;
    String newName;

    private File(Parcel in) {
        fileUri = in.readParcelable((ClassLoader) Uri.CREATOR);
        currentName = in.readString();
        newName = in.readString();
    }

    public File(Uri uri) {
        fileUri = uri;
        currentName = newName = fileUri.getLastPathSegment();
    }

    public File(String name) {
        this(Uri.parse(name));
    }

    // TODO: This constructor should be removed when file-picking system is implemented
    public File(String currentName, String newName) {
        this(Uri.parse(currentName));
        this.currentName = currentName;
        this.newName = newName;
    }

    public int Rename() {
        // TODO: implement file rename
        try {
            java.io.File file = new java.io.File(fileUri.getPath());
            Log.d("file", "Name: " + file.getName() + ", Path: " + file.getAbsolutePath() + ", uriScheme: " + fileUri.getScheme() + ", canWrite: " + (file.canWrite() ? "true" : "false"));
            return 0;
        } catch (Exception e) {
            return 1;
        }
    }

    public static final Parcelable.Creator<File> CREATOR
            = new Parcelable.Creator<File>() {
        public File createFromParcel(Parcel in) {
            return new File(in);
        }

        public File[] newArray(int size) {
            return new File[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        Uri.writeToParcel(parcel, fileUri);
        parcel.writeString(currentName);
        parcel.writeString(newName);
    }
}
