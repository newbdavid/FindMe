package ec.edu.epn.findme.enums;

/**
 * Created by David Moncayo on 07/10/2018.
 */

public enum AlertTypeEnum {
    PISTA("Pista"),
    AVISTAMIENTO("Avistamiento");

    private String description;

    AlertTypeEnum(String description){
        this.description = description;
    }

    @Override
    public String toString(){
        return description;
    }
}
