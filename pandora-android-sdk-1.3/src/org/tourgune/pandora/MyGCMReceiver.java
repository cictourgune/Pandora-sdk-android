package org.tourgune.pandora;

import android.content.Context;

import com.google.android.gcm.GCMBroadcastReceiver;

public class MyGCMReceiver extends GCMBroadcastReceiver { 
    @Override
	protected String getGCMIntentServiceClassName(Context context) { 
		return "org.tourgune.pandora.MyGCMIntentService"; 
	} 
}
