package com.example.lenovo.mmessenger;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG ="message" ;
    private TextView mName;
    private TextView mStatus;
    private ImageView mImage;
    private ImageView profileGreenDot;
    private Button sendrequest;
    private Button declinerequest;
    private DatabaseReference usersDatabaseRef;
    private DatabaseReference mDatabase;
    private DatabaseReference fri_req_database;
    private DatabaseReference fri_list_database;
    private DatabaseReference mNotificationDatabase;
    private FirebaseUser current_user;
    private String current_state;
    private ProgressBar mProgress;
    private RelativeLayout profile_layout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);


        final String from_user_id=getIntent().getStringExtra("from_user_id");

        current_user=FirebaseAuth.getInstance().getCurrentUser();

        current_state="not_friend";
        final String currentDate= DateFormat.getDateTimeInstance().format(new Date());

        mName=(TextView)findViewById(R.id.profile_display_name);
        mStatus=(TextView)findViewById(R.id.profile_status);
        mImage=(ImageView)findViewById(R.id.profile_image);
        profileGreenDot=(ImageView)findViewById( R.id.profile_greendot );
        sendrequest=(Button)findViewById(R.id.profile_sendrequest);
        declinerequest=(Button)findViewById(R.id.profile_declinerequest);
        mProgress=(ProgressBar)findViewById( R.id.profile_progressBar ) ;
        profile_layout=(RelativeLayout)findViewById( R.id.profile_layout );

        declinerequest.setVisibility(View.INVISIBLE);
        declinerequest.setEnabled(false);

        mDatabase= FirebaseDatabase.getInstance().getReference().child("Users").child(from_user_id);
        fri_req_database=FirebaseDatabase.getInstance().getReference().child("friend_req");
        fri_list_database=FirebaseDatabase.getInstance().getReference().child("friends");
        mNotificationDatabase=FirebaseDatabase.getInstance().getReference().child("notification");


        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name=dataSnapshot.child("name").getValue().toString();
                String status=dataSnapshot.child("status").getValue().toString();
                String image=dataSnapshot.child("image").getValue().toString();
                if(dataSnapshot.hasChild( "online" )) {
                    Boolean isonline = (Boolean) dataSnapshot.child( "online" ).getValue();
                    if (isonline == true) {
                        profileGreenDot.setVisibility( VISIBLE );
                    }
                }
                mName.setText(name);
                mStatus.setText(status);

                Picasso.get().load(image).placeholder(R.drawable.defaultpic).into(mImage);

                //-------------CHECKING REQUEST TYPE FOR CURRENT USER::WANT TO ACCEPT TO DECLINE FRIEND REQUST-----------------
                fri_req_database.child(current_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(from_user_id)) {
                            String req_type=dataSnapshot.child(from_user_id).child("request_type").getValue().toString();
                            if(req_type.equals("received")){
                                sendrequest.setText("ACCEPT FRIEND REQUEST");
                                declinerequest.setVisibility( VISIBLE);
                                declinerequest.setEnabled(true);
                                current_state="received";
                            }else if(req_type.equals("sent")){
                                sendrequest.setText("CANCEL FRIEND REQUEST");
                                current_state="req_sent";
                            }
                        }else{
                            fri_list_database.child(current_user.getUid()).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(from_user_id)){
                                       sendrequest.setText("UNFRIEND THIS PERSON");
                                       current_state="friends";
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        sendrequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgress.setIndeterminate( true );
                mProgress.setVisibility( VISIBLE );

                //-------------------REQUEST_TYPE=NOT_SET AND STATE=NOT _FRIEND::WANT TO SEND REQUEST--------------------
                if(current_state.equals("not_friend")){
                    sendrequest.setEnabled(false);
                    fri_req_database.child(from_user_id).child(current_user.getUid()).child("request_type").setValue("received")
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                fri_req_database.child(current_user.getUid()).child(from_user_id).child("request_type").setValue("sent")
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                                HashMap<String,String> notificationData = new HashMap<>();
                                                notificationData.put("from",current_user.getUid());
                                                notificationData.put("type","request");
                                                mNotificationDatabase.child(from_user_id).push().setValue(notificationData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Toast.makeText(ProfileActivity.this,"Friend Request sent.",Toast.LENGTH_SHORT).show();
                                                        sendrequest.setEnabled(true);
                                                        current_state="req_sent";
                                                        sendrequest.setText("CANCEL FRIEND REQUEST");
                                                        profile_layout.post( new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                mProgress.setVisibility( GONE );
                                                            }
                                                        } );
                                                    }
                                                });
                                            }
                                        });
                            }else{
                                Toast.makeText(ProfileActivity.this,"Request sending failed.",Toast.LENGTH_SHORT)
                                        .show();
                            }
                        }
                    });
                //--------------------REQUEST TYPE=SENT AND STATE=REQUEST_SENT::WANT TO CANCEL REQUEST-------------------
                }else if (current_state.equals("req_sent")){
                    sendrequest.setEnabled(false);
                    fri_req_database.child(current_user.getUid()).child(from_user_id).removeValue()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            fri_req_database.child(from_user_id).child(current_user.getUid()).removeValue()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(ProfileActivity.this,"Friend request cancelled.",Toast.LENGTH_SHORT)
                                            .show();
                                    current_state="not_friend";
                                    sendrequest.setText("SEND FRIEND REQUEST");
                                    sendrequest.setEnabled(true);
                                    profile_layout.post( new Runnable() {
                                        @Override
                                        public void run() {
                                            mProgress.setVisibility( GONE );
                                        }
                                    } );
                                }
                            });
                        }
                    });
                }else if(current_state.equals("received")){
                    sendrequest.setEnabled(false);
                    fri_list_database.child(from_user_id).child(current_user.getUid()).child("date").setValue(currentDate)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            fri_list_database.child(current_user.getUid()).child(from_user_id).child("date").setValue(currentDate)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    fri_req_database.child(current_user.getUid()).child(from_user_id).removeValue()
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            fri_req_database.child(from_user_id).child(current_user.getUid()).removeValue()
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    sendrequest.setText("UNFRIEND THIS PERSON");
                                                    current_state="friends";
                                                    sendrequest.setEnabled(true);
                                                    profile_layout.post( new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            mProgress.setVisibility( GONE );
                                                        }
                                                    } );
                                                }
                                            });
                                        }
                                    });
                                }
                            });
                        }
                    });
                }else if(current_state.equals("friends")){
                    sendrequest.setEnabled(false);
                    fri_list_database.child(current_user.getUid()).child(from_user_id).child("date").removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            fri_list_database.child(from_user_id).child(current_user.getUid()).child("date").removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    current_state="not_friend";
                                    sendrequest.setText("SEND FRIEND REQUEST");
                                    sendrequest.setEnabled(true);
                                    profile_layout.post( new Runnable() {
                                        @Override
                                        public void run() {
                                            mProgress.setVisibility( GONE );
                                        }
                                    } );
                                }
                            });
                        }
                    });
                }

            }
        });



    }

    @Override
    protected void onResume() {
        super.onResume();
        usersDatabaseRef=FirebaseDatabase.getInstance().getReference().child("Users");
        usersDatabaseRef.child(current_user.getUid()).child("online").setValue(true);
        usersDatabaseRef.child( current_user.getUid()).child( "last_seen" ).setValue( "online" );
    }

    @Override
    protected void onPause() {
        super.onPause();
        usersDatabaseRef.child(current_user.getUid()).child("online").setValue(false);
        usersDatabaseRef.child(current_user.getUid()).child( "last_seen" ).setValue( ServerValue.TIMESTAMP );
    }
}
