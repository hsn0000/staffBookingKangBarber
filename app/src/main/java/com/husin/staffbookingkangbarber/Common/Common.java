package com.husin.staffbookingkangbarber.Common;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.husin.staffbookingkangbarber.Model.Barber;
import com.husin.staffbookingkangbarber.Model.MyToken;
import com.husin.staffbookingkangbarber.Model.Salon;
import com.husin.staffbookingkangbarber.R;
import com.husin.staffbookingkangbarber.Service.MyFCMService;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Formatter;
import java.util.logging.SimpleFormatter;

import io.paperdb.Paper;

public class Common {
    public static final String KEY_ENABLE_BUTTON_NEXT = "ENABLE_BUTTON_NEXT";
    public static final String KEY_SALON_STORE = "SALON_SAVE";
    public static final String KEY_STEP = "STEP";
    public static final Object DISABLE_TAG = "DISABLE" ;
    public static final int TIME_SLOT_TOTAL = 20;
    public static final String LOGGED_KEY = "LOGGED";
    public static final String STATE_KEY = "STATE";
    public static final String SALON_KEY = "SALON";
    public static final String BARBER_KEY = "BARBER";
    public static final String TITLE_KEY =  "title";
    public static final String CONTENT_KEY = "content";
    public static final int MAX_NOTIFICATION_PER_LOAD = 10;


    public static String state_name="";
    public static Barber currentBarber;
    public static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd_MM_yyyy");
    public static Calendar bookingDate=Calendar.getInstance();
    public static Salon selected_salon;

    public static String converTimeSlotToString(int slot) {
        switch (slot)
        {
            case 0:
                return "9:00 - 9:30";
            case 1:
                return "9:30 - 10:00";
            case 2:
                return "10:00 - 10:30";
            case 3:
                return "10-30 - 11:00";
            case 4:
                return "11:00 - 11:30";
            case 5:
                return "11:30 - 12:00";
            case 6:
                return "12:00 - 12:30";
            case 7:
                return "12:30 - 13:00";
            case 8:
                return "13:00 - 13-30";
            case 9:
                return "13:30 - 14:00";
            case 10:
                return "14:00 - 14:30";
            case 11:
                return "14:30 - 15:00";
            case 12:
                return "15:00 - 15:30";
            case 13:
                return "15:30 - 16:00";
            case 14:
                return "16:00 - 16:30";
            case 15:
                return "16:30 - 17:00";
            case 16:
                return "17:00 - 17:30";
            case 17:
                return "17:30 - 18:00";
            case 18:
                return "18:00 - 18:30";
            case 19:
                return "18:30 - 19:00";
            default:
                return "Closed";




        }
    }


    public static void showNotification(Context context, int notification_id, String title, String content, Intent intent) {

        PendingIntent pendingIntent = null;
         if (intent !=null)
             pendingIntent = PendingIntent.getActivity(context,
                     notification_id,
                     intent,
                     pendingIntent.FLAG_UPDATE_CURRENT);
         String NOTIFICATION_CHANNEL_ID = "edmt_barber_booking_channel_01";
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    "Staff Booking Kang Barber", NotificationManager.IMPORTANCE_DEFAULT);

            notificationChannel.setDescription("Staff app");
            notificationChannel.enableLights(true);
            notificationChannel.enableVibration(true);


            notificationManager.createNotificationChannel(notificationChannel);

        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context,NOTIFICATION_CHANNEL_ID);

        builder.setContentTitle(title)
                .setContentText(content)
                .setAutoCancel(false)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),R.mipmap.ic_launcher));

        if (pendingIntent !=null)
            builder.setContentIntent(pendingIntent);
        Notification notification = builder.build(); // Notifi tanda kutip

        notificationManager.notify(notification_id,notification);

    }

    public enum TOKEN_TYPE {
        CLIENT,
        BARBER,
        MANAGER

    }

    public static void updateToken(Context context, String token) {
//        cek user jika sudah login
        Paper.init(context);
        String user = Paper.book().read(Common.LOGGED_KEY);
        if (user !=null)
        {
            if (!
                    TextUtils.isEmpty(user))
            {
                MyToken myToken = new MyToken();
                myToken.setToken(token);
                myToken.setTokenType(TOKEN_TYPE.BARBER); // run from barber staf
                myToken.setUserPhone(user);

//                submit on firebase
                FirebaseFirestore.getInstance()
                        .collection("Tokens")
                        .document(user)
                        .set(myToken)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {


                            }
                        });


            }
        }
    }
}
