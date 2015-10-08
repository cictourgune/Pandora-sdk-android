package org.tourgune.pandora;

 
public final class Configuration {

    /**
     * Google API project id registered to use GCM.
     */
    public static final String SENDER_ID = "821810937323"; //lab 
    
    /** 
     * Tag used on log messages.
     */
    public  static final String TAG = "pandora";
    
    public static final boolean BATTERY_MANAGER = false; //activar o no el control de la batería
    public static int battery = 15; //nivel en el que para
    
    //GPS
    public static int MIN_TIME = 20000;    //sg
	public static int MIN_DIST = 0; //m TODO indicar alguna distancia?
	
	//Optimization options active
	public static int NO_CONNECTION = 0;
	public static int CONNECTION = 1;
	public static int NO_MOVEMENT = 2;
	public static int NEW_ACTIVITY = 3;
	
	public static int STILL = 4;
    
}
