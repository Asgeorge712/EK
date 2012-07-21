package com.paulgeorge.ek;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class MessageReceivedActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_result);
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			String message = extras.getString("payload");
			if (message != null && message.length() > 0) {
				Log.i("MessageReceiverActivity.onCreate", message );
				TextView view = (TextView) findViewById(R.id.result);
				view.setText(message);
			}
		}
		super.onCreate(savedInstanceState);
	}
	
	
	/*******************************************************************
	 * 
	 * 
	 * 
	 *******************************************************************/
	private void popMessage( String message ) {
		int duration = Toast.LENGTH_LONG;
		Context context = getApplicationContext();
		Toast toast = Toast.makeText(context, message, duration);
		toast.show();
	}
	
}