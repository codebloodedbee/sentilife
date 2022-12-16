package com.sapphirevirtual.sentilife;

import android.content.Context;
import android.content.SharedPreferences;

public class UserData {

    private Context mContext;

    public UserData(Context context) {
        mContext = context;
    }


    public SharedPreferences getSharedPref(){
        return mContext.getSharedPreferences(mContext.getPackageName(), Context.MODE_PRIVATE);
    }

    public void storeData(String userId, String firstname, String lastname, String email, String phone, String address, String pictureFile, String country, String state) {
        getSharedPref().edit().putString("userId", userId).apply();
        getSharedPref().edit().putString("firstname", firstname).apply();
        getSharedPref().edit().putString("lastname", lastname).apply();
        getSharedPref().edit().putString("email", email).apply();
        getSharedPref().edit().putString("address", address).apply();
        getSharedPref().edit().putString("pictureFile", pictureFile).apply();
        getSharedPref().edit().putString("country", country).apply();
        getSharedPref().edit().putString("state", state).apply();
        getSharedPref().edit().putString("phone", phone).apply();
    }

    public void clearData() {
        getSharedPref().edit().putString("firstname", "").clear();
        getSharedPref().edit().putString("lastname", "").clear();
        getSharedPref().edit().putString("email", "").clear();
        getSharedPref().edit().putString("country", "").clear();
        getSharedPref().edit().putString("state", "").clear();
    }

    public String getLastname(){
        return getSharedPref().getString("lastname", "");
    }

    public String getFirstname(){
        return getSharedPref().getString("firstname", "");
    }

    public String getEmail(){ return getSharedPref().getString("email", "");  }

    public String getPhone(){ return getSharedPref().getString("phone", "");  }

    public String getCountry(){ return getSharedPref().getString("country", ""); }

    public String getState(){
        return getSharedPref().getString("state", "");
    }

    public String getUserId(){
        return getSharedPref().getString("userId", "");
    }




}
