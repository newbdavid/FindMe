package ec.edu.epn.findme.entity;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by David Moncayo on 05/04/2018.
 */

public class TrackObject {
    private Date lastTraveled;
    private List<GeoPoint> points;



    private FieldValue lastTraveledFieldValue;

    public Date getLastTraveled() {
        return lastTraveled;
    }

    public void setLastTraveled(Date lastTraveled) {
        this.lastTraveled = lastTraveled;
    }
    public FieldValue getLastTraveledFieldValue() {
        return lastTraveledFieldValue;
    }

    public void setLastTraveledFieldValue(FieldValue lastTraveledFieldValue){
        this.lastTraveledFieldValue = lastTraveledFieldValue;
    }

    public List<GeoPoint> getPoints() {
        return points;
    }

    public void setPoints(List<GeoPoint> points) {
        this.points = points;
    }
    public void setLatLngPoints(List<LatLng> latLngPoints){
        List<GeoPoint> geoPoints = new ArrayList<GeoPoint>(latLngPoints.size());
        for(int i = 0; i<latLngPoints.size();i++){
            geoPoints.add(new GeoPoint(latLngPoints.get(i).latitude,latLngPoints.get(i).longitude));
        }
        this.points = geoPoints;
    }

}
