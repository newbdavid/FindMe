package ec.edu.epn.findme.enums;

/**
 * Created by David Moncayo on 26/09/2018.
 */

public enum AlertStatusEnum {
    CHECKED("Checked"),
    PENDING("Pending"),
    REJECTED("Rejected");

    private String status;

    private AlertStatusEnum(String status){
        this.status = status;
    }
    @Override
    public String toString(){
        return status;
    }

}
