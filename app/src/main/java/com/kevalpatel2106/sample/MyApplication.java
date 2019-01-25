package com.kevalpatel2106.sample;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Marco on 30/01/17.
 */

public class MyApplication extends Application
{
	private SharedPreferences sharedPreferences;
	public final String PREFS_NAME = "BIOMETRIC_PREFERENCES";

	@Override
	public void onCreate()
	{
		super.onCreate();
		sharedPreferences = this.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
	}

	public SharedPreferences getSharedPreferences()
	{
		return sharedPreferences;
	}
}
