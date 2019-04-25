package uk.marathon.com.marathontracker;

public class RunningUserModel {
    String userName,userStatus,userTimer;
    double totalSec,userDistance;

    public RunningUserModel()
    {

    }



    public RunningUserModel(String userName, String userStatus, String userTimer, double userDistance, double totalSec) {
        this.userName = userName;
        this.userStatus = userStatus;
        this.userTimer = userTimer;
        this.userDistance = userDistance;
        this.totalSec=totalSec;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserStatus() {
        return userStatus;
    }

    public String getUserTimer() {
        return userTimer;
    }

    public double getUserDistance() {
        return userDistance;
    }
    public double getTotalSec() {return totalSec; }
}
