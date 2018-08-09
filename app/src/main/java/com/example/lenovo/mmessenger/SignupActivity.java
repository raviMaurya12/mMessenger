package com.example.lenovo.mmessenger;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class SignupActivity extends AppCompatActivity {

    private static final String TAG ="message" ;
    private FirebaseAuth mAuth;    //Firebase
    private Toolbar mToolbar;
    private ProgressDialog reg_progress;

    private DatabaseReference mDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();   //Firebase

        final EditText reg_et_displayname=(EditText)findViewById(R.id.reg_et_displayname);
        final EditText reg_et_email=(EditText)findViewById(R.id.login_et_email);
        final EditText reg_et_password=(EditText)findViewById(R.id.reg_et_password);
        Button reg_signup_button=(Button)findViewById(R.id.reg_signup_button);

        //To display the toolbar and the home button
        mToolbar = (Toolbar)findViewById(R.id.signup_toolbar) ;
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Initializing Progress Dialog
        reg_progress=new ProgressDialog(this);

        //Create Accout button click event
        reg_signup_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               String displayname=reg_et_displayname.getText().toString();
               String email=reg_et_email.getText().toString();
               String password=reg_et_password.getText().toString();

                if(!TextUtils.isEmpty(displayname) || !TextUtils.isEmpty(email) || !TextUtils.isEmpty(password)){

                    reg_progress.setTitle("Registering User");
                    reg_progress.setMessage("Please wait while we create the account");
                    reg_progress.setCanceledOnTouchOutside(false);
                    reg_progress.show();
                    register(displayname,email,password);

                }else{
                    Toast.makeText(SignupActivity.this, "All field are Mandatory.Please try again.",
                            Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    //Method to so the Registration on Firebase
    private void register(final String displayname,final String email,final String password) {

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");

                            //Getting UID
                            FirebaseUser current_user=FirebaseAuth.getInstance().getCurrentUser();
                            String uid=current_user.getUid();

                            mDatabase= FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

                            String deviceToken= FirebaseInstanceId.getInstance().getToken();

                            //Entering key and values in the table
                            HashMap<String,String> usermap=new HashMap<>();
                            usermap.put("device_token",deviceToken);
                            usermap.put("name",displayname);
                            usermap.put("image","default");
                            usermap.put("status","Hi there,I'm Using mMessenger.");
                            usermap.put("thumb_image","default");
                            mDatabase.setValue(usermap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        reg_progress.dismiss();
                                        Intent mainActivity=new Intent(SignupActivity.this,MainActivity.class);
                                        mainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(mainActivity);
                                        finish();
                                    }
                                }
                            });


                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            reg_progress.hide();
                            Toast.makeText(SignupActivity.this, "Unable to create Account.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}











