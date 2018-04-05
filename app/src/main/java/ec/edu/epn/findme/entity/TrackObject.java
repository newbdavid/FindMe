package ec.edu.epn.findme.entity;

import com.google.firebase.firestore.GeoPoint;

import java.util.Date;
import java.util.List;

/**
 * Created by David Moncayo on 05/04/2018.
 */

public class TrackObject {
    private Date lastTraveled;
    private List<GeoPoint> points;

    public Date getLastTraveled() {
        return lastTraveled;
    }

    public void setLastTraveled(Date lastTraveled) {
        this.lastTraveled = lastTraveled;
    }

    public List<GeoPoint> getPoints() {
        return points;
    }

    public void setPoints(List<GeoPoint> points) {
        this.points = points;
    }
}
