package com.jackpf.halo4servicerecord2;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.jackpf.halo4servicerecord2.R;

public class PreferencesActivity extends PreferenceActivity
{
	public final static String KEY_SPARTANQUALITY	= "pref_spartanQuality",
							   KEY_EMAILADDRESS		= "pref_email",
							   KEY_PASSWORD			= "pref_password";
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        addPreferencesFromResource(R.xml.preferences);
    }
}