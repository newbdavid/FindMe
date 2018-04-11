package ec.edu.epn.findme.entity;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.GeoPoint;

import java.util.Date;

/**
 * Created by David Moncayo on 11/04/2018.
 */

public class LastLocation {
    private java.util.Date lastSeen;
    private GeoPoint lastUbication;
    private long lastSeenMillis;
    public long getLastSeenMillis() {
        return lastSeenMillis;
    }

    public void setLastSeenMillis(long lastSeenMillis) {
        this.lastSeenMillis = lastSeenMillis;
    }


    public Date getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(Date lastSeen) {
        this.lastSeen = lastSeen;
    }



    public GeoPoint getLastUbication() {
        return lastUbication;
    }

    public void setLastUbication(GeoPoint lastUbication) {
        this.lastUbication = lastUbication;
    }

    public LatLng getLastUbicationLatLng(){
        LatLng lastUbication = new LatLng(this.lastUbication.getLatitude(),this.lastUbication.getLongitude());
        return lastUbication;
    }


}
