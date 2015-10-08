package org.tourgune.pandora;

 
import java.util.Random;
import java.util.UUID;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.util.EntityUtils;
import org.tourgune.pandora.sdk.PandoraSDK;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import org.tourgune.pandora.sdk.utils.Log;
import com.google.android.gcm.GCMRegistrar;


/**
 * Helper class used to communicate with the demo server.
 */
public final class ServerUtilities {

    private static final int MAX_ATTEMPTS = 5;
    private static final int BACKOFF_MILLI_SECONDS = 2000;
    private static final Random random = new Random();

    /**
     * Register this account/device pair within the server.
     * 
     * @return whether the registration succeeded or not.
     */
    public static boolean register(final Context context, final String regId) {
        Log.i(Configuration.TAG, "registering device on GCM server");
        
    	ApplicationInfo ai=null;
		try {
			ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
		} catch (NameNotFoundException e2) { 
			e2.printStackTrace();
		}
		Bundle bundle = ai.metaData; 
		String domain = bundle.getString("pandora.gcm.domain"); 
		Integer port = bundle.getInt("pandora.gcm.port"); 
        
        String serverUrl = "http://"+domain+":"+port+"/gcm/api/v1/device/"+regId+"?key="+PandoraSDK.dToken; 
         
        long backoff = BACKOFF_MILLI_SECONDS + random.nextInt(1000);
        // Once GCM returns a registration id, we need to register it in the
        // demo server. As the server might be down, we will retry it a couple
        // times.
        for (int i = 1; i <= MAX_ATTEMPTS; i++) {
            Log.d(Configuration.TAG, "Attempt #" + i + " to register");
            try {
 
//                AsyncTask<Void, Void, String> result = asyncPOST.execute();
               

                HttpPost postRequest = new HttpPost(serverUrl);
        		BasicHttpResponse httpResponse = null; 
        		String respStr="";
        		try { 
        			HttpClient httpclient = new DefaultHttpClient(); 
        			httpResponse = (BasicHttpResponse) httpclient.execute(postRequest); 
        			respStr = EntityUtils.toString(httpResponse.getEntity());
        			
        		} catch (Exception e) {
        			Log.e("respuesta catch", e.toString()); 
        		}         		
                GCMRegistrar.setRegisteredOnServer(context, true);
                return true;
            } catch (Exception e) {
                // Here we are simplifying and retrying on any error; in a real
                // application, it should retry only on unrecoverable errors
                // (like HTTP error code 503).
                Log.d(Configuration.TAG, "Failed to register on attempt ");
                if (i == MAX_ATTEMPTS) {
                    break;
                }
                try {
                    Log.d(Configuration.TAG, "Sleeping for " + backoff + " ms before retry");
                    Thread.sleep(backoff);
                } catch (InterruptedException e1) {
                    // Activity finished before we complete - exit.
                    Log.d(Configuration.TAG, "Thread interrupted: abort remaining retries!");
                    Thread.currentThread().interrupt();
                    return false;
                }
                // increase backoff exponentially
                backoff *= 2;
            }
        }

        return false;
    }

    /**
     * Unregister this account/device pair within the server.
     */
    public static void unregister(final Context context, final String regId) {
        Log.i(Configuration.TAG, "unregistering device from GCM server");
        
    	ApplicationInfo ai=null;
		try {
			ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
		} catch (NameNotFoundException e2) { 
			e2.printStackTrace();
		}
		Bundle bundle = ai.metaData; 
		String domain = bundle.getString("pandora.gcm.domain"); 
		Integer port = bundle.getInt("pandora.gcm.port"); 
        
        String serverUrl =  "http://"+domain+":"+port+"/gcm/api/v1/device/"+regId+"?key="+PandoraSDK.dToken; 
      		
        try {
        	HttpDelete deleteRequest = new HttpDelete(serverUrl);
    		BasicHttpResponse httpResponse = null; 
    		String respStr="";
    		try {  
    			HttpClient httpclient = new DefaultHttpClient(); 
    			httpResponse = (BasicHttpResponse) httpclient.execute(deleteRequest); 
    			respStr = EntityUtils.toString(httpResponse.getEntity());
    			
    		} catch (Exception e) {
    			Log.e("respuesta catch", e.toString()); 
    		}        	
            GCMRegistrar.setRegisteredOnServer(context, false); 
 
            
        } catch (Exception e) {
        }
    }
     


}
