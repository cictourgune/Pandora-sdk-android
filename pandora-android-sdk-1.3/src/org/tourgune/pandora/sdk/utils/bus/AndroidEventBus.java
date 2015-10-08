package org.tourgune.pandora.sdk.utils.bus;

import android.os.Handler;
import android.os.Looper;

import com.squareup.otto.Bus;

public class AndroidEventBus {

	private AndroidBus bus;
	private static AndroidEventBus instance = null;

	protected AndroidEventBus() {
		bus = new AndroidBus();
	}
	public static AndroidEventBus getInstance() {
		if(instance == null) {
			instance = new AndroidEventBus();
		}
		return instance;
	}
	
	public Bus getBus() {
		return bus;
	} 
	
	
	class AndroidBus extends Bus {
	    private final Handler mainThread = new Handler(Looper.getMainLooper());

	    @Override
	    public void post(final Object event) {
	        if (Looper.myLooper() == Looper.getMainLooper()) {
	            super.post(event);
	        } else {
	            mainThread.post(new Runnable() {
	                @Override
	                public void run() {
	                    post(event);
	                }
	            });
	        }
	    }
	}

}
