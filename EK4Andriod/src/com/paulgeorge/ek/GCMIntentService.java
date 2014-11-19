package com.paulgeorge.ek;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;

/********************************************************************
 * 
 * @author Paul
 * 
 * Google Cloud Messaging Project ID(SENDER_ID): 204301434291
 * API Key: AIzaSyD5kBKPWmrb8WwiwHEZUD125PfPKIiieu8
 ********************************************************************/
public class GCMIntentService extends GCMBaseIntentService {
	protected static final String PROJECT_ID = "204301434291";

	/*****************************************************************************
	 * 
	 * 
	 *****************************************************************************/
	public GCMIntentService() {
		super(PROJECT_ID);
	}


	/*****************************************************************************
	 * 
	 * 
	 *****************************************************************************/
	@Override
	protected void onError( Context arg0, String arg1 ) {
		Log.e("GCMIntentService.onError", "Error!! Message: " + arg1);
	}


	/*****************************************************************************
	 * 
	 * 
	 *****************************************************************************/
	@Override
	protected void onMessage( Context context, Intent intent ) {
		String id = intent.getStringExtra("id");
		String operation = intent.getStringExtra("operation");
		String emailAddress = intent.getStringExtra("emailAddress");
		Log.i("GCMIntentService.onMessage", "id=" + id + ", operation=" + operation + ", emailAddress=" + emailAddress);

		// TODO What to do here??  
		// For now show notification, 
		//   Later need to fire the MessageRecieveActivity to act upon the payload info
		Log.w("GCMIntentService.onMessage", "Received message");
		final String payload = intent.getStringExtra("payload");
		Log.d("GCMIntentService.onMessage", "dmControl: payload = " + payload);
		if ( "sendLocation".equalsIgnoreCase( payload.trim() ) ) {
			//Use Location service to find location
			Log.i("GCMIntentService.onMessage", "A Request to Send Location has been received.");
			
			String phNum =  LocationServices.getPhoneNumber( context );
			Location loc = LocationServices.getLocation( context );
			
			String theXml = LocationServices.convertLocationToXml(loc);
			Log.i("LocationXML", theXml );
			
			//Send back call to web service to tell location info
			String resp = LocationServices.sendLocationToServer(phNum, LocationServices.getDeviceId( this ), theXml );
			
			createMessageNotification(context, resp);
		}
		else {
			// Lets make something visible to show that we received the message
			createMessageNotification(context, payload);
		}
	}


	/*****************************************************************************
	 * 
	 * 
	 *****************************************************************************/
	@Override
	protected void onRegistered( Context context, final String registrationId ) {
		Log.i("GCMIntentService.onReceive", "Registered Device Start.  RegistrationId: " + registrationId);
		
		TelephonyManager telephonyManager =	(TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
		final String phoneNumber = telephonyManager.getLine1Number();
		Log.d("GCMIntentService.onReceive", "Phone number: " + phoneNumber );
		
		final String deviceId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
		Log.d("GCMIntentService.onReceive", "DeviceId: " + deviceId);
		
		createRegistrationNotification(context, registrationId);
		
		new Thread(new Runnable() {
		    public void run() {
				sendRegistrationIdToServer(phoneNumber, deviceId, registrationId);
		    }
		}).start();

		// Also save it in the preference to be able to show it later
		saveRegistrationId(context, registrationId);
		Log.i("GCMIntentService.onReceive", "Registered Device End.");
	}


	/****************************************************************************
	 * 
	 * 
	 ****************************************************************************/
	@Override
	protected void onUnregistered( Context arg0, String arg1 ) {
	}
	
	
	/*******************************************************************************************
	 * 
	 * @param phoneNumber
	 * @param deviceId
	 * @param registrationId
	 * 
	 *******************************************************************************************/
	public void sendRegistrationIdToServer( String phoneNumber, String deviceId, String registrationId ) {
		Log.d("sendRegistrationIdToServer", "Sending registration ID to my application server");
		HttpClient client = new DefaultHttpClient();
		String clientUrl = "blooming-ice-3129.herokuapp.com";
    	Log.i("sendRegistrationIdToServer", "Sending registration info to " + clientUrl );

		// TODO Also must create register action in Play! EyeKeeper application
		//HttpPost post = new HttpPost("http://blooming-ice-3129.herokuapp.com/register");
		HttpPost post = new HttpPost("http://" + clientUrl + "/register");
		try {
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
			// Get the deviceID
			nameValuePairs.add( new BasicNameValuePair( "deviceid", deviceId ) );
			nameValuePairs.add( new BasicNameValuePair( "registrationid", registrationId) );
			nameValuePairs.add( new BasicNameValuePair( "phonenumber", phoneNumber ) );
			post.setEntity( new UrlEncodedFormEntity( nameValuePairs ) );
			
			HttpParams params = new BasicHttpParams();
			params.setParameter("deviceId", deviceId);
			params.setParameter("registrationId", registrationId);
			params.setParameter("phoneNumber", phoneNumber);
			post.setParams(params);
			
			HttpResponse response = client.execute(post);
			
			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

			String line = "";
			while ((line = rd.readLine()) != null) {
				Log.i("HttpResponse", line);
			}
		}
		catch (IOException e) {
			e.printStackTrace();
			Log.e("sendRegistrationIdToServer", "Error! " + e.getMessage());
		}
	}

	
	/*****************************************************************************
	 * 
	 * @param context
	 * @param registrationId
	 * 
	 *************************************************************************/
	private void saveRegistrationId( Context context, String registrationId ) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		Editor edit = prefs.edit();
		edit.putString(EyeKeeperActivity.AUTH, registrationId);
		edit.commit();
	}


	/*************************************************************************
	 * 
	 * @param context
	 * @param registrationId
	 * 
	 *************************************************************************/
	public void createRegistrationNotification( Context context, String registrationId ) {
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		
		Intent intent = new Intent(context, RegistrationResultActivity.class);
		intent.putExtra("registration_id", registrationId);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
		
		Notification notification = new Notification.Builder(context)
        			.setContentTitle("Registration successful")
        			.setContentText("Your registration id is: " + registrationId)
        			.setSmallIcon(R.drawable.ic_launcher)
        			.setAutoCancel(true)
        			.setContentIntent(pendingIntent)
        			.getNotification();

		notificationManager.notify(0, notification);
	}
	

	/*************************************************************************
	 * 
	 * @param context
	 * @param registrationId
	 * 
	 *************************************************************************/
	public void createMessageNotification( Context context, String message ) {
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	
		Notification notification = new Notification.Builder(context)
        			.setContentTitle("Message from the Cloud!")
        			.setContentText(message)
        			.setSmallIcon(R.drawable.ic_launcher)
        			.setAutoCancel(true)
        			.getNotification();

		notificationManager.notify(0, notification);
	}

}
