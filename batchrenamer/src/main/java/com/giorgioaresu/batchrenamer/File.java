package com.giorgioaresu.batchrenamer;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.List;

import eu.chainfire.libsuperuser.Shell;

public class File implements Parcelable {
    Uri fileUri;
    String oldName;
    String newName;
    RENAME status;

    public enum RENAME {
        SUCCESSFUL(0),
        FAILED_GENERIC(3),
        FAILED_NOSOURCEFILE(4),
        FAILED_PERMISSION(5),
        FAILED_DESTINATIONEXISTS(6);

        private final int id;

        private RENAME(int id) {
            this.id = id;
        }

        public int getID() {
            return id;
        }

        public boolean compare(int i) {
            return id == i;
        }

        public static RENAME getValue(int _id) {
            RENAME[] As = RENAME.values();
            for (int i = 0; i < As.length; i++) {
                if (As[i].compare(_id))
                    return As[i];
            }
            // Value not recognized. Just return generic error.
            return FAILED_GENERIC;
        }
    }

    private File(Parcel in) {
        fileUri = Uri.CREATOR.createFromParcel(in);
        oldName = in.readString();
        newName = in.readString();
        status = RENAME.getValue(in.readInt());
    }

    public File(Uri uri) {
        fileUri = uri;
        oldName = fileUri.getLastPathSegment();
        // TODO: Double-check if it's safe or it's better to do something more complex (maybe there are actions already loaded that doesn't fire an update?)
        newName = oldName;
        status = RENAME.SUCCESSFUL;
    }

    public RENAME rename() {
        // TODO: support different folder generated by rename (ie. Add "lorem/ipsum" to file")
        java.io.File dir = new java.io.File(fileUri.getPath().substring(0, fileUri.getPath().lastIndexOf(java.io.File.separator)));
        java.io.File from = new java.io.File(dir, oldName);
        java.io.File to = new java.io.File(dir, newName);

        if (!dir.canRead() && !dir.canWrite() && !from.exists() && !to.exists()) {
            // Every check fails, it's highly probable we're required to use superuser permissions
            // Try to obtain root access
            Log.d("rename", "Trying to obtain root access");
            try {
                if (SuHelper.isSuAvailable()) {
                    String shell = "sh";
                    String scriptPath = MainActivity.scriptFile.getCanonicalPath();
                    String command = String.format("%1$s \"%2$s\" \"%3$s\" \"%4$s\"",
                            shell,
                            scriptPath,
                            from.getCanonicalPath(),
                            to.getCanonicalPath());
                    List<String> result = Shell.SU.run(command);
                    if (result.size() > 0) {
                        status = RENAME.getValue(Integer.valueOf(result.get(0)));
                    }
                } else {
                    Log.d("rename", "su not available");
                }
            } catch (Exception e) {
            }
            status = RENAME.FAILED_PERMISSION;
        } else if (!from.exists()) {
            // Source file doesn't exist
            status = RENAME.FAILED_NOSOURCEFILE;
        } else if (!(from.canRead() && dir.canWrite())) {
            // Can't read source file or can't write on directory
            status = RENAME.FAILED_PERMISSION;
        } else if (to.exists()) {
            // Destination file already exists
            status = RENAME.FAILED_DESTINATIONEXISTS;
        } else if (from.renameTo(to)) {
            // Rename successful
            status = RENAME.SUCCESSFUL;
        } else {
            // Every check succeeded but rename failed
            status = RENAME.FAILED_GENERIC;
        }

        return status;
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
        parcel.writeString(oldName);
        parcel.writeString(newName);
        parcel.writeInt(status.getID());
    }
}
