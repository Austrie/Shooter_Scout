package com.shaneaustrie.shooterscout;

import android.app.Application;

import cafe.adriel.androidaudioconverter.AndroidAudioConverter;
import cafe.adriel.androidaudioconverter.callback.ILoadCallback;

/**
 * Created by austrie on 4/10/18.
 */

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
//        AndroidAudioConverter.load(this, new ILoadCallback() {
//            @Override
//            public void onSuccess() {
//                // Great!
//                System.out.println("This the success");
//            }
//            @Override
//            public void onFailure(Exception error) {
//                // FFmpeg is not supported by device
//                System.out.println("This the error ********" + error);
//            }
//        });
    }
}