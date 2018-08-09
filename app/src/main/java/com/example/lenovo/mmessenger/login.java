package com.example.lenovo.mmessenger;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class login extends AppCompatActivity {

    private static final String TAG ="Login_Message" ;
    private FirebaseAuth mAuth;    //Firebase
    private Toolbar mtoolbar;
    private EditText et_email;
    private EditText et_password;
    private Button signin_button;
    private ProgressDialog login_progress;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();  //Firebase

        //Working on toolbar
        mtoolbar=(Toolbar)findViewById(R.id.login_toolbar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle("Login");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Initailizing Progress Dialog
        login_progress=new ProgressDialog(this);

        et_email=(EditText)findViewById(R.id.login_et_email);
        et_password=(EditText)findViewById(R.id.login_et_password);
        signin_button=(Button)findViewById(R.id.login_signin_button);
        mDatabase= FirebaseDatabase.getInstance().getReference().child("Users");

        signin_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email=et_email.getText().toString();
                String password=et_password.getText().toString();

                if(!TextUtils.isEmpty(email) || !TextUtils.isEmpty(password)) {

                    //Building on Progress dialog
                    login_progress.setTitle("Logging In");
                    login_progress.setMessage("Please wait we Log you In");
                    login_progress.setCanceledOnTouchOutside(false);
                    login_progress.show();
                    Login(email, password);
                }else{
                    Toast.makeText(login.this,"Please fill Both email and password.",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    //Method to login the user through Firebase
    private void Login(String email, String password) {

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            login_progress.dismiss();

                            FirebaseUser user = mAuth.getCurrentUser();
                            String deviceToken= FirebaseInstanceId.getInstance().getToken();
                            String uid=user.getUid();
                            mDatabase.child(uid).child("device_token").setValue(deviceToken);

                            Intent mainIntent=new Intent(login.this,MainActivity.class);
                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(mainIntent);
                            finish();

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            login_progress.hide();
                            Toast.makeText(login.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }
}