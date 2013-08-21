package com.jackpf.halo4servicerecord2;

import java.io.InputStream;
import java.util.Map;

import android.os.AsyncTask;

public class ServiceRecordAsync extends AsyncTask<String, Void, Void>
{

	private Map<Integer, Map<String, String>> recentGames;
	
	@Override
	protected Void doInBackground(String... params)
	{
		ServiceRecord serviceRecord = new ServiceRecord(params[0]);
        InputStream request = serviceRecord.requestRecentGames();
        ServiceRecordParser parser = new ServiceRecordParser(request);
        
        recentGames = parser.getRecentGamesStats();
        
		return null;
	}
	
	@Override
	protected void onPreExecute()
	{
		
	} 
	
	@Override
	protected void onPostExecute(Void result)
	{
		ServiceRecordDisplay.displayRecentGames(recentGames);
	}
}