package uk.marathon.com.marathontracker;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.security.Provider;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;

public  class RaceActivity extends AppCompatActivity implements View.OnClickListener,AdapterView.OnItemLongClickListener, LocationListener {


    TextView name,timerTV,distanceTV;
    static EditText hourET,minET;
    Button startBtn, pauseBtn,resetBtn,logoutBtn,deleteRacer,setTimeBtn;
    final static int MSG_START_TIMER = 0;
    final int MSG_STOP_TIMER = 1;
    final int MSG_UPDATE_TIMER = 2;
    static Handler mHandler;
    Stopwatch timer;
    String userName,t,uiid;
    final int REFRESH_RATE = 1000;
    long h=0,m=0,s=0,pau=0;
    int i=0;
    boolean resets=true;
    static boolean serviceStarted=false;
    LocationManager locationManager;
    LocationListener locationListener;
    double lattitude,longitude,plt=0.0,plng=0.0,p=0;
    double distance=0.0,dis=0.0,avgSpeed=0.0,totalSec=0.0;
    static Thread thread;

    ArrayList<String> userNameArrayList=new ArrayList<String>();
    ArrayList<String> userStatusArrayList=new ArrayList<String>();
    ArrayList<String> userTimerArrayList=new ArrayList<String>();
    ArrayList<Double> userDistanceArrayList=new ArrayList<Double>();
    ListView listView;
    CustomAdapter customAdapter;
    DatabaseReference databaseReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_race);

        timer = new Stopwatch();

        name=findViewById(R.id.nameTV);
        timerTV=findViewById(R.id.timerTV);
        distanceTV=findViewById(R.id.distanceTV);
        hourET=findViewById(R.id.hourET);
        minET=findViewById(R.id.minET);
        startBtn=findViewById(R.id.startBtn);
        pauseBtn =findViewById(R.id.pauseBtn);
        resetBtn=findViewById(R.id.resetBtn);
        logoutBtn=findViewById(R.id.logoutBtn);
        setTimeBtn=findViewById(R.id.setTimeBtn);
        databaseReference= FirebaseDatabase.getInstance().getReference("RunningUsers");
        startBtn.setOnClickListener(this);
        pauseBtn.setOnClickListener(this);
        logoutBtn.setOnClickListener(this);
        resetBtn.setOnClickListener(this);
        setTimeBtn.setOnClickListener(this);

        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);


        listView=findViewById(R.id.RunningListView);

        Query dr=FirebaseDatabase.getInstance().getReference("RunningUsers").orderByChild("totalSec");
        dr.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userNameArrayList.clear();userStatusArrayList.clear();userTimerArrayList.clear();userDistanceArrayList.clear();
                for (DataSnapshot s:dataSnapshot.getChildren())
                {

                    RunningUserModel rUM=s.getValue(RunningUserModel.class);
                    userNameArrayList.add(rUM.getUserName());
                    userStatusArrayList.add(rUM.getUserStatus());
                    userTimerArrayList.add(rUM.getUserTimer());
                    userDistanceArrayList.add(rUM.getUserDistance());


                }
                Collections.reverse(userNameArrayList);Collections.reverse(userStatusArrayList);Collections.reverse(userTimerArrayList);
                Collections.reverse(userDistanceArrayList);
                customAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(),"error while reading",Toast.LENGTH_SHORT).show();

            }
        });



