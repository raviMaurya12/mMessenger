package com.example.lenovo.mmessenger;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private android.support.v7.widget.Toolbar mToolbar;
    private ViewPager mViewpager;
    private CustomPagerAdapter mPagerAdapter;
    private TabLayout main_tabs;
    private DatabaseReference mDatabase;
    private DatabaseReference usersdatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        mToolbar=(android.support.v7.widget.Toolbar)findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        Objects.requireNonNull( getSupportActionBar() ).setTitle("mMessenger App");

        mViewpager=(ViewPager)findViewById(R.id.main_tabpager);
        mPagerAdapter=new CustomPagerAdapter(getSupportFragmentManager());
        main_tabs=(TabLayout)findViewById(R.id.main_tabs);
        mViewpager.setAdapter(mPagerAdapter);
        main_tabs.setupWithViewPager(mViewpager);

        if(mAuth.getCurrentUser() !=null) {
            mDatabase = FirebaseDatabase.getInstance().getReference().child( "Users" ).child( mAuth.getCurrentUser().getUid() );
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser==null){
          sendToStart();
        }else{
            mDatabase.child("online").setValue(true);
            mDatabase.child( "last_seen" ).setValue( "online" );
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser!=null) {
            mDatabase.child( "online" ).setValue( false );
            mDatabase.child("last_seen").setValue( ServerValue.TIMESTAMP );
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId()==R.id.main_logout){
            String currentUserID=mAuth.getCurrentUser().getUid();
            usersdatabase=FirebaseDatabase.getInstance().getReference().child("Users");
            usersdatabase.child( currentUserID ).child( "online" ).setValue( false );
            usersdatabase.child( currentUserID ).child( "last_seen" ).setValue( ServerValue.TIMESTAMP);
            FirebaseAuth.getInstance().signOut();   // Firebase
            sendToStart();
        }
        if(item.getItemId()==R.id.main_settings){
            Intent settingsIntent = new Intent(MainActivity.this,AccountSetting.class);
            startActivity(settingsIntent);
        }
        if(item.getItemId()==R.id.main_all){
            Intent usersIntent= new Intent(MainActivity.this,UsersActivity.class);
            startActivity(usersIntent);
        }
        return  true;
    }

    private void sendToStart() {
        Intent WelcomeActivity = new Intent(MainActivity.this,StartActivity.class);
        startActivity(WelcomeActivity);
        finish();
    }
}
