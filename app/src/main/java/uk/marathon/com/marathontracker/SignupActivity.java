package uk.marathon.com.marathontracker;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.Patterns;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignupActivity extends AppCompatActivity implements View.OnClickListener {

    EditText signupBookSeekerNameET,signupBookSeekerEmailET,signupBookSeekerPasswordET;
    TextView loginHereTV;
    Button signupBookSeekerBtn;
    String fullName,email,password;
    ImageView bookSeekerEyeImg;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();

        signupBookSeekerNameET=findViewById(R.id.signupBookSeekerNameET);
        signupBookSeekerEmailET=findViewById(R.id.signupBookSeekerEmailET);
        signupBookSeekerPasswordET=findViewById(R.id.signupBookSeekerPasswordET);
        signupBookSeekerBtn=findViewById(R.id.signupBookSeekerBtn);
        loginHereTV=findViewById(R.id.loginHereTV);
        loginHereTV.setOnClickListener(this);
        signupBookSeekerBtn.setOnClickListener(this);


        bookSeekerEyeImg=findViewById(R.id.bookSeekerEyeImg);
        bookSeekerEyeImg.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch ( event.getAction() ) {
                    case MotionEvent.ACTION_DOWN:
                        signupBookSeekerPasswordET.setInputType(InputType.TYPE_CLASS_TEXT);
                        break;
                    case MotionEvent.ACTION_UP:
                        signupBookSeekerPasswordET.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        break;
                }
                return true;
            }
        });
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.signupBookSeekerBtn) {
            registerUser();


        }
        if (v.getId()==R.id.loginHereTV)
        {
            Intent intent=new Intent(this,MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    public void registerUser()
    {
        fullName=signupBookSeekerNameET.getText().toString();
        email=signupBookSeekerEmailET.getText().toString();
        password=signupBookSeekerPasswordET.getText().toString();

        if (fullName.isEmpty())
        {
            signupBookSeekerNameET.setError("Please enter your full name here");
            signupBookSeekerNameET.requestFocus();
            return;
        }
        if (email.isEmpty())
        {
            signupBookSeekerEmailET.setError("Please enter a email here");
            signupBookSeekerEmailET.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            signupBookSeekerEmailET.setError("Please enter a valid email here");
            signupBookSeekerEmailET.requestFocus();
            return;
        }
        if (password.isEmpty())
        {
            signupBookSeekerPasswordET.setError("Please enter a password here");
            signupBookSeekerPasswordET.requestFocus();
            return;
        }
        if(password.length()<8)
        {
            signupBookSeekerPasswordET.setError("Please enter 8-character passsword here");
            signupBookSeekerPasswordET.requestFocus();
            return;
        }


        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(),"successfully signuped",Toast.LENGTH_LONG).show();
                            FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();
                            String uid= firebaseAuth.getCurrentUser().getUid().toString();
                            Log.d("uid",uid);
                            DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference("Users");
                            UsersModel Users= new UsersModel(fullName,email,password);
                            databaseReference.child(uid).setValue(Users);
                            Intent i=new Intent(getApplicationContext(),MainActivity.class);
                            startActivity(i);

                        }
                        else {
                            if (task.getException() instanceof FirebaseAuthUserCollisionException)
                            {
                                signupBookSeekerEmailET.setError("Use another email because it's already used");
                                signupBookSeekerEmailET.requestFocus();
                            }
                            // If sign in fails, display a message to the user.
                            else {
                                Toast.makeText(getApplicationContext(), "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }

                        }

                    }
                });

    }
}
