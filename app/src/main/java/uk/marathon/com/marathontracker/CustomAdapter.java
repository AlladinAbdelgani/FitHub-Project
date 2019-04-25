package uk.marathon.com.marathontracker;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class CustomAdapter  extends ArrayAdapter{
    LayoutInflater layoutInflater;
    TextView userNameTV, userStatusTV, userTimerTV, userDistanceTV;

    Activity activity;
    ArrayList<String> userName =new ArrayList<String>();
    ArrayList<String> userStatus =new ArrayList<String>();
    ArrayList<String> userTimer =new ArrayList<String>();;
    ArrayList<Double> userDistance =new ArrayList<Double>();

    public CustomAdapter(@NonNull Activity activity, ArrayList<String> userName, ArrayList<String>userStatus, ArrayList<String> userTimer, ArrayList<Double> userDistance)
    {
        super(activity, R.layout.runninglistlinear, userName);

        this.activity=activity;
        this.userName =userName;
        this.userStatus =userStatus;
        this.userTimer =userTimer;
        this.userDistance =userDistance;

        layoutInflater=activity.getLayoutInflater();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        convertView=layoutInflater.inflate(R.layout.runninglistlinear,null);
        userNameTV =convertView.findViewById(R.id.userName);
        userStatusTV =convertView.findViewById(R.id.userStatus);
        userTimerTV =convertView.findViewById(R.id.usertimer);
        userDistanceTV =convertView.findViewById(R.id.userDistance);


        userNameTV.setText(userName.get(position));
        userStatusTV.setText(userStatus.get(position));
        userTimerTV.setText(userTimer.get(position));
        userDistanceTV.setText(String.valueOf(userDistance.get(position)));





        return convertView;
    }


}
