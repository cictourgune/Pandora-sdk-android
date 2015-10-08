package org.tourgune.pandora.sdk.utils;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.os.AsyncTask;
import android.util.Log;

public class RESTServiceRequest extends AsyncTask<Void, Void, String>{
	
	private String json;
	private String url;
	private Integer type;
	
	public static final int POST = 0;
	public static final int PUT = 1;
	public static final int GET = 2;
	public static final int DELETE = 3;
	 
	
	public RESTServiceRequest(String jsonPar, String urlPar, Integer methodType) {
		
		json = jsonPar;
		url = urlPar;
		type = methodType;
	}

	protected String doInBackground(Void... params) {
		StringEntity se = null;
		BasicHttpResponse httpResponse = null;
		String respStr="";
		try {
			
			se = new StringEntity(json, HTTP.UTF_8);
			se.setContentType("application/json");
			HttpClient httpclient = new DefaultHttpClient();
			
			if(type==0){
				HttpPost postRequest = new HttpPost(url); 
				postRequest.setEntity(se); 
				httpResponse = (BasicHttpResponse) httpclient.execute(postRequest);
			}else if(type==1){
				HttpPut putRequest = new HttpPut(url); 
				putRequest.setEntity(se); 
				httpResponse = (BasicHttpResponse) httpclient.execute(putRequest);				
			}else if(type==2){
				HttpGet getRequest = new HttpGet(url);  
				httpResponse = (BasicHttpResponse) httpclient.execute(getRequest);
				
			}else if(type==3){
				HttpDelete deleteRequest = new HttpDelete(url); 
				httpResponse = (BasicHttpResponse) httpclient.execute(deleteRequest); 
			}
			
			respStr = EntityUtils.toString(httpResponse.getEntity());
			
		} catch (Exception e) {
			Log.e("respuesta catch", e.toString());
			return respStr;
		}
		return respStr;
	}



}
