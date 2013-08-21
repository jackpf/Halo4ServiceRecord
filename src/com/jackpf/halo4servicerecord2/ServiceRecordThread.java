package com.jackpf.halo4servicerecord2;

import java.io.File;
import java.io.InputStream;
import java.util.Map;

import android.app.IntentService;
import android.content.Intent;

public class ServiceRecordThread extends IntentService
{
	
	public ServiceRecordThread()
	{
		super("ServiceRecordThread");
	}
	
	@Override
    protected void onHandleIntent(Intent workIntent)
    {
		//create the intent
		Intent intent = new Intent(this, MainActivity.class);
		
		//get the message from the intent
        String gamertag = workIntent.getStringExtra(MainActivity.I_GAMERTAG);
        
        //request service record
        ServiceRecord serviceRecord = new ServiceRecord(gamertag);
        InputStream request = serviceRecord.requestServiceRecord();
        ServiceRecordParser parser = new ServiceRecordParser(request);
        
		//get stats
		Map<String, String> stats = parser.getStats();
		
		if(stats != null)
		{
			for(Map.Entry<String, String> entry : stats.entrySet())
				intent.putExtra(entry.getKey(), entry.getValue());
	
	        //get spartan image
			File spartan = serviceRecord.requestSpartan(MainActivity.instance.getCacheDir());
			intent.putExtra("Spartan", (spartan != null) ? spartan.getName() : "");
			
			//get emblem
			File emblem = serviceRecord.requestEmblem(MainActivity.instance.getCacheDir(), stats.get("EmblemImageUrl"));
			intent.putExtra("Emblem", (emblem != null) ? emblem.getName() : "");
		}
		
		//start mainactivity again
		intent.putExtra(MainActivity.I_SERVICERECORD_RECEIVED, true);
		intent.putExtra(MainActivity.I_STATUS_CODE, ServiceRecordParser.statusCode);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
    
}