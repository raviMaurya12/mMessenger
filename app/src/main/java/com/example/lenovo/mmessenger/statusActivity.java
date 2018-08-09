package com.example.lenovo.mmessenger;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class statusActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private android.support.v7.widget.Toolbar mToolbar ;
    private EditText status_et;
    private Button save_button;
    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        //getting the uid of the current user
        FirebaseUser current_user=FirebaseAuth.getInstance().getCurrentUser();
        String uid=current_user.getUid();

        //getting database instance and pointing to the database
        mAuth = FirebaseAuth.getInstance();
        mDatabase= FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

        //setting up the toolbar
        mToolbar=(android.support.v7.widget.Toolbar)findViewById(R.id.status_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("STATUS");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        status_et=(EditText)findViewById(R.id.status_et);
        save_button=(Button)findViewById(R.id.status_save_button);

        //Getting the status text from the Settings activity and changing the text in the Edittext.
        String status_text=getIntent().getStringExtra("status_text");
        status_et.setText(status_text);

        //On save changes button click
        save_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //creating progress dialog box
                mProgress=new ProgressDialog(statusActivity.this);
                mProgress.setTitle("Saving changes");
                mProgress.setMessage("Please wait while we save the changes");
                mProgress.show();

                //Getting the string and entering into the Database
                String status=status_et.getText().toString();
                mDatabase.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            //Got to Settings Activity and dismiss the Progress Dialog
                            Intent setting_intent = new Intent(statusActivity.this,AccountSetting.class);
                            startActivity(setting_intent);
                            finish();
                            mProgress.dismiss();
                        }else{
                            mProgress.hide();
                            Toast.makeText(statusActivity.this,"Unable to save the changes.",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        mDatabase.child("online").setValue(true);
        mDatabase.child( "last_seen" ).setValue( "online" );
    }

    @Override
    protected void onPause() {
        super.onPause();
        mDatabase.child("online").setValue(false);
        mDatabase.child( "last_seen" ).setValue( ServerValue.TIMESTAMP );
    }
}
