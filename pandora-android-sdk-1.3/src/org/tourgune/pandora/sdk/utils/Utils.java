package org.tourgune.pandora.sdk.utils;

 

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import org.altbeacon.beacon.Beacon;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.tourgune.pandora.Configuration;
import org.tourgune.pandora.sdk.PandoraSDK;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
 


public class Utils {
	
	public static String registrationID;  
	
	public static String getOrGenerateUserID(Context context){
		
		SharedPreferences prefs = context.getSharedPreferences(Configuration.TAG, Context.MODE_WORLD_WRITEABLE);
		String userID = prefs.getString("userID", "null"); 
		
		if(userID=="null"){
			userID = UUID.randomUUID().toString();
			SharedPreferences.Editor editor = prefs.edit();
			editor.putString("userID", userID);
			editor.commit();
			Log.i("pandora","...generating user uuid");
		}else{
			Log.i("pandora","...reusing user uuid");
		}
		 
		return userID; 
	}
	
	
	public static String getDeviceID(TelephonyManager phonyManager){
		 
		String id = phonyManager.getDeviceId();
		if (id == null){
			id = "not available";
		}
		int phoneType = phonyManager.getPhoneType();
		switch(phoneType){
		case TelephonyManager.PHONE_TYPE_NONE:
			return id;
		 
		case TelephonyManager.PHONE_TYPE_GSM:
			return id;
		 
		case TelephonyManager.PHONE_TYPE_CDMA:
			return id; 
		default:
			return "UNKNOWN: ID=" + id;
		} 
	}
	
	public static void deleteUserFromServer(Context contexto){
		
		Log.i("pandora","removing user from server KB");
		
		SharedPreferences prefs = contexto.getSharedPreferences(Configuration.TAG,Context.MODE_PRIVATE);
		String userId = prefs.getString("userID", "");
		
		String url = PandoraSDK.rootURL + "/open/sdk/user" +"?userid="+userId+"&key=" + PandoraSDK.dToken; 
		
		JSONObject object = new JSONObject();
		  try {
				
			    object.put("id", userId);
				String result = callRESTService(object.toString(), url, RESTServiceRequest.DELETE);  				 
			   
		  } catch (JSONException e) {
		    e.printStackTrace();
		  }  
	}
	
	
	public static void sendiBeaconsToServer(Collection<Beacon> beacons, Context contexto){ 
		
		String url = PandoraSDK.rootURL + "/open/sdk/user/ibeacon" + "?key=" + PandoraSDK.dToken; 
		 
		JSONArray listaIBeaconJSON = new JSONArray();
		
		for (Beacon rangedBeacon : beacons) {
			String beaconId = String.valueOf(rangedBeacon.getId1())+String.valueOf(rangedBeacon.getId2())+String.valueOf(rangedBeacon.getId3());
			listaIBeaconJSON.put(beaconId); 
		}
		//MANDAR ID DEL USER TAMBIÉN Y EL GCM, CON EL LISTADO DE IBEACON detectados
		JSONObject object = new JSONObject();
		  try { 
			    object.put("id", Utils.getOrGenerateUserID(contexto)); 
			    object.put("ibeacon_id_list", listaIBeaconJSON);
			     
		    	//Hasta que no haya gcmid no se manda nada
		    	 if(Utils.registrationID!=null){
				    	object.put("gcmId", Utils.registrationID); 
						callRESTService(object.toString(), url, RESTServiceRequest.POST);  
				    }					  
			   
			   
		  } catch (JSONException e) {
		    e.printStackTrace();
		  }

	}
	
	
	public static void sendPosToServer(Location loc, Context contexto){ 
//		Log.i("GCM","....sendPosToServer");
		String url = PandoraSDK.rootURL + "/open/sdk/user" + "?key=" + PandoraSDK.dToken; 
	 
		JSONObject object = new JSONObject();
		  try {
			  	object.put("id", Utils.getOrGenerateUserID(contexto)); 
			    object.put("latitude", loc.getLatitude()); 
			    object.put("longitude", loc.getLongitude()); 
			    
			    JSONObject extras = new JSONObject();
			    
			    Iterator it = PandoraSDK.extras.entrySet().iterator();
			    while (it.hasNext()) {
			        Map.Entry pairs = (Map.Entry)it.next(); 
			        extras.put(pairs.getKey().toString(),  pairs.getValue());
			    }
			    
			    object.put("extras", extras); 
			    
		    	//Hasta que no haya gcmid no se manda nada
		    	 if(Utils.registrationID!=null){
				    	object.put("gcmId", Utils.registrationID); 
						callRESTService(object.toString(), url, RESTServiceRequest.POST);  
				    }					  
			   
			   
		  } catch (JSONException e) {
		    e.printStackTrace();
		  }  
	}

	
	public static String callRESTService(String json, String url, Integer type) {

		Log.i("pandora", "sending data to pandora server: "+json);
		RESTServiceRequest async = new RESTServiceRequest(json, url, type);
		AsyncTask<Void, Void, String> result = async.execute();
		try {
			return result.get();

		} catch (Exception e) {
			e.printStackTrace();
			return "-1";
		}

	}
	
	
	public static boolean isNetworkAvailable(Context context) {
	    ConnectivityManager connectivityManager 
	          = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

}
