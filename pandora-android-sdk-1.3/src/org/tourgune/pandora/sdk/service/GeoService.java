package org.tourgune.pandora.sdk.service;
 

import org.tourgune.pandora.Configuration;
import org.tourgune.pandora.sdk.utils.Log;
import org.tourgune.pandora.sdk.utils.Utils;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

public class GeoService extends Service implements GooglePlayServicesClient.ConnectionCallbacks,
GooglePlayServicesClient.OnConnectionFailedListener, LocationListener{
	
	private Location currentBestLocation;
	
	private LocationClient locationclient;
	private LocationRequest locationrequest;
	
	private GeoService geoService;
	public static int receivedFix = 0;
	
	private ConnectionCallbacks connectionListener = new ConnectionCallbacks() {

        @Override
        public void onDisconnected() {
            //play services disconnected  
        }

        @Override
        public void onConnected(Bundle arg0) {
            //play services connected
            Log.d("GeoService", "connected!");
            if (locationclient != null && locationclient.isConnected()) {  
    			locationrequest = LocationRequest.create();
    			locationrequest.setInterval(Configuration.MIN_TIME).setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY).setSmallestDisplacement(Configuration.MIN_DIST);
    			locationclient.requestLocationUpdates(locationrequest, geoService); 
    		} 
        }
    };

	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
	}
	
	public void onCreate() { 
		super.onCreate();	
		geoService = this;
		
		int resp = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if (resp == ConnectionResult.SUCCESS) {
			locationclient = new LocationClient(this, connectionListener, this);
			locationclient.connect();
		} else {
			Toast.makeText(this, "Google Play Service Error " + resp,
					Toast.LENGTH_LONG).show(); 
		} 
	}
  
 
	public void onDestroy() {
		super.onDestroy(); 
		locationclient.removeLocationUpdates(this);
		if (locationclient != null)
			locationclient.disconnect();
	}

	public IBinder onBind(Intent arg0) { 
		return null;
	}
	  

	@Override
	public void onLocationChanged(Location location) {
		
		Log.i("pandora","....new location!"); 
		receivedFix++;
		float distancia = 0;
		
		if(currentBestLocation!=null){
			distancia = location.distanceTo(currentBestLocation);
			
			Log.i("pandora","....distance to currentBestLocation: "+distancia); 
		} 
		if(isBetterLocation(location, currentBestLocation)){
			currentBestLocation = location; 
			Utils.sendPosToServer(currentBestLocation, this);  
		}
		
	}

	 
 
	
	/** Determines whether one Location reading is better than the current Location fix
	  * @param location  The new Location that you want to evaluate
	  * @param currentBestLocation  The current Location fix, to which you want to compare the new one
	  */
	protected boolean isBetterLocation(Location location, Location currentBestLocation) {
	    if (currentBestLocation == null) {
	        // A new location is always better than no location
	        return true;
	    }

	    // Check whether the new location fix is newer or older
	    long timeDelta = location.getTime() - currentBestLocation.getTime();
	    boolean isSignificantlyNewer = timeDelta > 60000;//Configuration.MIN_TIME;
	    boolean isSignificantlyOlder = timeDelta < 60000;//-Configuration.MIN_TIME;
	    boolean isNewer = timeDelta > 0;

	    // If it's been more than two minutes since the current location, use the new location
	    // because the user has likely moved
	    if (isSignificantlyNewer) {
	    	Log.i("pandora","....isSignificantlyNewer"); 
	        return true;
	    // If the new location is more than two minutes older, it must be worse
	    } 
//	    else if (isSignificantlyOlder) {
//	    	Log.i("pandora","....isSignificantlyOlder"); 
//	        return false;
//	    }

	    // Check whether the new location fix is more or less accurate
	    int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
	    boolean isLessAccurate = accuracyDelta > 0;
	    boolean isMoreAccurate = accuracyDelta < 0;
	    boolean isSignificantlyLessAccurate = accuracyDelta > 200;

	    // Check if the old and new location are from the same provider
	    boolean isFromSameProvider = isSameProvider(location.getProvider(),
	            currentBestLocation.getProvider());

	    // Determine location quality using a combination of timeliness and accuracy
	    if (isMoreAccurate) {
	    	Log.i("pandora","....isMoreAccurate"); 
	        return true;
	    } else if (isNewer && !isLessAccurate) {
	    	Log.i("pandora","....isNewer && !isLessAccurate"); 
	        return true;
	    } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
	    	Log.i("pandora","....isNewer && !isSignificantlyLessAccurate && isFromSameProvider"); 
	        return true;
	    }
	    return false;
	}
	

	/** Checks whether two providers are the same */
	private boolean isSameProvider(String provider1, String provider2) {
	    if (provider1 == null) {
	      return provider2 == null;
	    }
	    return provider1.equals(provider2);
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) { 
	}

	@Override
	public void onConnected(Bundle arg0) { 
	}

	@Override
	public void onDisconnected() { 
	}
		
}
