package com.example.lenovo.mmessenger;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Adapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


public class ChatActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private String FriendId;
    private String FriendName;
    private String FriendImage;
    private TextView customToolbarName,customToolbarLastSeen;
    private CircleImageView customToolbarImage;
    private DatabaseReference usersdatabase;
    private DatabaseReference mRootRef;
    private FirebaseAuth mAuth;
    private String currentUserID;
    private String timestamp;
    private ImageButton chat_send_button;
    private EditText chat_message_et;
    private  ImageButton chat_add_button;
    private String chat_message;
    private RecyclerView mMessageList;
    private final List<Messages> messagesList=new ArrayList<>();
    private LinearLayoutManager layoutManager;
    private MessageAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_chat );

        FriendId=getIntent().getStringExtra( "friendID" );
        currentUserID=getIntent().getStringExtra( "CurrentUserID" );
        mToolbar=(Toolbar)findViewById( R.id.chat_app_bar );
        setSupportActionBar( mToolbar );

        LayoutInflater inflater = (LayoutInflater)this.getSystemService(LAYOUT_INFLATER_SERVICE);
        View action_bar_view=inflater.inflate(R.layout.chat_custom_toolbar,null);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled( true );
        actionBar.setDisplayShowCustomEnabled( true );
        actionBar.setCustomView(action_bar_view);

        mAdapter=new MessageAdapter(messagesList);

        customToolbarName=(TextView)findViewById( R.id.chat_toolbar_name );
        customToolbarLastSeen=(TextView)findViewById( R.id.chat_toolbar_lastseen );
        customToolbarImage=(CircleImageView)findViewById( R.id.chat_toolbar_image );
        mMessageList=(RecyclerView)findViewById( R.id.chat_message_list );
        layoutManager=new LinearLayoutManager( this );
        mMessageList.setLayoutManager( layoutManager );

        mMessageList.setAdapter(mAdapter);
        usersdatabase= FirebaseDatabase.getInstance().getReference().child( "Users" );
        mRootRef=FirebaseDatabase.getInstance().getReference();    //Must be initiated before calling loadMessages.


    }

    private void loadMessages() {

        mRootRef.child( "messages" ).child( currentUserID ).child( FriendId ).orderByKey().addChildEventListener( new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Messages message = dataSnapshot.getValue( Messages.class );
                messagesList.add( message );
                mAdapter.notifyDataSetChanged();
                mMessageList.post(new Runnable() {
                    @Override
                    public void run() {
                        mMessageList.smoothScrollToPosition(mAdapter.getItemCount() - 1);
                    }
                });
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        } );
    }

    @Override
    protected void onResume() {
        super.onResume();
        usersdatabase.child( currentUserID ).child( "online" ).setValue( true );
        usersdatabase.child( currentUserID ).child( "last_seen" ).setValue( "online" );


        loadMessages();


        usersdatabase.child(FriendId).addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String FriendName=dataSnapshot.child("name").getValue().toString();
                String FriendImage=dataSnapshot.child("thumb_image").getValue().toString();
                Boolean isOnline=(Boolean)dataSnapshot.child("online").getValue();
                customToolbarName.setText(FriendName);
                Picasso.get().load( FriendImage ).placeholder( R.drawable.defaultpic ).into( customToolbarImage );
                if(isOnline==true){
                    customToolbarLastSeen.setText("online");
                }

                if(dataSnapshot.hasChild("last_seen")) {
                    if(isOnline==false){
                        timestamp=dataSnapshot.child("last_seen").getValue().toString();
                    //    GetTimeAgo get_time_ago = new GetTimeAgo();
                    //    long longTime = Long.valueOf(timestamp);
                    //    String last_seen_time = get_time_ago.getTimeAgo(longTime);
                        customToolbarLastSeen.setText( timestamp );
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        } );

        //Handling database "chat"----------------

        chat_send_button=(ImageButton)findViewById( R.id.chat_send_button ) ;
        chat_message_et=(EditText)findViewById( R.id.chat_message );
        chat_add_button=(ImageButton)findViewById( R.id.chat_add_button );

        Map chat_inner_map=new HashMap();
        chat_inner_map.put( "seen",false );
        chat_inner_map.put("timestamp",ServerValue.TIMESTAMP);

        Map chat_map=new HashMap();
        chat_map.put("chat/" + currentUserID + "/"+FriendId,chat_inner_map);
        chat_map.put("chat/" + FriendId + "/" + currentUserID,chat_inner_map);

        mRootRef.updateChildren( chat_map, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if(databaseError!=null){
                    Log.d( "CHAT ERROR LOG",databaseError.getMessage().toString() );
                }
            }
        } );


        //Handling Database messages on send button click--------

        chat_send_button.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                chat_message=chat_message_et.getText().toString();
                if(!TextUtils.isEmpty(chat_message)){
                    DatabaseReference push_ref=mRootRef.child("messages").child(currentUserID).child(FriendId).push();
                    String push_id_str=push_ref.getKey();

                    Map message_inner_map=new HashMap();
                    message_inner_map.put("message",chat_message);
                    message_inner_map.put("type","text");
                    message_inner_map.put("seen",false);
                    message_inner_map.put("timestamp",ServerValue.TIMESTAMP);
                    message_inner_map.put("from",currentUserID);

                    Map message_map=new HashMap();
                    message_map.put("messages/"+currentUserID+"/"+FriendId+"/"+push_id_str,message_inner_map);
                    message_map.put("messages/"+FriendId+"/"+currentUserID+"/"+push_id_str,message_inner_map);

                    mRootRef.updateChildren( message_map, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if(databaseError!=null){
                                Log.d( "MESSAGES ERROR LOG",databaseError.getMessage().toString() );
                            }
                        }
                    } );
                    chat_message_et.setText(null);
                    mMessageList.post(new Runnable() {
                        @Override
                        public void run() {
                            mMessageList.smoothScrollToPosition(mAdapter.getItemCount() - 1);
                        }
                    });
                }

            }
        } );
    }

    @Override
    protected void onPause() {
        super.onPause();
        usersdatabase.child( currentUserID ).child( "online" ).setValue( false );
        usersdatabase.child( currentUserID ).child( "last_seen" ).setValue( ServerValue.TIMESTAMP);
    }
}
