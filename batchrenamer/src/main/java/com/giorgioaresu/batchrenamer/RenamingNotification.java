package com.giorgioaresu.batchrenamer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

/**
 * Helper class for showing and canceling renaming
 * notifications.
 * <p/>
 * This class makes heavy use of the {@link NotificationCompat.Builder} helper
 * class to create notifications in a backward-compatible way.
 */
public class RenamingNotification {
    /**
     * The unique identifier for this type of notification.
     */
    private static final String NOTIFICATION_TAG = "Renaming";
    /**
     * Pass this as progress to set the notification to not ongoing
     */
    private static final int COMPLETED = -1;
    /**
     * Pass this as progress to set the progressbar to indeterminate
     */
    private static final int INDETERMINATE = -1;

    private static long defaultWhen = System.currentTimeMillis();

    /**
     * Shows the notification, or updates a previously shown notification of
     * this type, with the given parameters.
     *
     * @see #cancel(Context)
     */
    public static void notify(final Context context, final int progress, final int max, final int completedNumber, final int failedNumber, long when) {
        final Resources res = context.getResources();

        final boolean indeterminate = max == INDETERMINATE;
        final boolean completed = progress == COMPLETED;
        final String ticker = res.getString(completed ? R.string.renaming_notification_ticker_completed : R.string.renaming_notification_ticker_started);
        final String title = res.getString(R.string.renaming_notification_title);
        final String text;
        if (completed) {
            if (failedNumber == 0) {
                text = res.getString(R.string.renaming_notification_text_completed);
            } else {
                text = String.format(res.getString(R.string.renaming_notification_text_completed_failed), failedNumber);
            }
        } else if (indeterminate) {
            text = res.getString(R.string.renaming_notification_text_indeterminate);
        } else {
            text = res.getString(R.string.renaming_notification_text_template, progress, max);
        }
        final String summary = res.getString(R.string.renaming_notification_summary_big, completedNumber, failedNumber);


        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(context, MainActivity.class);
        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );


        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_stat_renaming)
                .setContentTitle(title)
                .setContentText(text)
                .setTicker(ticker)
                .setOngoing(!completed)
                        // Use a default priority (recognized on devices running Android
                        // 4.1 or later)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                        // Set the pending intent to be initiated when the user touches
                        // the notification.
                //.setContentIntent(resultPendingIntent)
                        // Automatically dismiss the notification when it is touched.*/
                .setAutoCancel(true)
                        // Set a time to avoid blinking notification
                .setWhen((when > 0) ? when : defaultWhen);

        if (!indeterminate) {
            // Show expanded text content on devices running Android 4.1 or later.
            builder.setStyle(new NotificationCompat.BigTextStyle()
                    .bigText(text)
                    .setBigContentTitle(title)
                    .setSummaryText(summary));
        }

        if (!completed) {
            // Add progressbar
            builder.setProgress(max, progress, indeterminate);
        }

        notify(context, builder.build());
    }

    public static void notifyCompleted(Context context, int completedNumber, int failedNumber) {
        notify(context, COMPLETED, 0, completedNumber, failedNumber, 0);
    }

    public static void notifyIndeterminate(Context context) {
        notify(context, 0, INDETERMINATE, 0, 0, 0);
    }

    private static void notify(final Context context, final Notification notification) {
        final NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(NOTIFICATION_TAG, 0, notification);
    }

    public static void cancel(final Context context) {
        final NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        nm.cancel(NOTIFICATION_TAG, 0);
    }
}