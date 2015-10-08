package org.tourgune.pandora.sdk.utils.activity;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.ActivityRecognitionClient;
 

public class ActivityRecognitionManager implements ConnectionCallbacks, OnConnectionFailedListener{
	 
	private boolean connecting;
	private boolean requestUpdates;
	
	private Context context;
	private PendingIntent recognitionIntent; 
	private ActivityRecognitionClient recognitionClient;
	
	public static final int FIX_PLAY_SERVICES = 1;
	private final int DETECTION_INTERVAL = 1000 * 1;	// sg
	
	public ActivityRecognitionManager(Context context){
		this.context = context;
		connecting = false;
        recognitionClient = new ActivityRecognitionClient(context, this, this);  
        
        Intent intent = new Intent(context, ActivityRecognitionIntentService.class);
        recognitionIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}
	
	/**
     * Checks that Google Play services is available.
     *
     * @return true if Google Play services is available, otherwise false
     */
    public boolean servicesAvailable(){
    	
        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);

        // If Google Play services is available
        if(resultCode == ConnectionResult.SUCCESS){ 
            // Continue
            return true; 
        }else{	// Google Play services is not available! 
            return false;
        }
    }
	
    /**
     * Starts the Activity Recognition client and requests updates
     */
    public void start(){
    	requestUpdates = true;
    	connect();
    }
    
    /**
     * Stops requesting updates
     */
    public void stop(){
    	requestUpdates = false;
    	 recognitionClient.disconnect();
    }
    
    /**
    * Connects to the Activity Recognition Service
    */
	private void connect(){
        if(servicesAvailable()){	// Check for Google Play services
	        // If a request is not already underway
	        if(!connecting){ 
	            // Indicate that a request is in progress
	            connecting = true;
	            // Request a connection to Location Services
	            recognitionClient.connect();
	        }
        }
    }
	
	/**
	 * Activity Recognition Service connection succeed callback
	 */
    @Override
	public void onConnected(Bundle connectionHint){
    	connecting = false;
    	if(requestUpdates){
    		// send request
    		recognitionClient.requestActivityUpdates(DETECTION_INTERVAL, recognitionIntent);
    	}else{
    		// cancel updates
    		recognitionClient.removeActivityUpdates(recognitionIntent);
    	}
    	recognitionClient.disconnect();
	}
    
    /**
     * Activity Recognition Service disconnection succeed callback
     */
	public void onDisconnected(){
		connecting = false;
	}
    
	/**
	 * Activity Recognition Service connection failed callback
	 */
	@Override
	public void onConnectionFailed(ConnectionResult result){
	 
	}

 
}
