/*
 * Copyright (C) 2012-2013 Jorrit "Chainfire" Jongma
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.giorgioaresu.batchrenamer;

import android.os.Environment;
import android.util.Log;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Utility class for logging and debug features that (by default) does nothing when not in debug mode
 */
public class Debug {
    public static final String LOG_FILENAME = "batchrenamer-log.txt";

    // ----- DEBUGGING -----

    private static boolean debug = BuildConfig.DEBUG;

    private static boolean debugToFile = false;

    /**
     * <p>Enable or disable debug mode</p>
     * <p/>
     * <p>By default, debug mode is enabled for development
     * builds and disabled for exported APKs - see
     * BuildConfig.DEBUG</p>
     *
     * @param enabled Enable debug mode ?
     */
    public static void setDebug(boolean enabled) {
        debug = enabled;
    }

    /**
     * <p>Is debug mode enabled ?</p>
     *
     * @return Debug mode enabled
     */
    public static boolean getDebug() {
        return debug;
    }

    /**
     * <p>Enable or disable debug to file mode</p>
     * <p/>
     * <p>By default, debug mode is disabled</p>
     *
     * @param enabled Enable debug to file mode ?
     */
    public static void setDebugToFile(boolean enabled) {
        debugToFile = enabled;
        if (!enabled) {
            // Remove old file
            java.io.File file = new java.io.File(Environment.getExternalStorageDirectory(), LOG_FILENAME);
            file.delete();
        }
    }

    /**
     * <p>Is debug to file mode enabled ?</p>
     *
     * @return Debug to file mode enabled
     */
    public static boolean getDebugToFile() {
        return debugToFile;
    }

    // ----- LOGGING -----

    public interface OnLogListener {
        public void onLog(int type, String typeIndicator, String message);
    }

    public static final String TAG = "batchrenamer";

    public static final int LOG_GENERAL = 0x0001;
    public static final int LOG_ERROR = 0x0002;

    public static final int LOG_NONE = 0x0000;
    public static final int LOG_ALL = 0xFFFF;

    private static int logTypes = LOG_ALL;

    private static OnLogListener logListener = null;

    /**
     * <p>Log a message (internal)</p>
     * <p/>
     * <p>Current debug and enabled logtypes decide what gets logged -
     * even if a custom callback is registered</p>
     *
     * @param type          Type of message to log
     * @param typeIndicator String indicator for message type
     * @param message       The message to log
     */
    private static void logCommon(int type, String typeIndicator, String message) {
        if ((logTypes & type) == type) {
            if (logListener != null) {
                logListener.onLog(type, typeIndicator, message);
            } else {
                String text = "[" + TAG + "][" + typeIndicator + "]" + (!message.startsWith("[") && !message.startsWith(" ") ? " " : "") + message;
                if (debug) {
                    Log.d(TAG, message);
                }
                if (debugToFile) {
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                    String timestamp = df.format(new Date());
                    writeToLogFile(timestamp + " " + message);
                }
            }
        }
    }

    private static void writeToLogFile(String what) {
        // Get the directory for the user's public pictures directory.
        java.io.File file = new java.io.File(Environment.getExternalStorageDirectory(), LOG_FILENAME);
        try {
            FileOutputStream outputStream = new FileOutputStream(file, true);
            outputStream.write((what + "\n").getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /**
     * <p>Log a "general" message</p>
     * <p/>
     * <p>These messages are the most frequent</p>
     *
     * @param message The message to log
     */
    public static void log(String message) {
        logCommon(LOG_GENERAL, "G", message);
    }

    /**
     * <p>Log a "error" message</p>
     * <p/>
     * <p>This could produce a lot of output</p>
     *
     * @param message The message to log
     */
    public static void logError(String message) {
        logCommon(LOG_ERROR, "E", message);
    }

    public static void logError(String message, Exception e) {
        logError(message + " (" + e.getMessage() + ")");
    }

    public static void logError(Class c, String message) {
        logError("(" + c.getSimpleName() + ") " + message);
    }

    public static void logError(Class c, String message, Exception e) {
        logError("(" + c.getSimpleName() + ") " + message, e);
    }

    /**
     * <p>Enable or disable logging specific types of message</p>
     * <p/>
     * <p>You may | (or) LOG_* constants together. Note that
     * debug mode must also be enabled for actual logging to
     * occur.</p>
     *
     * @param type    LOG_* constants
     * @param enabled Enable or disable
     */
    public static void setLogTypeEnabled(int type, boolean enabled) {
        if (enabled) {
            logTypes |= type;
        } else {
            logTypes &= ~type;
        }
    }

    /**
     * <p>Is logging for specific types of messages enabled ?</p>
     * <p/>
     * <p>You may | (or) LOG_* constants together, to learn if
     * <b>all</b> passed message types are enabled for logging. Note
     * that debug mode must also be enabled for actual logging
     * to occur.</p>
     *
     * @param type LOG_* constants
     */
    public static boolean getLogTypeEnabled(int type) {
        return ((logTypes & type) == type);
    }

    /**
     * <p>Is logging for specific types of messages enabled ?</p>
     * <p/>
     * <p>You may | (or) LOG_* constants together, to learn if
     * <b>all</b> message types are enabled for logging. Takes
     * debug mode into account for the result.</p>
     *
     * @param type LOG_* constants
     */
    public static boolean getLogTypeEnabledEffective(int type) {
        return getDebug() && getLogTypeEnabled(type);
    }

    /**
     * <p>Register a custom log handler</p>
     * <p/>
     * <p>Replaces the log method (write to logcat) with your own
     * handler. Whether your handler gets called is still dependent
     * on debug mode and message types being enabled for logging.</p>
     *
     * @param onLogListener Custom log listener or NULL to revert to default
     */
    public static void setOnLogListener(OnLogListener onLogListener) {
        logListener = onLogListener;
    }

    /**
     * <p>Get the currently registered custom log handler</p>
     *
     * @return Current custom log handler or NULL if none is present
     */
    public static OnLogListener getOnLogListener() {
        return logListener;
    }

}
