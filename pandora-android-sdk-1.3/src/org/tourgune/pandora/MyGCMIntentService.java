package org.tourgune.pandora;

import org.tourgune.pandora.sdk.utils.GCMProcessor;
import org.tourgune.pandora.sdk.utils.Utils;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import org.tourgune.pandora.sdk.utils.Log;

import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMRegistrar;

/**
 * {@link IntentService} responsible for handling GCM messages.
 */
public class MyGCMIntentService extends GCMBaseIntentService {

	public static GCMProcessor gcmProcessor;
	
    @SuppressWarnings("hiding")
    private static final String TAG = "GCMIntentService";

    public MyGCMIntentService() {
        super(Configuration.SENDER_ID); 
    }
     
	@Override
    protected void onRegistered(Context context, String registrationId) { 
		Log.i("pandora", "onRegistered");
	    Utils.registrationID =  registrationId;  
	    ServerUtilities.register(context, registrationId);
	}
	
	@Override
	protected void onUnregistered(Context context, String registrationId) { 
		Log.i("pandora", "onUnregistered");
	    if (GCMRegistrar.isRegisteredOnServer(context)) {
	        ServerUtilities.unregister(context, registrationId);
	    } else {
	        // This callback results from the call to unregister made on
	        // ServerUtilities when the registration to the server failed.
	        Log.i("pandora", "Ignoring unregister callback");
	    }
	}
	
	@Override
	protected void onMessage(Context context, Intent intent) {
	    String messageData = intent.getStringExtra("data");  
	    if(messageData!=null){
	    	  this.gcmProcessor.processMessage(messageData) ;
	    } 
	}
	
	@Override
	public void onError(Context context, String errorId) { 
		throw new RuntimeException();
	}
	
	@Override
	protected boolean onRecoverableError(Context context, String errorId) { 
	    return super.onRecoverableError(context, errorId);
	}
 

}
