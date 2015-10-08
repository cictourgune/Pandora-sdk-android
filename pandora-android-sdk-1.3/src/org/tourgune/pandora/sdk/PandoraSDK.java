package org.tourgune.pandora.sdk;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.service.RangedBeacon;
import org.tourgune.pandora.Configuration;
import org.tourgune.pandora.MyGCMIntentService;
import org.tourgune.pandora.ServerUtilities;
import org.tourgune.pandora.sdk.service.GeoService;
import org.tourgune.pandora.sdk.utils.GCMProcessor;
import org.tourgune.pandora.sdk.utils.Log;
import org.tourgune.pandora.sdk.utils.Utils;
import org.tourgune.pandora.sdk.utils.activity.ActivityRecognitionManager;
import org.tourgune.pandora.sdk.utils.bus.AndroidEventBus;
import org.tourgune.pandora.sdk.utils.bus.AndroidEventMessage;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;

import com.google.android.gcm.GCMRegistrar;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.squareup.otto.Subscribe;
 

public class PandoraSDK  implements BeaconConsumer{
	 
	public static String dToken; 
	public static Context contexto; 
	public static String rootURL;  
	public static String webContext;  
 
	private GCMProcessor processor;
	private AsyncTask<Void, Void, Void> mRegisterTask;
	public static HashMap extras = new HashMap();
	
	private ActivityRecognitionManager manager;
	
	private Boolean iBeacon = false;
	
	private BeaconManager beaconManager; 
	
    private HashMap now = new HashMap();
    private HashMap then = new HashMap();
    private HashMap counter = new HashMap(); //para contar cuantas veces se detecta en exit

    private HashMap ibeaconsToSend = new HashMap();
      
	
	//********* SINGLETON PATTERN ***********************************
	 private static PandoraSDK instance = null; 
	 
	 private static boolean startFlag = false;
	 
	 protected PandoraSDK() { 
	 }
	 
	 public static PandoraSDK getInstance(Context context, GCMProcessor processor, Boolean iBeacon) throws NameNotFoundException {
	      if(instance == null) {
	         instance = new PandoraSDK(context, processor, iBeacon);
	         startFlag = true; //hay que llamar al método start
	      }else{
	    	  startFlag = false; //NO hay que llamar al método start, ya se inicializó anteriormente
	      }
	      return instance;
	 }

	//TODO se puede indicar otro SENDER ID? Si es así, ponerlo como parámetro y que el developer lo obtenga del API Google GCM
	private PandoraSDK(Context context, GCMProcessor processor, Boolean iBeacon) throws NameNotFoundException{ 
	 
		contexto = context;
		 
		if(processor==null){
			throw new RuntimeException();
		} 

		this.iBeacon = iBeacon;
		this.processor = processor;
		this.processor.setContext(context);
	 
		MyGCMIntentService.gcmProcessor = this.processor;
		
		initConfiguration(context);  
		
		AndroidEventBus.getInstance().getBus().register(this);
		
	}

	private void initConfiguration(Context context) throws NameNotFoundException{

		ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
		Bundle bundle = ai.metaData;
		Integer port = bundle.getInt("pandora.port");
		String domain = bundle.getString("pandora.domain"); 
		dToken = bundle.getString("pandora.key");  
		rootURL= "http://"+domain+":"+port+"/pandora"; 
		if(webContext!=null){
			rootURL = "http://"+domain+":"+port+"/"+webContext; 
		}
		this.contexto = context;  
	} 


