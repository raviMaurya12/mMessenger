package com.example.lenovo.mmessenger;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerAdapter_LifecycleAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView mRecyclerview;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    public String uid;

    FirebaseRecyclerAdapter<Users, UserViewHolder> firebaseRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        mAuth= FirebaseAuth.getInstance();
        uid = mAuth.getCurrentUser().getUid();

        mToolbar = (Toolbar) findViewById(R.id.users_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRecyclerview = (RecyclerView) findViewById(R.id.users_list);
        mRecyclerview.setLayoutManager(new LinearLayoutManager(this));

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Users> options = new FirebaseRecyclerOptions.Builder<Users>().setQuery(mDatabase, Users.class).build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, UserViewHolder>(options) {

            @NonNull
            @Override
            public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.single_user, parent, false);
                return new UserViewHolder(view);
            }
            @Override
            protected void onBindViewHolder(@NonNull final UserViewHolder holder, int position, @NonNull Users model) {
                holder.setName(model.getName());
                holder.setStatus(model.getStatus());
                holder.setImage(model.getThumb_image(),getApplicationContext());
                holder.setIsRecyclable(false);

                String listUser=getRef( position ).getKey();
                mDatabase.child(listUser).addValueEventListener( new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild( "online" )){
                            Boolean isOnline=(Boolean)dataSnapshot.child("online").getValue();
                            holder.setOnline(isOnline);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                } );


                final String user_id=getRef(position).getKey();

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent profile_Intent = new Intent(UsersActivity.this,ProfileActivity.class);
                        profile_Intent.putExtra("from_user_id",user_id);
                        startActivity(profile_Intent);
                    }
                });
            }
        };

        firebaseRecyclerAdapter.startListening();
        mRecyclerview.setAdapter(firebaseRecyclerAdapter);

    }

    @Override
    protected void onResume() {
        super.onResume();
        mDatabase.child(uid).child( "online" ).setValue( true );
        mDatabase.child( uid ).child( "last_seen" ).setValue( "online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        mDatabase.child(uid).child( "online" ).setValue( false );
        mDatabase.child( uid ).child( "last_seen" ).setValue( ServerValue.TIMESTAMP);
        firebaseRecyclerAdapter.stopListening();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        View mView;
        public TextView model_display_name;
        public TextView model_status;
        public CircleImageView model_image_view;
        public ImageView greenDot;

        public UserViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }

        public void setName(String name) {
            model_display_name=(TextView)mView.findViewById(R.id.model_displayname);
            model_display_name.setText(name);
        }

        public void setStatus(String status) {
            model_status=(TextView)mView.findViewById(R.id.model_status);
            model_status.setText(status);
        }

        public void setImage(String image, Context ctx) {
            model_image_view=(CircleImageView)mView.findViewById(R.id.model_image);
            Picasso.get().load(image).placeholder(R.drawable.defaultpic).into(model_image_view);
        }

        public void setOnline(Boolean online) {
            greenDot=(ImageView)mView.findViewById( R.id.greendot );
            if(online==true){
                greenDot.setVisibility( View.VISIBLE );
            }
        }
    }
}


