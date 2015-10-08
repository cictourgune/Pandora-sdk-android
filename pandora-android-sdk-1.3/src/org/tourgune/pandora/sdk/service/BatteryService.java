package org.tourgune.pandora.sdk.service;

import org.tourgune.pandora.Configuration;
import org.tourgune.pandora.sdk.utils.Utils;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.IBinder;

public class BatteryService extends Service {

//	private final static String TAG = BatteryService.class.getSimpleName();
	private BroadcastReceiver timeTickReceiver;//changeReceiver;
	private boolean registered = false;

	public class LocalBinder extends Binder {
	    BatteryService getService() {
	        return BatteryService.this;
	    }
	}

	private final IBinder mBinder = new LocalBinder();

	@Override
	public void onStart(Intent intent, int startId){
	   

	    IntentFilter filter = new IntentFilter();
	    filter.addAction(Intent.ACTION_BATTERY_CHANGED);     
	    timeTickReceiver = new TimeTickReceiver();

	    this.getApplicationContext().registerReceiver(timeTickReceiver, filter);
	    registered = true;
	}


	@Override
	public void onDestroy(){
	    
	if(registered){

	        this.getApplicationContext().unregisterReceiver(timeTickReceiver);
	    }
	}

	@Override
	public IBinder onBind(Intent intent) { 
	    return mBinder;
	}

	public class TimeTickReceiver extends BroadcastReceiver {

	    @Override
	    public void onReceive(Context context, Intent intent) {

			if (intent == null)
				return;
			if (context == null)
				return;
			
			String action = intent.getAction();
			
			if (action == null)
				return;
			
			if (action.equals(Intent.ACTION_BATTERY_CHANGED) && Configuration.BATTERY_MANAGER) {
				
				int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
			
				if (level < Configuration.battery){
					
					//Utils.stopService(context);//TODO activarlo cuando hay batería?
				}	
 
	        }
	    }
	}
}