	@Subscribe 
	public void messageAvailable(AndroidEventMessage m) {
		
		 if(m.getId().equals(Configuration.NO_CONNECTION)){
			 //detener geo e ibacon services
			 Log.i("pandora","stoping services because NO CONNECTION");
			 stopServices(contexto);
		 }else if(m.getId().equals(Configuration.CONNECTION)){
			//activar geo e ibacon services
			 Log.i("pandora","restarting services because CONNECTION");
			 startServices(contexto, this.iBeacon, true);
		 }else if(m.getId().equals(Configuration.NEW_ACTIVITY) && GeoService.receivedFix>2){ 
			
			 ActivityRecognitionResult result = (ActivityRecognitionResult) m.getContent(); 
			 String activity = parseActivity(result.getMostProbableActivity());
			 Log.i("pandora","new activity: "+activity);  
			 if(result.getMostProbableActivity().getType()==DetectedActivity.STILL && result.getMostProbableActivity().getConfidence()>80){
				 Log.i("pandora","stoping services because STILL");
				 stopServices(contexto);
			 }else {
				 Log.i("pandora","restarting services because NOT STILL");	
				 startServices(contexto, this.iBeacon, true);
			 }
		 } 
	}

	
	private String parseActivity(DetectedActivity activity){
    	String log;
    	
    	switch(activity.getType()){
    	case DetectedActivity.IN_VEHICLE:
    		log = "In car.";
    		break;
    	case DetectedActivity.ON_BICYCLE:
    		log = "On bike.";
    		break;
    	case DetectedActivity.ON_FOOT:
    		log = "On foot.";
    		break;
    	case DetectedActivity.STILL:
    		log = "Standing still.";
    		break;
    	case DetectedActivity.TILTING:
    		log = "Tilting.";
    		break;
		default:
			log = "Unknown.";
    	}
    	
    	return activity.getConfidence() + "%:\t\t\t" + log;
    }

	
	public void start() {  
		 
		 if(startFlag) { //If it is already an instance available, do not restart Pandora
			 
			Log.i("pandora","....starting Pandora");
			
			Utils.deleteUserFromServer(contexto);  
			
			if(this.processor!=null){
				startGCM();
			} 
			startServices(contexto, this.iBeacon, false); 
			
			//start service activity
//			Log.i("pandora","...starting activity recognition");
//			manager = new ActivityRecognitionManager(contexto);
//			manager.start(); 
			
			
			if(iBeacon==true){ 
				Log.i("pandora","....starting pandora iBeacon Service ");
				beaconManager = BeaconManager.getInstanceForApplication(contexto); 
				beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
				RangedBeacon.setSampleExpirationMilliseconds(1000); //tiempo en caché del dato del beacon, p.ej. distancia
				
				beaconManager.setForegroundBetweenScanPeriod(1000);  
				beaconManager.setForegroundScanPeriod(5000); 
				beaconManager.setBackgroundBetweenScanPeriod(1000);  
				beaconManager.setBackgroundScanPeriod(5000); 
			
				beaconManager.bind(instance);
			}
			
		} 

	}
	
	

	private void startServices(Context context, Boolean iBeacon, Boolean restarting) { 
		
		if(!isServiceRunning(GeoService.class)){
			Log.i("pandora","....starting Services GEO");
			GeoService.receivedFix = 0;
			//Iniciamos el servicio de tracking
			final Intent intent = new Intent(context, org.tourgune.pandora.sdk.service.GeoService.class);
			context.startService(intent);	 
		}
		
		
		//------------------------------------- IBEACONS LIBRERIA
		 
		if(iBeacon==true){ 
//			Log.i("pandora","....instance "+contexto.getPackageName());
//			beaconManager = BeaconManager.getInstanceForApplication(contexto); 
//			beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
//			RangedBeacon.setSampleExpirationMilliseconds(1000); //tiempo en caché del dato del beacon, p.ej. distancia
//			
//			beaconManager.setForegroundBetweenScanPeriod(1000);  
//			beaconManager.setForegroundScanPeriod(5000); 
//			beaconManager.setBackgroundBetweenScanPeriod(1000);  
//			beaconManager.setBackgroundScanPeriod(5000); 
//			 
//			Log.i("pandora","....instance "+contexto.getPackageName());
//			beaconManager.bind(instance);
			
			
			if(restarting){
				try {
					beaconManager.startRangingBeaconsInRegion(new Region("ALL", null, null, null));
				} catch (RemoteException e) {
					e.printStackTrace();
				}	
			} 
			
		}
	
		
		 
//		if(!isServiceRunning(IBeaconService.class)){
//			//iBeacon scanning
//			if(iBeacon==true){ 
//				Log.i("pandora","....starting Services iBeacon");
//				//start service
//		    	Intent serviceIntent = new Intent(context, IBeaconService.class); 
//		    	context.startService(serviceIntent);   
//			}
//		}
		
//		if(iBeacon==true){ 
//			try {
//			    beaconManager.startRangingBeaconsInRegion(new Region(null, null, null, null));
//			    Log.i("pandora","....starting Services beaconManager");
//			} catch (RemoteException e) { 
//				e.printStackTrace();
//			}
//		}
		
	 	
//		//Iniciamos el servicio de bateria
//		if(Configuration.BATTERY_MANAGER){ //si está configurado para contemplar la batería
//			final Intent intentBattery = new Intent(context, org.tourgune.pandora.sdk.service.BatteryService.class);
//			context.startService(intentBattery);
//		}
	}
		
	
	public void stop(){
	
		if(this.processor!=null){
			GCMRegistrar.unregister(contexto);  
		} 
		stopServices(contexto);
		Log.i("pandora","...stoping activity recognition");
		if(manager!=null){
			manager.stop();
		} 
		beaconManager.unbind(this);
		//borrar datos de usuario en servidor	
		Utils.deleteUserFromServer(contexto);  
	} 
	 
