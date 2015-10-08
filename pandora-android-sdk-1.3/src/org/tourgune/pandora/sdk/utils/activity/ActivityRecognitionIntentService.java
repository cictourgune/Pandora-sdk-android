package org.tourgune.pandora.sdk.utils.activity;

import org.tourgune.pandora.Configuration;
import org.tourgune.pandora.sdk.utils.bus.AndroidEventBus;
import org.tourgune.pandora.sdk.utils.bus.AndroidEventMessage;

import android.app.IntentService;
import android.content.Intent;

import com.google.android.gms.location.ActivityRecognitionResult;

public class ActivityRecognitionIntentService extends IntentService{
	
    public ActivityRecognitionIntentService(){
		super("ActivityRecognitionService");
	}
	
    /** Called when a new activity detection update is available.
     * 
     */
    @Override
    protected void onHandleIntent(Intent intent){
    	if(ActivityRecognitionResult.hasResult(intent)){
    		
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            // notify   
            AndroidEventBus.getInstance().getBus().post(new AndroidEventMessage(Configuration.NEW_ACTIVITY, result)); 
            
        }
    }
}
