package com.sapphirevirtual.sentilife.remote;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Headers;
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
    @Headers("requestApiKey: zyJPW4avI3G1REZp26iFtB75HrnQ")
    @POST("emergency/")
    Call<ResponseBody> uploadFile(
            @Part("userId") RequestBody userId,
            @Part("emergencyType") RequestBody emergencyType,
            @Part MultipartBody.Part pictureFile
           );

    // new code for multiple files
    @Multipart
    @Headers("requestApiKey: zyJPW4avI3G1REZp26iFtB75HrnQ")
    @POST("emergency/")
    Call<ResponseBody> uploadAudio(
            @Part("userId") RequestBody userId,
            @Part("emergencyType") RequestBody emergencyType,
            @Part MultipartBody.Part audioFile
    );



    @Multipart
    @POST("/")
    Call<ResponseBody> postImage(@Part MultipartBody.Part image, @Part("name") RequestBody name);


}
