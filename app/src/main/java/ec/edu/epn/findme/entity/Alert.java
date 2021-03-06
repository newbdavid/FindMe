package ec.edu.epn.findme.entity;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.GeoPoint;

/**
 * Created by David Moncayo on 19/04/2018.
 */

public class Alert {
    private transient boolean reviewed;
    private String status;
    private String ownerUid;
    private String alertType;
    private String title;
    private String description;
    private GeoPoint location;
    private long alertTimeMillis;

    public Alert() {
    }

    public Alert(boolean reviewed, String status, String ownerUid, String alertType, String title, String description, GeoPoint location, long alertTimeMillis) {
        this.reviewed = reviewed;
        this.status = status;
        this.ownerUid = ownerUid;
        this.alertType = alertType;
        this.title = title;
        this.description = description;
        this.location = location;
        this.alertTimeMillis = alertTimeMillis;
    }

    public LatLng getLocationLatLng(){
        LatLng lastUbication = new LatLng(this.location.getLatitude(),this.location.getLongitude());
        return lastUbication;
    }

    public boolean isReviewed() {
        return reviewed;
    }

    public void setReviewed(boolean reviewed) {
        this.reviewed = reviewed;
    }

    public String getOwnerUid() {
        return ownerUid;
    }

    public void setOwnerUid(String ownerUid) {
        this.ownerUid = ownerUid;
    }

    public String getAlertType() {
        return alertType;
    }

    public void setAlertType(String alertType) {
        this.alertType = alertType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public GeoPoint getLocation() {
        return location;
    }

    public void setLocation(GeoPoint location) {
        this.location = location;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getAlertTimeMillis() {
        return alertTimeMillis;
    }

    public void setAlertTimeMillis(long alertTimeMillis) {
        this.alertTimeMillis = alertTimeMillis;
    }
}