//        DatabaseReference dr=FirebaseDatabase.getInstance().getReference("RunningUsers");
//        dr.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                userNameArrayList.clear();userStatusArrayList.clear();userTimerArrayList.clear();userDistanceArrayList.clear();
//                for (DataSnapshot s:dataSnapshot.getChildren())
//                {
//
//                    RunningUserModel rUM=s.getValue(RunningUserModel.class);
//                    userNameArrayList.add(rUM.getUserName());
//                    userStatusArrayList.add(rUM.getUserStatus());
//                    userTimerArrayList.add(rUM.getUserTimer());
//                    userDistanceArrayList.add(rUM.getUserDistance());
//
//
//                }
//                customAdapter.notifyDataSetChanged();
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//                Toast.makeText(getApplicationContext(),"error while reading",Toast.LENGTH_SHORT).show();
//
//            }
//        });
        customAdapter=new CustomAdapter(this,userNameArrayList,userStatusArrayList,userTimerArrayList,userDistanceArrayList);
        listView.setAdapter(customAdapter);
        listView.setOnItemLongClickListener(this);




        mHandler = new Handler()
        {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case MSG_START_TIMER:
                        timer.start(); //start timer
                        mHandler.sendEmptyMessage(MSG_UPDATE_TIMER);
                        break;

                    case MSG_UPDATE_TIMER:
                        t=h+" : "+ m +" :"+s;
                        timerTV.setText(t);

                        if (dis>=0.1)
                        {
                            mHandler.sendEmptyMessage(MSG_STOP_TIMER);
                            locationManager.removeUpdates(RaceActivity.this);
                            timerTV.setText(h+" : "+ m +" :"+s);
                            RunningUserModel RUsers= new RunningUserModel(userName,"Paused",t,dis,dis/totalSec);
                            databaseReference.child(uiid).setValue(RUsers);
                            resets=true;
                            pau=s;
                        }
                        totalSec=p+timer.getElapsedTimeSecs();
                        if (totalSec<=0 || dis<=0)
                        {
                            RunningUserModel RUsers2= new RunningUserModel(userName,"Running",t,dis,0);
                            databaseReference.child(uiid).setValue(RUsers2);
                        }
                        else
                        {
                            RunningUserModel RUsers1= new RunningUserModel(userName,"Running",t,dis,dis/totalSec);
                            databaseReference.child(uiid).setValue(RUsers1);
                        }

                        s = pau+timer.getElapsedTimeSecs();

                        if (s<60) {
                            s = pau+timer.getElapsedTimeSecs();
                        }
                        if(s%60==0 && s!=0)
                        {
                            m= m+1;
                            if (m%60==0 && m!=0)
                            {

                                h=h+1;
                                m=0;
                            }
                        }
                        if (s>=60)
                        {

                            s = s%(60*m);
                        }
                        mHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIMER,REFRESH_RATE); //text view is updated every second,
                        break;                                  //though the timer is still running
                    case MSG_STOP_TIMER:
                        mHandler.removeMessages(MSG_UPDATE_TIMER); // no more updates.
                        timer.stop();//stop timer
                        break;

                    default:
                        break;
                }
            }
        };


    }
    @Override
    public void onLocationChanged(Location location) {

        lattitude=location.getLatitude();
        longitude=location.getLongitude();
        if (i==0)
        {
            plt=lattitude;plng=longitude;
        }
        //Toast.makeText(this,lattitude+"...."+longitude,Toast.LENGTH_LONG).show();
        distanceBetweenTwoPoint(plt,plng,lattitude,longitude);
        plt=lattitude;plng=longitude;
        i=1;
    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }
    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(this,"GPS enabled",Toast.LENGTH_LONG).show();
    }
    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(this,"GPS disabled",Toast.LENGTH_LONG).show();

    }

    @Override
    public void onStart() {
        super.onStart();



        FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();
        uiid=firebaseAuth.getCurrentUser().getUid().toString();
      //  Toast.makeText(this,"onStart",Toast.LENGTH_SHORT).show();
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users").child(uiid);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UsersModel b=dataSnapshot.getValue(UsersModel.class);
                userName=b.getFullName();
                //Toast.makeText(getApplicationContext(),userName,Toast.LENGTH_SHORT).show();
                name.setText("User : "+b.getFullName());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        requestForGps();


    }
    public void onStop()
    {
        super.onStop();
        stopped();
    }


    public void onClick(View v) {
        Intent si=new Intent(this,MyServiceClass.class);
        if(v.getId()==R.id.startBtn)
        {

           if (resets!=true)
           {
               Toast.makeText(this,"Please reset or pause first",Toast.LENGTH_SHORT).show();
           }
           else
           {
               mHandler.sendEmptyMessage(MSG_STOP_TIMER);
               requestForGps();
               mHandler.sendEmptyMessage(MSG_START_TIMER);

               if (serviceStarted==true)
               {
                   stopService(si);
                   serviceStarted=false;
                   if (thread!=null)
                   {
                       thread.interrupt();
                   }
                   Toast.makeText(this,"Service Stopped, please set the timmer again",Toast.LENGTH_LONG).show();
               }


           }
           resets=false;

        }

        else if(v.getId()==R.id.pauseBtn){


            mHandler.sendEmptyMessage(MSG_STOP_TIMER);
            locationManager.removeUpdates(this);
            timerTV.setText(h+" : "+ m +" :"+s);
            stopped();
            resets=true;
            pau=s;
            p=totalSec;


        }
        else if (v.getId()==R.id.resetBtn)
        {
            resets=true;
            p=0;totalSec=0;
            mHandler.sendEmptyMessage(MSG_STOP_TIMER);
            locationManager.removeUpdates(this);
            h=0;m=0;s=0;i=0;distance=0;dis=0;
            distanceTV.setText("0.00");

           pau=0;
            stopped();
            timerTV.setText(0+" : "+ 0 +" :"+0);
            if (serviceStarted==true)
            {
                stopService(si);
                serviceStarted=false;
                if (thread!=null)
                {
                    thread.interrupt();
                }
                Toast.makeText(this,"Service Stopped, please set the timmer again",Toast.LENGTH_LONG).show();
            }



        }
        else if (v.getId()==R.id.logoutBtn)
        {
            mHandler.sendEmptyMessage(MSG_STOP_TIMER);
            locationManager.removeUpdates(this);
            stopped();
            if (serviceStarted==true)
            {
                stopService(si);
                serviceStarted=false;
                if (thread!=null)
                {
                    thread.interrupt();
                }
                Toast.makeText(this,"Service Stopped, please set the timmer again",Toast.LENGTH_LONG).show();
            }
            FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();
            firebaseAuth.signOut();
            Intent intent=new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        else if (v.getId()==R.id.setTimeBtn)
        {
            if (serviceStarted!=true)
            {
                startService(si);
                serviceStarted=true;
                requestForGps();
            }
            else
            {
                Toast.makeText(this,"Service already started, press reset to stop",Toast.LENGTH_SHORT).show();
            }

        }
    }

    public class Stopwatch {

        private long startTime = 0;
        private long stopTime = 0;
        private boolean running = false;

        public void start() {
            this.startTime = System.currentTimeMillis();
            this.running = true;
        }
        public void stop() {
            this.stopTime = System.currentTimeMillis();
            this.running = false;
        }
        // elaspsed time in milliseconds
        public long getElapsedTime() {
            if (running) {
                return System.currentTimeMillis() - startTime;
            }
            return stopTime - startTime;
        }
        // elaspsed time in seconds
        public long getElapsedTimeSecs() {
            if (running) {
                return ((System.currentTimeMillis() - startTime) / 1000);
            }
            return ((stopTime - startTime) / 1000);
        }


    }
    public void stopped()
    {

        if (totalSec<=0 || dis<=0)
        {
            RunningUserModel RUsers2= new RunningUserModel(userName,"Paused",t,dis,0);
            databaseReference.child(uiid).setValue(RUsers2);
        }
        else
        {
            RunningUserModel RUsers= new RunningUserModel(userName,"Paused",t,dis,dis/totalSec);
            databaseReference.child(uiid).setValue(RUsers);
        }

    }
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    // Before 2.0
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    public void onDestroy()
    {
        super.onDestroy();
        if (totalSec<=0 || dis<=0)
        {
            RunningUserModel RUsers2= new RunningUserModel(userName,"Paused",t,dis,0);
            databaseReference.child(uiid).setValue(RUsers2);
        }
        else
        {
            RunningUserModel RUsers= new RunningUserModel(userName,"Paused",t,dis,dis/totalSec);
            databaseReference.child(uiid).setValue(RUsers);
        }

    }

    void distanceBetweenTwoPoint(double srcLat, double srcLng, double desLat, double desLng) {
        Location startPoint=new Location("locationA");
        startPoint.setLatitude(srcLat);
        startPoint.setLongitude(srcLng);

        Location endPoint=new Location("locationA");
        endPoint.setLatitude(desLat);
        endPoint.setLongitude(desLng);

        distance=distance+startPoint.distanceTo(endPoint);

        double km=distance/1000;
        dis=(double)Math.round(km * 100d) / 100d;
//        distanceTV.post(new Runnable() {
//            @Override
//            public void run() {
//
//                distanceTV.setText(String.valueOf(dis));
//            }
//        });
        distanceTV.setText(String.valueOf(dis));
        Toast.makeText(this,"distance : "+dis+" latandlong: "+desLat+desLng,Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED)
        {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED)
            {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, this);
            }
        }
    }



    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {

        final AlertDialog.Builder dialogBuilder=new AlertDialog.Builder(this);
        final LayoutInflater inflater=getLayoutInflater();
        final View dialogView=inflater.inflate(R.layout.deleteracer,null);
        dialogBuilder.setView(dialogView);
        deleteRacer=dialogView.findViewById(R.id.deleteRacer);
        dialogBuilder.setTitle("Update or Delete this Runner?");
        final AlertDialog alertDialog=dialogBuilder.create();
        alertDialog.show();


        deleteRacer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

             alertDialog.dismiss();
                final String n=userNameArrayList.get(position);
                 final DatabaseReference d=FirebaseDatabase.getInstance().getReference("RunningUsers");
                d.orderByChild("userName").equalTo(n).addListenerForSingleValueEvent(new ValueEventListener(){
                    String keys;
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot datas : dataSnapshot.getChildren()) {
                            keys = datas.getKey();
                            if(keys!=null) {
                                FirebaseDatabase.getInstance().getReference("RunningUsers").child(keys).removeValue();


                            }



                        }
                      //  Toast.makeText(getApplicationContext(),keys,Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }


                });



            }


        });




        return false;
    }
    public void requestForGps()
    {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(RaceActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
            //Toast.makeText(getApplicationContext(),"if",Toast.LENGTH_LONG).show();
        }
        else
        {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, this);
            //  Toast.makeText(getApplicationContext(),"else",Toast.LENGTH_LONG).show();
        }
    }


    public static class MyServiceClass extends Service implements Runnable
    {
        int hourOfET=0,minOfET=0,hour,min;
        Calendar rightNow;


       public MyServiceClass()
       {
            super();
       }
        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        @Override
        public void onCreate() {
            super.onCreate();
            rightNow = Calendar.getInstance();
            hour=rightNow.get(Calendar.HOUR_OF_DAY);
            min=rightNow.get(Calendar.MINUTE);
            if(hourET.getText().toString().isEmpty() && minET.getText().toString().isEmpty())
            {
                hourOfET=Integer.parseInt(hourET.getText().toString());
                minOfET=Integer.parseInt(minET.getText().toString());
            }
            else
            {
                hourOfET=15;
                minOfET=0;
            }


            thread=new Thread(this);

            Log.d("service","onCreate");


        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            if ((hourOfET>=0 && hourOfET<=23) && (minOfET>=0 && minOfET<=59) )
                {
                    Log.d("service",hourOfET+" "+minOfET);
                thread.start();
            }
            else
            {
                stopSelf();
                Log.d("service","wrong hour and min at edittext");

            }



            return super.onStartCommand(intent, flags, startId);
        }
        @Override
        public void onDestroy() {
            serviceStarted=false;
            super.onDestroy();
            Log.d("service","onDestroy");

        }

        @Override
        public void run() {
            while(hour!=hourOfET || min!=minOfET)
            {
                Log.d("service","onStartCommand "+hour+"  "+min);
                rightNow = Calendar.getInstance();
                hour=rightNow.get(Calendar.HOUR_OF_DAY);
                min=rightNow.get(Calendar.MINUTE);
                if (hour==hourOfET && min==minOfET)
                {
                    mHandler.sendEmptyMessage(MSG_START_TIMER);
                    thread.interrupt();
                    stopSelf();



                }
            }

        }
    }
}
