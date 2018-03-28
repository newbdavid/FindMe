package ec.edu.epn.findme.vo;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.Polyline;

import java.util.HashMap;

/**
 * Created by David Moncayo on 27/03/2018.
 */

public class RutaRecorrida implements Parcelable{

    Polyline polyline;



    HashMap<String,Object> timeStampLastTraveled;

    public RutaRecorrida(Polyline polyline,HashMap<String,Object> timeStampLastTraveled){
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

    public HashMap<String, Object> getTimeStampLastTraveled() {
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
