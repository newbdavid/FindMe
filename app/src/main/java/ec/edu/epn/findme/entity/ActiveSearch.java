package ec.edu.epn.findme.entity;

import com.google.firebase.firestore.GeoPoint;

/**
 * Created by David Moncayo on 16/04/2018.
 */

public class ActiveSearch {

    private String name;
    private String gender;
    private GeoPoint ultimoAvistamiento;
    private long age;
    private String description;
    private boolean active;
    private String id;
    private boolean isFoundYet;
    private transient boolean listSelected;

    public ActiveSearch(){

    }
    public ActiveSearch(String name, String gender, GeoPoint ultimoAvistamiento, long age, String description, boolean active, String id, boolean isFoundYet, boolean listSelected) {
        this.name = name;
        this.gender = gender;
        this.ultimoAvistamiento = ultimoAvistamiento;
        this.age = age;
        this.description = description;
        this.active = active;
        this.id = id;
        this.isFoundYet = isFoundYet;
        this.listSelected = listSelected;
    }

    public boolean isListSelected() {
        return listSelected;
    }

    public void setListSelected(boolean listSelected) {
        this.listSelected = listSelected;
    }

    public boolean getIsFoundYet() {
        return isFoundYet;
    }

    public void setIsFoundYet(boolean foundYet) {
        isFoundYet = foundYet;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
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
