package com.paulgeorge.ek;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gcm.GCMRegistrar;

public class EyeKeeperActivity extends Activity {

	public final static String AUTH = "authentication";
	private String phoneNumber = "";
	private String deviceId = "";


	@Override
	public void onCreate( Bundle savedInstanceState ) {
		super.onCreate(savedInstanceState);
		GCMRegistrar.checkDevice(this);
		GCMRegistrar.checkManifest(this);
		if ( android.os.Build.VERSION.SDK_INT > 9 ) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}

		final String regId = GCMRegistrar.getRegistrationId(this);
		if ( regId.equals("") ) {
			GCMRegistrar.register(this, GCMIntentService.PROJECT_ID);
		}
		else {
			Log.v("EyeKeeperActivity", "Already registered");
		}
		setContentView(R.layout.main);
	}


	/****************************************************************************
	 * 
	 * @param view
	 ****************************************************************************/
	public void sendLocation( View view ) {
		Log.w("EyeKeeperActivity", "Start sendLocation");
		String phNum = LocationServices.getPhoneNumber(this.getApplicationContext());
		Location loc = LocationServices.getLocation(this.getApplicationContext());
		String theXml = LocationServices.convertLocationToXml(loc);
		String resp = LocationServices.sendLocationToServer(phNum, LocationServices.getDeviceId(this), theXml);
		Toast.makeText(this, resp, Toast.LENGTH_LONG).show();
		
	}


	/**************************************************************************
	 * 
	 * @param view
	 **************************************************************************/
	public void register( View view ) {
		Log.w("EyeKeeperActivity", "Start registration process");

		Intent intent = new Intent("com.google.android.c2dm.intent.REGISTER");
		intent.putExtra("app", PendingIntent.getBroadcast(this, 0, new Intent(), 0));

		// Sender currently not used
		intent.putExtra("sender", GCMIntentService.PROJECT_ID);
		startService(intent);
	}


	/*******************************************************************************
	 * 
	 * @param view
	 *******************************************************************************/
	public void showRegistrationId( View view ) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

		String regId = prefs.getString(AUTH, "n/a");

		Toast.makeText(this, regId, Toast.LENGTH_LONG).show();
		Log.d("EyeKeeperActivity RegId", regId);

	}

}