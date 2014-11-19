package com.paulgeorge.ek;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.Log;

public class LocationServices {

	private static String phoneNumber = "";
	private static String deviceId = "";
	
	/**************************************************************************
	 * 
	 * @return
	 **************************************************************************/
	public static Location getLocation(Context ctx) {
		// Acquire a reference to the system Location Manager
		LocationManager locationManager = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
		
		// Use LocationManager.NETWORK_PROVIDER Or use LocationManager.GPS_PROVIDER
		return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
	}
	
	
	/*******************************************************************************
	 * 
	 * @return
	 *******************************************************************************/
	public static String convertLocationToXml(Location loc) {
		return "<?xml version='1.0' encoding='ISO-8859-1'?>" +
				"<LOCATIONS><LOCATION><LAT>" + loc.getLatitude() + "</LAT><LNG>" + loc.getLongitude() + "</LNG>" +
				"<TIMESTAMP>" + loc.getTime() + "</TIMESTAMP><SPEED>0</SPEED>" +
				"<DIRECTION>0</DIRECTION></LOCATION></LOCATIONS>";
	}
	
	
	/****************************************************
	 * 
	 * @return
	 ****************************************************/
	public static String getPhoneNumber(Context ctx) {
		if ( phoneNumber.length() == 0 ) {
			TelephonyManager telephonyManager =	(TelephonyManager)ctx.getSystemService(Context.TELEPHONY_SERVICE);
			phoneNumber = telephonyManager.getLine1Number();
		}
		return phoneNumber;
	}
	
	
	/*****************************************************
	 * 
	 * @return
	 ****************************************************/
	public static String getDeviceId(Context ctx) {
		if ( deviceId.length() == 0 ) {
			deviceId = Secure.getString(ctx.getContentResolver(), Secure.ANDROID_ID);
		}
		return deviceId;
	}


	/*******************************************************************************************
	 * 
	 * @param phoneNumber
	 * @param deviceId
	 * @param locationXml
	 * 
	 *******************************************************************************************/
	public static String sendLocationToServer( String phoneNumber, String deviceId, String locationXml ) {
		Log.d("sendLocationToServer", "Sending registration ID to my application server");
		HttpClient client = new DefaultHttpClient();
		String clientUrl = "blooming-ice-3129.herokuapp.com";
		String responseText = "";
		
    	Log.i("sendLocationToServer", "Sending location info to " + clientUrl );

		HttpPost post = new HttpPost("http://" + clientUrl + "/reportLocation");
		try {
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

			nameValuePairs.add( new BasicNameValuePair( "deviceid", deviceId ) );
			nameValuePairs.add( new BasicNameValuePair( "locationxml", locationXml) );
			nameValuePairs.add( new BasicNameValuePair( "phn", phoneNumber ) );
			post.setEntity( new UrlEncodedFormEntity( nameValuePairs ) );
			
			HttpParams params = new BasicHttpParams();
			params.setParameter("deviceId", deviceId);
			params.setParameter("locationxml", locationXml);

			post.setParams(params);
			
			HttpResponse response = client.execute(post);
			
			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

			String line = "";
			while ((line = rd.readLine()) != null) {
				responseText += line; 
				Log.i("HttpResponse", line);
			}
		}
		catch (IOException e) {
			e.printStackTrace();
			Log.e("sendLocationToServer", "Error! " + e.getMessage());
		}
		
		return responseText;
	}
}
