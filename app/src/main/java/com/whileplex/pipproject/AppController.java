package com.whileplex.pipproject;

import android.app.Application;

import com.firebase.client.Firebase;

/**
 * Created by Rawipol on 9/1/15 AD.
 */
public class AppController extends Application {

    private static Firebase myFirebaseRef;
    public static String uid;
    public static String UIDTAG = "UID";

    public static Firebase getFirebase(){
        if(myFirebaseRef == null){
            myFirebaseRef = new Firebase("https://whileplex-pip.firebaseio.com/");
        }
        return myFirebaseRef;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
    }
}
