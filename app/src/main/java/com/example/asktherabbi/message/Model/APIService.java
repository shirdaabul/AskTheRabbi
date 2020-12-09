package com.example.asktherabbi.message.Model;

import com.example.asktherabbi.message.Notifications.MyResponse;
import com.example.asktherabbi.message.Notifications.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAYv4VAh4:APA91bHRYI8G9Lds-WLrplmXWmLFlhP5TeTYrjDXk-kSZWHV1r8zIzlruaLM_4CdlMF5iS9aq_zmvg_NLMfnkYU1fwcHmyvFzMma_DsnoBD3iZhAFf4r8eEQyAUFj9d5SkXbEYu1RrA4"            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
