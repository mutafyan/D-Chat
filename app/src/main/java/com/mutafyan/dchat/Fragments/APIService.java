package com.mutafyan.dchat.Fragments;

import com.mutafyan.dchat.Notifications.MyResponse;
import com.mutafyan.dchat.Notifications.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAP55IHDc:APA91bHsb8UgNAlgPR17JtHipWyHDIbY8ctHSkhnN5W140e5cJUMn9PxiWjRg_MF-lKNbbUAkMPzhylqZtx8NzLseyp9COx3LJfIlpsa0We1qGrqj8nhml4vVuhUrrkMq68nzyCvc10y"
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
