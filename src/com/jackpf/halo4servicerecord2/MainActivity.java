package com.jackpf.halo4servicerecord2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.jackpf.halo4servicerecord2.R;

public class MainActivity extends Activity
{
	
	public static MainActivity instance;
	private Intent intent;
	private SharedPreferences preferences;
	
	public final static String	I_GAMERTAG					= "GAMERTAG",
								I_SERVICERECORD_RECEIVED	= "SERVICERECORD",
								I_STATUS_CODE				= "STATUSCODE",
								I_CHALLENGES_ACTIVITY		= "CHALLENGES";
	
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        instance = this;
        intent = getIntent();
        
        setContentView(R.layout.activity_main);
        
        preferences = this.getPreferences(Context.MODE_PRIVATE);
        
        String savedGamertag = preferences.getString(getString(R.string.gamertag_preference), "");
        
        EditText gamertagInput = (EditText) findViewById(R.id.gamertag);
        
        if(!savedGamertag.equals(""))
        {
        	gamertagInput.setText(savedGamertag);
        	//remove focus from gamertag input
        	this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        	
        	if(!intent.getBooleanExtra(MainActivity.I_SERVICERECORD_RECEIVED, false) &&
        	   !intent.getBooleanExtra(MainActivity.I_CHALLENGES_ACTIVITY, false))
        		gamertagUpdate();
        }
        
        gamertagInput.setOnEditorActionListener(
        	new OnEditorActionListener()
        	{
        		public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
        		{
	                if(v.getText().length() > 0 && ((event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) || actionId == EditorInfo.IME_ACTION_DONE))
	                    gamertagUpdate();
	                return false;
            	}
        	}
        );
        
        //menu
        ((ImageView) findViewById(R.id.menu)).setOnClickListener(
        	new OnClickListener()
			{
    			@Override
    	        public void onClick(View v)
    			{
    				openOptionsMenu();
    			}
			}
	    );

        if(intent.getBooleanExtra(MainActivity.I_SERVICERECORD_RECEIVED, false))
        {
    		//unloading!
    		Lib.loading(this, true);
    		
    		int statusCode = intent.getIntExtra(MainActivity.I_STATUS_CODE, ServiceRecordParser.PARSE_ERROR_UNKOWN);
    		
    		if(statusCode > ServiceRecordParser.PARSE_OK)
        		Lib.error(this, getString(getResources().getIdentifier("error_" + statusCode, "string", getPackageName())));
    		else
    		{
    			//prompt the user to rate?
    			Lib.ratePrompt(this, preferences);
    			
    			ServiceRecordDisplay.display(this, intent);
    		}
        }
        else if(intent.getBooleanExtra(MainActivity.I_CHALLENGES_ACTIVITY, false))
        {
        	H4Challenges.display(this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
        	case R.id.menu_settings:
        		Intent intent = new Intent(this, PreferencesActivity.class);
        		startActivity(intent);
	        break;
        	case R.id.menu_about:
        		Lib.showMessage(this, "About", getString(R.string.about));
        	break;
        	case R.id.menu_donate:
        		startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Lib.paypalUrl)));
        	break;
        	case R.id.menu_refresh:
        		gamertagUpdate();
        	break;
        	case R.id.menu_search:
                ((ScrollView) findViewById(R.id.scroll_view)).smoothScrollTo(0, 0);
                ((EditText) findViewById(R.id.gamertag)).requestFocus();
        	break;
        	case R.id.menu_challenges:
        		H4Challenges.startActivity();
        	break;
        }
        
        return true;
    }
    
    private void gamertagUpdate()
    {
    	//servicerecord service
    	Intent intent = new Intent(this, ServiceRecordThread.class);
    	
    	//get the gamertag input
    	EditText gamertagInput = (EditText) findViewById(R.id.gamertag);
    	
    	//send the gamertag to the servicerecord intent
    	String gamertag = gamertagInput.getText().toString();
    	intent.putExtra(I_GAMERTAG, gamertag);
    	
    	//save gamertag
    	SharedPreferences.Editor preferencesEditor = preferences.edit();
    	preferencesEditor.putString(getString(R.string.gamertag_preference), gamertag);
    	preferencesEditor.apply();
    	
    	//set status to loading
    	Lib.loading(this, false);
    	
    	//start background service
    	startService(intent);
    }
    
    public static void loadRecentGames()
    {
    	new ServiceRecordAsync().execute(ServiceRecord.instance.getGamertag());
    }
    
}
