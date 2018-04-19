package ec.edu.epn.findme.entity;

/**
 * Created by David Moncayo on 18/04/2018.
 */

public class LoginObject {
    private String email;

    private String password;
    private String repeatPassword;
    public LoginObject(String email, String password, String repeatPassword){
        this.email = email;
        this.password = password;
        this.repeatPassword =repeatPassword;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRepeatPassword() {
        return repeatPassword;
    }

    public void setRepeatPassword(String repeatPassword) {
        this.repeatPassword = repeatPassword;
    }
}
