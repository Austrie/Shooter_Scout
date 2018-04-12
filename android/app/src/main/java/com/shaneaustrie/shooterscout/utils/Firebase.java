package com.shaneaustrie.shooterscout.utils;

import android.content.Context;
import android.location.Location;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.shaneaustrie.shooterscout.models.Report;

import java.io.File;

/**
 * Created by austrie on 4/11/18.
 */

public class Firebase {
    private static StorageReference sRef;
    private static DatabaseReference dbRef;
    private static FirebaseAuth mAuth;

    public static void setup() {
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            signInAnonymously();
        } else if (sRef == null || dbRef == null) {
            sRef = FirebaseStorage.getInstance().getReference().child("shooterscout/wav");
            dbRef = FirebaseDatabase.getInstance().getReference("shooterscout");
        }
    }

    private static void signInAnonymously() {
        mAuth.signInAnonymously().addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                System.out.println("SHOOTERSCOUT SIGNIN: " + task.isSuccessful());
                sRef = FirebaseStorage.getInstance().getReference().child("shooterscout/wav");
                dbRef = FirebaseDatabase.getInstance().getReference("shooterscout");
            }
        });
    }

    public static void sendReport(String filePath, final Context context) {
        Uri file = Uri.fromFile(new File(filePath));
        StorageReference fileRef = sRef.child(file.getLastPathSegment());
        UploadTask uploadTask = fileRef.putFile(file);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                System.out.println(exception);
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                long time = System.currentTimeMillis();
                DeviceInfo dInfo = new DeviceInfo();
                Location location = dInfo.getLocation(context);
                String uid = dInfo.getUID(context);
                Report report = new Report(uid, location.getLatitude(), location.getLongitude(), time, downloadUrl.toString());
                dbRef.child("reports").setValue(report);
            }
        });
    }
}
