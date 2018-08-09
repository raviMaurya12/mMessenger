package com.example.lenovo.mmessenger;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;

public class StartActivity extends AppCompatActivity {

    private Button new_reg_button;
    private Button start_reg_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Full screen
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_start);

        new_reg_button=(Button)findViewById(R.id.start_new_reg_button);
        start_reg_button=(Button)findViewById(R.id.start_signin_button) ;
        new_reg_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent signup_intent = new Intent(StartActivity.this,SignupActivity.class);
                startActivity(signup_intent);
                finish();
            }
        });

        start_reg_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginIntent = new Intent(StartActivity.this,login.class);
                startActivity(loginIntent);
            }
        });

    }
}
