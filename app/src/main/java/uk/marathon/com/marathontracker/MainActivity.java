package uk.marathon.com.marathontracker;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity  implements View.OnClickListener{

    TextView signupHereTV;
    EditText loginEmailET,loginPasswordET;
    Button loginBtn;
    String loginEmail,loginPassword;
    private FirebaseAuth mAuth;
    ProgressBar loginProgessBar;
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loginProgessBar=findViewById(R.id.loginProgressBar);
        loginProgessBar.setVisibility(View.GONE);
        loginEmailET=findViewById(R.id.loginEmailET);
        loginPasswordET=findViewById(R.id.loginPasswordET);

        loginBtn=findViewById(R.id.startBtn);
        loginBtn.setOnClickListener(this);
        signupHereTV=findViewById(R.id.signupHereTV);
        signupHereTV.setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();


    }

    @Override
    public void onStart() {
        super.onStart();
        Toast.makeText(this,"onStart",Toast.LENGTH_SHORT).show();
        FirebaseAuth m=FirebaseAuth.getInstance();
        FirebaseUser currentUser = m.getCurrentUser();
        if (currentUser!=null) {
            Intent i=new Intent(this,RaceActivity.class);
            startActivity(i);

        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId()==R.id.startBtn)
        {
            loginEmail=loginEmailET.getText().toString();
            loginPassword=loginPasswordET.getText().toString();
            if (loginEmail.isEmpty())
            {
                loginEmailET.setError("Please enter email here");
                loginEmailET.requestFocus();
                return;
            }
            if(!Patterns.EMAIL_ADDRESS.matcher(loginEmail).matches())
            {
                loginEmailET.setError("Please enter valid email here");
                loginEmailET.requestFocus();
                return;
            }
            if (loginPassword.isEmpty())
            {
                loginPasswordET.setError("Please enter password here");
                loginPasswordET.requestFocus();
                return;

            }
            if (loginPassword.length()<8)
            {
                loginPasswordET.setError("Please enter atlest 8 character password here");
                loginPasswordET.requestFocus();
                return;

            }
            loginProgessBar.setVisibility(View.VISIBLE);
            mAuth.signInWithEmailAndPassword(loginEmail, loginPassword)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(getApplicationContext(),"user successfully logined",Toast.LENGTH_LONG).show();
                                loginProgessBar.setVisibility(View.GONE);
                                Intent i=new Intent(getApplicationContext(),RaceActivity.class);
                                startActivity(i);

                            } else {
                                Toast.makeText(getApplicationContext(), "failed",Toast.LENGTH_LONG).show();
                                loginProgessBar.setVisibility(View.GONE);
                            }
                        }
                    });

        }

        if (v.getId()==R.id.signupHereTV) {
            Intent intent = new Intent(this, SignupActivity.class);
            startActivity(intent);
            finish();
        }
    }

}
