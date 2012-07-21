package com.paulgeorge.ek;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class RegistrationResultActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_result);
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			String registrationId = extras.getString("registration_id");
			if (registrationId != null && registrationId.length() > 0) {
				TextView view = (TextView) findViewById(R.id.result);
				view.setText("Yay! Your registration ID is " + registrationId);
			}
		}
		else {
			TextView view = (TextView) findViewById(R.id.result);
			view.setText("Boo! Your registration ID was not returned.... ");
		}

		super.onCreate(savedInstanceState);
	}
}