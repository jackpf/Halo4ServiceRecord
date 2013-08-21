package com.jackpf.halo4servicerecord2;

import android.content.Intent;
import android.os.AsyncTask;

public class H4ChallengesThread extends AsyncTask<Void, Void, Void>
{
	
	@Override
    protected Void doInBackground(Void... args)
    {
		H4Challenges.challenges = H4Challenges.parse(H4Challenges.requestChallenges());
		
		return null;
    }
	
	@Override
	protected void onPostExecute(Void result)
	{
		//Lib.loading(MainActivity.instance, true);
		
		H4Challenges.display(MainActivity.instance);
	}
    
}