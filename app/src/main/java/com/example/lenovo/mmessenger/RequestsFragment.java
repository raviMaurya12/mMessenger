package com.example.lenovo.mmessenger;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class RequestsFragment extends Fragment {


    public RequestsFragment() {
        // Required empty public constructor
    }

    private View Fview;
    private RecyclerView request_list;
    FirebaseRecyclerAdapter<Requests,RequestHolder> mFirebaseRecyclerAdapter;
    private DatabaseReference mDatabase;
    private DatabaseReference usersDatabaseRef;
    private FirebaseAuth mAuth;
    private LinearLayoutManager mLayoutmanger;
    private TextView empty_view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Fview=inflater.inflate(R.layout.fragment_requests, container, false);
        request_list=(RecyclerView)Fview.findViewById( R.id.requests_list );
        Context ctx=Fview.getContext();
        mLayoutmanger=new LinearLayoutManager(ctx);
        request_list.setLayoutManager(mLayoutmanger);
        empty_view=(TextView)Fview.findViewById( R.id.requests_empty_view );

        mAuth=FirebaseAuth.getInstance();
        String current_userid=mAuth.getCurrentUser().getUid();

        mDatabase=FirebaseDatabase.getInstance().getReference().child("friend_req").child(current_userid);
        mDatabase.keepSynced( true );
        usersDatabaseRef=FirebaseDatabase.getInstance().getReference().child("Users");
        usersDatabaseRef.keepSynced( true );
        return Fview;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Requests> query = new FirebaseRecyclerOptions.Builder<Requests>().setQuery(mDatabase,Requests.class).build();
        mFirebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Requests, RequestHolder>(query) {
            @Override
            protected void onBindViewHolder(@NonNull final RequestHolder holder, int position, @NonNull final Requests model) {
            final String reqFriendId=getRef(position).getKey();

            usersDatabaseRef.child(reqFriendId).addValueEventListener( new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String reqFriendName=dataSnapshot.child("name").getValue().toString();
                    String reqFriendStatus=dataSnapshot.child("status").getValue().toString();
                    String reqFriendImageStr=dataSnapshot.child("thumb_image").getValue().toString();
                    if(dataSnapshot.hasChild( "online" )) {
                        Boolean reqFriendOnline = (Boolean) dataSnapshot.child( "online" ).getValue();
                        holder.setOnline( reqFriendOnline );
                    }
                    holder.setName(reqFriendName);
                    holder.setStatus(model.getRequest_type());
                    holder.setImage(reqFriendImageStr);

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            } );

            holder.mView.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent profile_Intent = new Intent(getContext(),ProfileActivity.class);
                    profile_Intent.putExtra("from_user_id",reqFriendId);
                    startActivity(profile_Intent);
                }
            } );

            }

            @NonNull
            @Override
            public RequestHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.single_user, parent, false);
                return new RequestHolder(view);
            }
        };

        mDatabase.addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getChildrenCount()==0){
                    request_list.setVisibility( View.GONE );
                    empty_view.setVisibility( View.VISIBLE );
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        } );

        mFirebaseRecyclerAdapter.startListening();
        request_list.setAdapter(mFirebaseRecyclerAdapter);

        }


    @Override
    public void onStop() {
        super.onStop();
        mFirebaseRecyclerAdapter.stopListening();
    }

    public static class RequestHolder extends RecyclerView.ViewHolder{

        private View mView;
        private CircleImageView image;
        private TextView name;
        private TextView status;
        private ImageView greenDot;

        public RequestHolder(View itemView) {
            super( itemView );
            mView=itemView;
        }

        public void setName(String reqFriendName) {
            name=(TextView)mView.findViewById( R.id.model_displayname );
            name.setText(reqFriendName);
        }

        public void setStatus(String reqFriendStatus) {
            status=(TextView)mView.findViewById(R.id.model_status);
            status.setText(reqFriendStatus);
        }

        public void setImage(String reqFriendImageStr) {
            image=(CircleImageView)mView.findViewById( R.id.model_image );
            Picasso.get().load(reqFriendImageStr).placeholder( R.drawable.defaultpic ).into(image);
        }

        public void setOnline(Boolean reqFriendOnline) {
            if(reqFriendOnline==true) {
                greenDot = (ImageView) mView.findViewById( R.id.greendot );
                greenDot.setVisibility( View.VISIBLE );
            }
        }
    }
}
