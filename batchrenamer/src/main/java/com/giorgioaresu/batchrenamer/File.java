package com.giorgioaresu.batchrenamer;

import android.os.Parcel;
import android.os.Parcelable;

public class File implements Parcelable {
    String currentName;
    String newName;

    private File(Parcel in) {
        currentName = in.readString();
        newName = in.readString();
    }

    public File(String name) {
        newName = currentName = name;
    }

    // TODO: This constructor should be removed when file-picking system is implemented
    public File(String currentName, String newName) {
        this.currentName = currentName;
        this.newName = newName;
    }

    public int Rename() {
        // TODO: implement file rename
        try {
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
        parcel.writeString(currentName);
        parcel.writeString(newName);
    }
}
