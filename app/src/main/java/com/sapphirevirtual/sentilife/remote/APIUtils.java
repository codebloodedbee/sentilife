package com.sapphirevirtual.sentilife.remote;

public class APIUtils {

    private APIUtils(){}

    public static final String API_URL = "https://apis.sentinelock.com/v1/android/sentinelapi.php/";

    public static FileService getFileService(){
        return RetrofitClient.getClient(API_URL).create(FileService.class);

    }
}
