package com.droid.nav.socialtag;

import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.droid.nav.socialtag.databinding.ActivityMainBinding;

public class HelloActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityMainBinding  mainBinding= DataBindingUtil.setContentView(HelloActivity.this,R.layout.activity_main);
//
        mainBinding. etSocial.setMentionEnabled(true);
        mainBinding.etSocial.setHashtagEnabled(true);
        mainBinding.etSocial.setMentionColor(ContextCompat.getColor(HelloActivity.this, R.color.colorAccent));

    }
}
