package com.shaneaustrie.shooterscout.models;

/**
 * Created by austrie on 4/11/18.
 */

public class Report {
    public String uid;
    public long time;
    public double lat;
    public double lng;
    public String download;
    public int status;

    public Report(String uid, double lat, double lng, long time, String download) {
        this.uid = uid;
        this.time = time;
        this.download = download;
        this.lat = lat;
        this.lng = lng;
        status = 0;
    }

}
