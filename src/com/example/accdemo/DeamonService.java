package com.example.accdemo;

import com.edroid.common.utils.Logger;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class DeamonService extends Service {
	static Logger log = Logger.create(DeamonService.class);

	@Override
	public void onCreate() {
		super.onCreate();
		
		log.e("Deamon start");
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		log.i("onStartCommand " + intent);
		
		return super.onStartCommand(intent, flags, startId);
	}
}
