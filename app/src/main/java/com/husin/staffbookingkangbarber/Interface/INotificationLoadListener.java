package com.husin.staffbookingkangbarber.Interface;

import com.google.firebase.firestore.DocumentSnapshot;
import com.husin.staffbookingkangbarber.Model.MyNotification;

import java.util.List;

public interface INotificationLoadListener {

    void onNotificationLoadSuccess(List<MyNotification> myNotificationList, DocumentSnapshot lastDocument);
    void onNotificationLoadFailed(String message);

}
