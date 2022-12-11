package com.sapphirevirtual.sentilife.remote;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface FileService {

//    @Multipart
//    @POST("/")

    @Multipart
    @POST("upload")
    Call<ResponseBody> upload(
            @Part("command") RequestBody description,
            @Part MultipartBody.Part file
    );

    // new code for multiple files
    @Multipart
    @POST("upload")
    Call<ResponseBody> uploadMultipleFiles(
            @Part("command") RequestBody command,

            @Part("agent_code") RequestBody agent_code,
            @Part MultipartBody.Part first_image
           );



    @Multipart
    @POST("/")
    Call<ResponseBody> postImage(@Part MultipartBody.Part image, @Part("name") RequestBody name);


}
