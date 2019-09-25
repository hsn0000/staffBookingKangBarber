package com.husin.staffbookingkangbarber.Retrofit;

import com.husin.staffbookingkangbarber.Model.FCMResponse;
import com.husin.staffbookingkangbarber.Model.FCMSendData;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMService {

    @Headers({
            "Content-Type:aplication/json",
            "Authorization:key=AAAAD0oLwKA:APA91bGrL0jIeptXjAA33uuzEAywvygP4wzgils1kCLpgF_BXEg8A8fRRRm3FGwdllmhNwDB207sUA914Hjq7twB9r9tms_V56fb_SVAeKdtEY5WWG8kvFUaDjVKXR1-RTKK9CBZP7tG"
    })

    @POST("fcm/send")
    Observable<FCMResponse> sendNotification(@Body FCMSendData body);

}