	private void stopServices(Context context){
		
		if(isServiceRunning(GeoService.class)){
			Log.i("pandora","...stoping Services GEO");  
			// Paramos el servicio de posicionamiento
			String actionName = "org.tourgune.pandora.sdk.intent.action.SERVICE";
			Intent intento = new Intent(actionName);   
			context.stopService(intento);
		}
		 
		if(iBeacon==true){ 
			//beaconManager.unbind(this);
			try {
				beaconManager.stopRangingBeaconsInRegion(new Region("ALL", null, null, null));
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		

//		if(Configuration.BATTERY_MANAGER){ //si está configurado para contemplar la batería
//			String actionName2 = "org.tourgune.pandora.sdk.intent.action.BATTERY";
//			Intent intento2 = new Intent(actionName2);
//			boolean prueba2 = context.stopService(intento2);
//		}	
	}
	
	private boolean isServiceRunning(Class<?> serviceClass) {
	    ActivityManager manager = (ActivityManager) contexto.getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if (serviceClass.getName().equals(service.service.getClassName())) {
	            return true;
	        }
	    }
	    return false;
	}
	
	private void startGCM(){   
	 
		Log.i("GCM","....starting GCM");
        // Make sure the device has the proper dependencies.
        GCMRegistrar.checkDevice(this.contexto);
        // Make sure the manifest was properly set - comment out this line
        // while developing the app, then uncomment it when it's ready.
        GCMRegistrar.checkManifest(this.contexto);
    
        final String regId = GCMRegistrar.getRegistrationId(this.contexto);
        Log.i("GCM", "registration ID: "+regId);
        
        if (regId.equals("")) { 
            // Automatically registers application on startup. 
        	try{
        		  GCMRegistrar.register(this.contexto, Configuration.SENDER_ID);
        	}catch(Exception e){   
        		e.printStackTrace();
        	}
        	
        	if (GCMRegistrar.isRegisteredOnServer(this.contexto)) {
        		  Log.i("pandora", "Registered successfully in GCM"); 
        		  //generar id usuario si no estaba generada u obtener la generada anteriormente
        		  Utils.getOrGenerateUserID(contexto); 
        	} 
        } else {
        	 
            // Device is already registered on GCM, needs to check if it is
            // registered on our server as well.
            if (GCMRegistrar.isRegisteredOnServer(this.contexto)) { 
                // Skips registration.
            	 Log.i("pandora", "Already Registered in GCM"); 
            	 Utils.registrationID = regId; 
            	 //no generar userid y utilizar el anterior que está almacenado en shared preferences 
            } else {
                // Try to register again, but not in the UI thread.
                // It's also necessary to cancel the thread onDestroy(),
                // hence the use of AsyncTask instead of a raw thread.
                final Context context = this.contexto;
                mRegisterTask = new AsyncTask<Void, Void, Void>() {

                    @Override
                    protected Void doInBackground(Void... params) {
                        boolean registered = ServerUtilities.register(context, regId); 
                        Log.i("GCM", "Registered-----");  
                        // At this point all attempts to register with the app
                        // server failed, so we need to unregister the device
                        // from GCM - the app will try to register again when
                        // it is restarted. Note that GCM will send an
                        // unregistered callback upon completion, but
                        // GCMIntentService.onUnregistered() will ignore it.
                        if (!registered) {
                            GCMRegistrar.unregister(context);
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void result) {
                        mRegisterTask = null;
                    }  
                };
                mRegisterTask.execute(null, null, null);
            }
        }   
	}
	
	public void addExtraData(Object key, Object value){
		extras.put(key, value);
	}
	
	public Object getExtraData(Object key){
		return extras.get(key);
	}
	
	public void removeExtraData(Object key){
		extras.remove(key);
	}
	
	public void clearExtraData(Object key){
		extras.clear();
	}

	
	
	@Override
	public boolean bindService(Intent intent, ServiceConnection conn, int mode) {
		 return contexto.bindService(intent, conn, mode);
	}

	@Override
	public Context getApplicationContext() {
		 return contexto;
	}

	@Override
	public void onBeaconServiceConnect() {
		
		
		
		Log.i("pandora","...onBeaconServiceConnect");
		
		


		
		//-------------- rango --------------------------
		
		beaconManager.setRangeNotifier(new RangeNotifier() {
		        @Override 
		        public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) { 
		            if (beacons.size() > 0) {  
	    				//crear lista now
	    				for (Beacon rangedBeacon : beacons) {
	    					String beaconId = String.valueOf(rangedBeacon.getId1())+String.valueOf(rangedBeacon.getId2())+String.valueOf(rangedBeacon.getId3());
	    					Log.i("pandora","....detected iBeacon "+beaconId);
	    					now.put(beaconId, beaconId);
	    				}
	    				
	    				//entradas
	    				for (Beacon rangedBeacon : beacons) {
	    					String beaconId = String.valueOf(rangedBeacon.getId1())+String.valueOf(rangedBeacon.getId2())+String.valueOf(rangedBeacon.getId3());

	    					if(now.containsKey(beaconId) && !then.containsKey(beaconId)){ //está en now pero no en then = entrada 
	    						Log.i("pandora","Enter: "+beaconId);
	    						ibeaconsToSend.put(beaconId, beaconId); 
	    						//Enviar directamente toda la lista de los beacon descubiertos, sería como obtener todas las áreas dado un punto
	    	    				Utils.sendiBeaconsToServer(beacons, PandoraSDK.contexto); 
	    					}
	    				}
	    				//salidas
	    				 Iterator it = then.entrySet().iterator();
	    				 while (it.hasNext()) {
	    				        Map.Entry pairs = (Map.Entry)it.next();
	    				        if(!now.containsKey(pairs.getKey())){ 
	    				        	Log.i("pandora","Exit: "+pairs.getKey().toString());
	    				        	ibeaconsToSend.remove(pairs.getKey().toString()); 
	    				        	//Enviar directamente toda la lista de los beacon descubiertos, sería como obtener todas las áreas dado un punto
	    		    				Utils.sendiBeaconsToServer(beacons, PandoraSDK.contexto);
	    				        }
	    				 }
	    				     
	    				//pasar de now a then    					
						then = new HashMap(now);
						now.clear();	 
		            }
		        }
		});
		
		try {
		    beaconManager.startRangingBeaconsInRegion(new Region("ALL", null, null, null));
		} catch (RemoteException e) { 
			e.printStackTrace();
		}
		
		
		
		
		
	}

	@Override
	public void unbindService(ServiceConnection conn) {
		contexto.unbindService(conn);
		
	} 
	 
    
}
