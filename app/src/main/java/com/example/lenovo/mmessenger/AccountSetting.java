package com.example.lenovo.mmessenger;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

import static android.view.View.GONE;

public class AccountSetting extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private CircleImageView dp;
    private TextView displayname;
    private TextView settings_status;
    private Button change_status_button;
    private Button change_dp_button;
    private static final int GALLERY_PICK=1;
    private StorageReference mStorageRef;
    private StorageReference thumb_storage_reference;
    private ProgressBar mProgressBar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_setting);

        //Button and textFields references
        dp=(CircleImageView)findViewById(R.id.setting_dp);
        displayname=(TextView)findViewById(R.id.setting_displayname) ;
        settings_status=(TextView) findViewById(R.id.setting_status);
        change_status_button = (Button)findViewById(R.id.setting_change_status);
        change_dp_button=(Button)findViewById(R.id.setting_change_dp);
        mProgressBar=(ProgressBar) findViewById( R.id.progressBar );

        //getting uid of the current user
        FirebaseUser current_user=FirebaseAuth.getInstance().getCurrentUser();
        String uid=current_user.getUid();

        //Database reference and pointing to the table
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mDatabase= FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
        mDatabase.keepSynced(true);

        //addValueEventListener to change the table contents
        mProgressBar.setIndeterminate( true );
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final String name = dataSnapshot.child("name").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();
                String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();
                final String status = dataSnapshot.child("status").getValue().toString();


                findViewById(R.id.settings_rel_layout).post( new Runnable() {
                    @Override
                    public void run() {
                        displayname.setText(name);
                        settings_status.setText(status);

                        //Using Picasso library to convert a link into image and loading it
                        Picasso.get().load(image).placeholder( R.drawable.defaultpic ).into(dp);
                        mProgressBar.setVisibility( GONE );
                    }
                } );
                

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                mProgressBar.setVisibility( GONE );
                Toast.makeText( AccountSetting.this, "Error loading the data.Check your Connection!", Toast.LENGTH_SHORT ).show();
            }
        });

        //Click Event on Change Status Button
        change_status_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String status = settings_status.getText().toString();
                Intent status_intent=new Intent(AccountSetting.this,statusActivity.class);
                status_intent.putExtra("status_text",status);
                startActivity(status_intent);
            }
        });

        //Click Event on Change Image Button
        change_dp_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Intent for Opening Gallery
                Intent gallery_intent= new Intent();
                gallery_intent.setType("image/*");
                gallery_intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(gallery_intent,GALLERY_PICK);
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

    //On successful gallery Intent
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==GALLERY_PICK && resultCode==RESULT_OK){

            //Getting URI of the selected image
            Uri imageUri = data.getData();

            //Passing URI of selected image to open Crop Activity using an External Crop-Image-Library
            CropImage.activity(imageUri)
                    .setAspectRatio(1,1)
                    .start(this);


        }

        //Events after cropping the Image
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                //Getting Uid of the current user because we are out of the OnCreate Method.
                FirebaseUser current_user=FirebaseAuth.getInstance().getCurrentUser();
                String uid=current_user.getUid();

                //Getting URI of the cropped Image.
                Uri resultUri = result.getUri();


                //getting Actual file from uri for compression.
                File thumb_file = new File(resultUri.getPath());


                final Bitmap thumb_image = new Compressor(this)
                            .setMaxHeight(200)
                            .setMaxWidth(200)
                            .setQuality(75)
                            .compressToBitmap(thumb_file);


                //Storage reference for Creating an blank jpg image path with name=uid.jpg under profile_images folder
                StorageReference filePath=mStorageRef.child("profile_images").child(uid+".jpg");
                thumb_storage_reference=FirebaseStorage.getInstance().getReference().child("Users").child("thumbs").child(uid+".jpg");

                //Putting the uri of the cropped image into the created blank image path add setting Oncomplete listener
                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){

                            //getting the download link for downloading the profile pic
                            final String download_link=task.getResult().getDownloadUrl().toString();


                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            thumb_image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                            byte[] thumb_image_byte = baos.toByteArray();
                            UploadTask uploadTask = thumb_storage_reference.putBytes(thumb_image_byte);
                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                    if(task.isSuccessful()){
                                        String thumb_download_link=task.getResult().getDownloadUrl().toString();

                                        //Changing the values in the table Users and adding OnComplete Listener
                                        Map update_Hashmap = new HashMap();
                                        update_Hashmap.put("image",download_link);
                                        update_Hashmap.put("thumb_image",thumb_download_link);
                                        mDatabase.updateChildren(update_Hashmap).addOnCompleteListener(new OnCompleteListener() {
                                            @Override
                                            public void onComplete(@NonNull Task task) {
                                                if(task.isSuccessful()){
                                                    mDatabase.notify();
                                                    Toast.makeText(AccountSetting.this,"Uploading Successful!",
                                                            Toast.LENGTH_SHORT).show();
                                                }else{
                                                    Toast.makeText(AccountSetting.this,"Changing Firebase Database Failed!",
                                                            Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });



                                    }else{
                                        Toast.makeText(AccountSetting.this,"Thumbnail Uploading Failed", Toast.LENGTH_SHORT)
                                                .show();
                                    }
                                }
                            });

                        }else{
                            Toast.makeText(AccountSetting.this,"Profile Image Uploading Failed.", Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }
                });
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

    }
}
