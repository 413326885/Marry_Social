package com.pkjiao.friends.mm.base;

import com.dhn.marrysocial.R;
import com.pkjiao.friends.mm.MarrySocialMainActivity;
import com.pkjiao.friends.mm.activity.ChatMsgActivity;
import com.pkjiao.friends.mm.database.MarrySocialDBHelper;
import com.pkjiao.friends.mm.utils.Utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

public class NotificationManagerControl {

    private final Context mContext;
    private NotificationManager mNotificationManager;
    private Bitmap mLargeIcon;

    private static final int COMMENTS_NOTIFICATION_ID = -100;
    private static final int CONTACTS_NOTIFICATION_ID = -200;
    private static NotificationManagerControl mNotificationManagerControl = null;

    private NotificationManagerControl(Context context) {
        mContext = context;
        mNotificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        Bitmap icon_launcher = BitmapFactory.decodeResource(
                context.getResources(), R.drawable.ic_launcher);
        mLargeIcon = Utils.resizeAndCropCenter(icon_launcher,
                Utils.mTinyCropCenterThumbPhotoWidth, true);
    }

    public static NotificationManagerControl newInstance(Context context) {
        if (mNotificationManagerControl == null) {
            mNotificationManagerControl = new NotificationManagerControl(
                    context);
        }
        return mNotificationManagerControl;
    }

    public void cancelAll() {
        mNotificationManager.cancelAll();
    }

    public void showChatMsgNotification(Bitmap header, String chatName,
            String chatMsg, int msgCount, String chatId) {

        Bitmap headerPic = Utils.resizeAndCropCenter(header,
                Utils.mTinyCropCenterThumbPhotoWidth, true);
        String[] uids = chatId.split("_");

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
        // Adds the back stack
        stackBuilder.addParentStack(ChatMsgActivity.class);
        // Adds the Intent to the top of the stack
        Intent resultIntent = new Intent(mContext, ChatMsgActivity.class);
        resultIntent.putExtra(MarrySocialDBHelper.KEY_CHAT_ID, chatId);
        stackBuilder.addNextIntent(resultIntent);
        // Gets a PendingIntent containing the entire back stack
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
                PendingIntent.FLAG_UPDATE_CURRENT);

        int notification_id = Integer.valueOf(uids[0] + uids[1]);
        Notification notification = new NotificationCompat.Builder(mContext)
                .setLargeIcon(headerPic).setSmallIcon(R.drawable.ic_launcher)
                .setTicker(chatName + " : " + chatMsg)
                .setContentTitle(chatName).setContentText(chatMsg)
                .setNumber(msgCount).setContentIntent(resultPendingIntent)
                .setAutoCancel(true).setDefaults(Notification.DEFAULT_ALL)
                .build();
        mNotificationManager.notify(notification_id, notification);
    }

    public void showCommentsNotification(int msgCount) {

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
        // Adds the back stack
        stackBuilder.addParentStack(MarrySocialMainActivity.class);
        // Adds the Intent to the top of the stack
        Intent resultIntent = new Intent(mContext,
                MarrySocialMainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        // Gets a PendingIntent containing the entire back stack
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(mContext)
                .setLargeIcon(mLargeIcon).setSmallIcon(R.drawable.ic_launcher)
                .setTicker("您有了新的动态").setContentTitle("新动态")
                .setContentText("您有了新的动态").setNumber(msgCount)
                .setContentIntent(resultPendingIntent).setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL).build();
        mNotificationManager.notify(COMMENTS_NOTIFICATION_ID, notification);
    }

    public void showIndirectsNotification(int contactsCount) {

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
        // Adds the back stack
        stackBuilder.addParentStack(MarrySocialMainActivity.class);
        // Adds the Intent to the top of the stack
        Intent resultIntent = new Intent(mContext,
                MarrySocialMainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        // Gets a PendingIntent containing the entire back stack
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(mContext)
                .setLargeIcon(mLargeIcon).setSmallIcon(R.drawable.ic_launcher)
                .setTicker("您有了新的好友").setContentTitle("新好友")
                .setContentText("您有了新的好友").setNumber(contactsCount)
                .setContentIntent(resultPendingIntent).setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL).build();
        mNotificationManager.notify(CONTACTS_NOTIFICATION_ID, notification);
    }
}
