package ec.edu.epn.findme.entity;

import com.google.firebase.firestore.GeoPoint;

/**
 * Created by David Moncayo on 16/04/2018.
 */

public class ActiveSearch {

    String name;
    String gender;
    GeoPoint ultimoAvistamiento;
    long age;
    String description;
    Boolean active;
    String id;
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }



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

    public GeoPoint getUltimoAvistamiento() {
        return ultimoAvistamiento;
    }

    public void setUltimoAvistamiento(GeoPoint ultimoAvistamiento) {
        this.ultimoAvistamiento = ultimoAvistamiento;
    }

    public long getAge() {
        return age;
    }

    public void setAge(long age) {
        this.age = age;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }



}
