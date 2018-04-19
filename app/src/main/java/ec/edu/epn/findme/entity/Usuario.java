package ec.edu.epn.findme.entity;

import java.util.UUID;

/**
 * Created by David Moncayo on 18/04/2018.
 */

public class Usuario {


    private String cedula;
    private String nombres;
    private String apellidos;
    private UUID uniqueId;
    private boolean usuarioDinased;
    private String numeroUnicoDinased;

    public Usuario(String cedula, String nombres, String apellidos, boolean usuarioDinased, String numeroUnicoDinased){

        this.cedula = cedula;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.usuarioDinased = usuarioDinased;
        if(usuarioDinased == true){
            this.numeroUnicoDinased = numeroUnicoDinased;
        } else {
            this.numeroUnicoDinased = null;
        }
    }

    public Usuario(){

    }


    public String getCedula() {
        return cedula;
    }

    public void setCedula(String cedula) {
        this.cedula = cedula;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(UUID uniqueId) {
        this.uniqueId = uniqueId;
    }

    public boolean isUsuarioDinased() {
        return usuarioDinased;
    }

    public void setUsuarioDinased(boolean usuarioDinased) {
        this.usuarioDinased = usuarioDinased;
    }

    public String getNumeroUnicoDinased() {
        return numeroUnicoDinased;
    }

    public void setNumeroUnicoDinased(String numeroUnicoDinased) {
        this.numeroUnicoDinased = numeroUnicoDinased;
    }





}
