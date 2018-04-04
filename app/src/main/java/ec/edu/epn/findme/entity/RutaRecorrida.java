package ec.edu.epn.findme.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.Polyline;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by David Moncayo on 27/03/2018.
 */

public class RutaRecorrida implements Parcelable{

    private Polyline polyline;



    private Map<String,Object> timeStampLastTraveled;

    public RutaRecorrida(Polyline polyline,Map<String,Object> timeStampLastTraveled){
        this.polyline=polyline;
        this.timeStampLastTraveled=timeStampLastTraveled;
    }

    protected RutaRecorrida(Parcel in) {
    }

    public static final Creator<RutaRecorrida> CREATOR = new Creator<RutaRecorrida>() {
        @Override
        public RutaRecorrida createFromParcel(Parcel in) {
            return new RutaRecorrida(in);
        }

        @Override
        public RutaRecorrida[] newArray(int size) {
            return new RutaRecorrida[size];
        }
    };

    public void setPolyline(Polyline polyline) {
        this.polyline = polyline;
    }

    public Polyline getPolyline() {

        return polyline;
    }

    public void setTimeStampLastTraveled(HashMap<String, Object> timeStampLastTraveled) {
        this.timeStampLastTraveled = timeStampLastTraveled;
    }

    public Map<String, Object> getTimeStampLastTraveled() {
        return timeStampLastTraveled;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
    }
}
