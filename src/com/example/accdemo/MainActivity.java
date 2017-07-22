package com.example.accdemo;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

//		MPermissions.requestPermissions(MainActivity.this, REQUECT_CODE_SDCARD,
//				Manifest.permission.WRITE_EXTERNAL_STORAGE);
		
		if(Build.VERSION.SDK_INT >= 23)
			requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
				}, 1);
		
	}
	
	@Override
	protected void onActivityResult(int arg0, int arg1, Intent arg2) {
		// TODO Auto-generated method stub
		super.onActivityResult(arg0, arg1, arg2);
	}

}
