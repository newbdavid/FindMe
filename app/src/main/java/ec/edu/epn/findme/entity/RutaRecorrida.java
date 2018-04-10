package ec.edu.epn.findme.entity;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.Polyline;
import com.google.firebase.firestore.FieldValue;

/**
 * Created by David Moncayo on 27/03/2018.
 */

public class RutaRecorrida implements Parcelable{

    private Polyline polyline;



    private FieldValue timestamp;


    public RutaRecorrida(Polyline polyline,FieldValue timestamp){
        this.polyline=polyline;
        this.timestamp=timestamp;
    }

    protected RutaRecorrida(Parcel in) {
    }

    public FieldValue getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(FieldValue timestamp) {
        this.timestamp = timestamp;
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



    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
    }
}
