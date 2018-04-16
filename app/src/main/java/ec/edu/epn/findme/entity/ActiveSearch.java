package ec.edu.epn.findme.entity;

import com.google.firebase.firestore.GeoPoint;

/**
 * Created by David Moncayo on 16/04/2018.
 */

public class ActiveSearch {

    String name;
    String gender;
    GeoPoint LastSeen;
    int age;
    String description;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public GeoPoint getLastSeen() {
        return LastSeen;
    }

    public void setLastSeen(GeoPoint lastSeen) {
        LastSeen = lastSeen;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }



}
