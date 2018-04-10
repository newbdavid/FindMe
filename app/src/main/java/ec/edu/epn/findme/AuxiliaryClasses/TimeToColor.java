package ec.edu.epn.findme.AuxiliaryClasses;

import android.util.Log;

import ec.edu.epn.findme.R;

/**
 * Created by David Moncayo on 10/04/2018.
 */

public class TimeToColor {
    long oneHourInMillis =3600000;
    long threeHourInMillis =10800000;
    long twelveHourInMillis = 43200000;
    long twoDaysInMillis = 172800000;

    public TimeToColor(){

    }

    public int getTimeToColor(long diffInMilliseconds){
        if(diffInMilliseconds>0&&diffInMilliseconds<oneHourInMillis){
            Log.d("MapsActivity", "LightGreen millies "+diffInMilliseconds);
            return R.color.zeroToOneHour;
        }
        else if(diffInMilliseconds>=oneHourInMillis&&diffInMilliseconds<threeHourInMillis){
            return R.color.oneToThreeHours;
        }
        else if(diffInMilliseconds>=threeHourInMillis&&diffInMilliseconds<twelveHourInMillis){
            Log.d("MapsActivity", "Blue millies "+diffInMilliseconds);
            return R.color.threeToTwelveHours;
        }
        else if(diffInMilliseconds>=twelveHourInMillis&&diffInMilliseconds<twoDaysInMillis){
            Log.d("MapsActivity", "Purple millies "+diffInMilliseconds);
            return R.color.twelveToTwoDays;
        }
        return R.color.twoToFiveDayS;
    }
}
