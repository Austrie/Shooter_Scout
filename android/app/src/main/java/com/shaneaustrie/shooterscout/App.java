package com.shaneaustrie.shooterscout;

import android.app.Application;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.shaneaustrie.shooterscout.utils.Firebase;


/**
 * Created by austrie on 4/10/18.
 */

public class App extends Application {
    private FirebaseAuth mAuth;

    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setup();
    }
}