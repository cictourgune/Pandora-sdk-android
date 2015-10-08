package org.tourgune.pandora.sdk.utils;

import android.content.Context;

public abstract class GCMProcessor {
	
	public Context context;
	
	public abstract void processMessage(String data); 

	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}
	
	

}
