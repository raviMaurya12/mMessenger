package com.example.lenovo.mmessenger;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class ChatsFragment extends Fragment {


    public ChatsFragment() {
        // Required empty public constructor
    }

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private  DatabaseReference usersdatabaseRef;
    private String mCurrentUID;
    private RecyclerView mRecyclerView;
    private View Fview;
    private LinearLayoutManager mManager;
    private TextView empty_view;
    private ProgressDialog mProgress;

    FirebaseRecyclerAdapter<chats,chatsViewHolder> mAdapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Fview=inflater.inflate( R.layout.fragment_chats, container, false );
        mAuth=FirebaseAuth.getInstance();
        mCurrentUID=mAuth.getCurrentUser().getUid();
        mDatabase=FirebaseDatabase.getInstance().getReference().child( "chat" ).child( mCurrentUID );
        usersdatabaseRef=FirebaseDatabase.getInstance().getReference().child("Users");

        mRecyclerView=(RecyclerView)Fview.findViewById( R.id.chats_recycler_list );
        mManager=new LinearLayoutManager( Fview.getContext() );
        mRecyclerView.setLayoutManager( mManager );

        empty_view=(TextView)Fview.findViewById( R.id.chats_empty_view);
        mProgress=new ProgressDialog(Fview.getContext() );
        // Inflate the layout for this fragment
        return Fview;
    }

    @Override
    public void onStart() {
        super.onStart();
        
        mProgress.setTitle( "Loading" );
        mProgress.setMessage( "Please wait while we get things ready for you." );
        mProgress.setCanceledOnTouchOutside( false );
        mProgress.show();

        FirebaseRecyclerOptions<chats> options = new FirebaseRecyclerOptions.Builder<chats>().setQuery( mDatabase,chats.class ).build();
        mAdapter=new FirebaseRecyclerAdapter<chats,chatsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final chatsViewHolder holder, int position, @NonNull chats model) {
                final String friend_id=getRef( position ).getKey();

                usersdatabaseRef.child( friend_id ).addValueEventListener( new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final String Fname=dataSnapshot.child("name").getValue().toString();
                        final String Fstatus=dataSnapshot.child( "status" ).getValue().toString();
                        final String Fimage=dataSnapshot.child( "thumb_image" ).getValue().toString();

                        holder.mView.post( new Runnable() {
                            @Override
                            public void run() {
                                holder.display(Fname,Fstatus,Fimage);
                                mProgress.dismiss();
                            }
                        } );
                    }


                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                                mProgress.dismiss();
                        Toast.makeText( getContext(), "Something Went Wrong.Please check your connection.", Toast.LENGTH_SHORT ).show();
                    }
                } );



                holder.mView.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent chat_Intent=new Intent( getContext(),ChatActivity.class );
                        chat_Intent.putExtra( "friendID",friend_id );
                        chat_Intent.putExtra( "CurrentUserID", mCurrentUID);
                        startActivity(chat_Intent);
                    }
                } );

            }

            @NonNull
            @Override
            public chatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view=LayoutInflater.from( parent.getContext() ).inflate( R.layout.single_user,parent,false );
                return new chatsViewHolder( view );
            }
        };

        mDatabase.addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getChildrenCount()==0){
                    mRecyclerView.setVisibility( View.GONE );
                    empty_view.setVisibility( View.VISIBLE );
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        } );


        mAdapter.startListening();
        mRecyclerView.setAdapter( mAdapter );
    }

    @Override
    public void onStop() {
        super.onStop();
        mAdapter.stopListening();
    }

    public static class chatsViewHolder extends RecyclerView.ViewHolder {

        private View mView;
        private CircleImageView dp;
        private TextView name,status;

        public chatsViewHolder(View itemView) {
            super( itemView );
            mView=itemView;
        }


        public void display(String fname, String fstatus, String fimage) {

            name=(TextView)mView.findViewById( R.id.model_displayname );
            status=(TextView)mView.findViewById( R.id.model_status );
            dp=(CircleImageView)mView.findViewById( R.id.model_image );

            name.setText( fname );
            status.setText( fstatus );
            Picasso.get().load( fimage ).placeholder( R.drawable.defaultpic ).into( dp );
        }
    }
}
