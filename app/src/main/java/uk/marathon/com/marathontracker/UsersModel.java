package uk.marathon.com.marathontracker;

public class UsersModel {
    String fullName,email,password;

    public UsersModel()
    {

    }
    public UsersModel(String fullName, String email, String password) {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
