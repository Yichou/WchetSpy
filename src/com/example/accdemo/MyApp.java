package com.example.accdemo;

import com.edroid.common.utils.WorkThread;

import android.app.Application;
import android.content.Intent;
import android.view.accessibility.AccessibilityManager;
import android.widget.Toast;

public class MyApp extends Application implements AccessibilityManager.AccessibilityStateChangeListener{

	@Override
	public void onCreate() {
		super.onCreate();

		start();
		
//		System.out.println("totalMemory=" + Runtime.getRuntime().totalMemory());
//		System.out.println("freeMemory=" + Runtime.getRuntime().freeMemory());
//		System.out.println("maxMemory=" + Runtime.getRuntime().maxMemory());
		
	}
	
	@Override
	public void onAccessibilityStateChanged(boolean b) {
		if (b) {
			// Intent intent = new Intent();
			// intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
			// intent.setClassName(MM, LauncherUI);
			// startActivity(intent);
		} else {
			try {
				// 打开系统设置中辅助功能
				Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
				startActivity(intent);
				Toast.makeText(MyApp.this, "找到检测被删好友辅助，然后开启服务即可", Toast.LENGTH_LONG).show();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	void start() {
		System.out.println("============");
		
		startService(new Intent(this, DeamonService.class).setAction("init"));

		AccessibilityManager accessibilityManager = (AccessibilityManager) getSystemService(ACCESSIBILITY_SERVICE);

		accessibilityManager
				.addAccessibilityStateChangeListener(this);

		if (!accessibilityManager.isEnabled()) {
			try {
				// 打开系统设置中辅助功能
				Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
				startActivity(intent);
				Toast.makeText(MyApp.this, "找到检测被删好友辅助，然后开启服务即可", Toast.LENGTH_LONG).show();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("accessibility is enable");
			// Intent intent = new Intent();
			// intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
			// intent.setClassName(MM, LauncherUI);
			// startActivity(intent);
		}
		
//		new Thread(new Runnable() {
//			
//			@Override
//			public void run() {
//				SystemClock.sleep(3000);
//				Process.killProcess(Process.myPid());
//			}
//		}).start();
	}
}
