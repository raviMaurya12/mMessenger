package com.example.lenovo.mmessenger;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
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


public class FriendsFragment extends Fragment {


    public FriendsFragment() {
        // Required empty public constructor
    }

    private View Fview;
    private RecyclerView friends_listview;
    private LinearLayoutManager mLayoutmanger;
    private DatabaseReference mDatabase;
    private DatabaseReference friDatabase;
    private FirebaseAuth mAuth;
    public String fri_name;
    private String fri_img_string;
    private  String mCurrent_user_id;
    private TextView empty_view;

    FirebaseRecyclerAdapter<Friends,FriendsViewHolder> mFireRecyclerAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Fview=inflater.inflate(R.layout.fragment_friends, container, false);

        friends_listview=(RecyclerView)Fview.findViewById(R.id.friends_listview);
        Context ctx=Fview.getContext();
        mLayoutmanger=new LinearLayoutManager(ctx);
        friends_listview.setLayoutManager(mLayoutmanger);
        empty_view=(TextView)Fview.findViewById( R.id.friends_empty_view ) ;

        mAuth=FirebaseAuth.getInstance();
        mCurrent_user_id=mAuth.getCurrentUser().getUid();
        mDatabase= FirebaseDatabase.getInstance().getReference().child("friends").child(mCurrent_user_id);
        mDatabase.keepSynced( true );
        friDatabase=FirebaseDatabase.getInstance().getReference().child("Users");
        friDatabase.keepSynced( true );

        return Fview;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Friends> query=new FirebaseRecyclerOptions.Builder<Friends>().setQuery(mDatabase,Friends.class).build();
        mFireRecyclerAdapter=new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(query) {
            @Override
            protected void onBindViewHolder(@NonNull final FriendsViewHolder holder, int position, @NonNull Friends model) {
                holder.setDate(model.getDate());

                final String list_item_id=getRef(position).getKey();

                friDatabase.child(list_item_id).addValueEventListener( new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        fri_name=dataSnapshot.child("name").getValue().toString();
                        fri_img_string=dataSnapshot.child("thumb_image").getValue().toString();
                        if(dataSnapshot.hasChild( "online" )) {
                            Boolean fri_online = (Boolean) dataSnapshot.child( "online" ).getValue();
                            holder.setOnline( fri_online );
                        }

                        holder.setName( fri_name );
                        holder.setImage( fri_img_string );
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                } );

                holder.mView.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent chat_Intent=new Intent( getContext(),ChatActivity.class );
                        chat_Intent.putExtra( "friendID",list_item_id );
                        chat_Intent.putExtra( "CurrentUserID", mCurrent_user_id);
                        startActivity(chat_Intent);
                    }
                } );

                holder.mView.setOnLongClickListener( new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        Intent profile_Intent = new Intent(getContext(),ProfileActivity.class);
                        profile_Intent.putExtra("from_user_id",list_item_id);
                        startActivity(profile_Intent);
                        return true;
                    }
                } );

            }

            @NonNull
            @Override
            public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.single_user, parent, false);
                return new FriendsViewHolder(view);

            }
        };

        mDatabase.addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getChildrenCount()==0){
                    friends_listview.setVisibility( View.GONE );
                    empty_view.setVisibility( View.VISIBLE );
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        } );

        mFireRecyclerAdapter.startListening();
        friends_listview.setAdapter(mFireRecyclerAdapter);

    }

    @Override
    public void onStop() {
        super.onStop();
        mFireRecyclerAdapter.stopListening();
        
    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder{

        private View mView;
        private TextView dateText;
        private TextView friName;
        private CircleImageView friImage;
        private ImageView greenDot;

        public FriendsViewHolder(View itemView) {
            super(itemView);
            mView=itemView;
        }

        public void setDate(String date) {
            dateText=(TextView)mView.findViewById( R.id.model_status );
            dateText.setText(date);
        }

        public void setName(String fri_name) {
            friName=(TextView)mView.findViewById( R.id.model_displayname )  ;
            friName.setText(fri_name);
        }

        public void setImage(String fri_img_string) {
            friImage=(CircleImageView) mView.findViewById( R.id.model_image );
            Picasso.get().load(fri_img_string).placeholder(R.drawable.defaultpic).into(friImage);
        }

        public void setOnline(Boolean fri_online) {
            if(fri_online==true) {
                greenDot = (ImageView) mView.findViewById( R.id.greendot );
                greenDot.setVisibility( View.VISIBLE );
            }
        }
    }

}

