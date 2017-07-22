package com.example.accdemo;

import com.edroid.common.utils.Logger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class DeamonReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Logger.si("DeamonReceiver", "onReceive: " + intent);
		
	}

}
