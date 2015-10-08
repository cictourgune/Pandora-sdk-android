package org.tourgune.pandora.sdk.utils;

import org.tourgune.pandora.Configuration;
import org.tourgune.pandora.sdk.utils.bus.AndroidEventBus;
import org.tourgune.pandora.sdk.utils.bus.AndroidEventMessage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class ConnectionChangeReceiver extends BroadcastReceiver
{
  @Override
  public void onReceive( Context context, Intent intent )
  {
    ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService( Context.CONNECTIVITY_SERVICE );
    NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
    
    
    if(activeNetInfo != null && activeNetInfo.isConnected()){
    	Log.i("pandora","CONNECTION!");
    	AndroidEventBus.getInstance().getBus().post(new AndroidEventMessage(Configuration.CONNECTION, "")); 
    }else{
    	Log.i("pandora","NO CONNECTION!!!");
    	AndroidEventBus.getInstance().getBus().post(new AndroidEventMessage(Configuration.NO_CONNECTION, "")); 
    }
    
  }
}