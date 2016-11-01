package com.whileplex.pipproject;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Rawipol on 8/27/15 AD.
 */
public class StoreData {

    private static StoreData storeData;
    private SharedPreferences prefs;

    public static StoreData getInstance(Context mContext){
        if(storeData == null) {
            storeData = new StoreData(mContext);
        }

        return storeData;
    }

    public StoreData(Context mContext) {
        prefs = mContext.getSharedPreferences("APP_INFO", Context.MODE_PRIVATE);
    }

    public String getUid(){
        return prefs.getString("ASD", null);
    }

    public void setUid(String uid){
        prefs.edit().putString("ASD", uid).apply();

    }

}
