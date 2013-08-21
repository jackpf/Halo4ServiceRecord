package com.jackpf.halo4servicerecord2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class ServiceRecord
{
	public	final static String	GAMERTAG_URL	= "https://stats.svc.halowaypoint.com/en-gb/players/{gamertag}/h4/servicerecord",
								SPARTAN_URL		= "https://spartans.svc.halowaypoint.com/players/{gamertag}/h4/spartans/fullbody?target={size}",
								EMBLEM_URL		= "https://emblems.svc.halowaypoint.com/h4/emblems/{emblem}",
								RECENTGAMES_URL	= "https://stats.svc.halowaypoint.com/en-gb/players/{gamertag}/H4/matches";
								//HALO3_URL		= "http://www.halowaypoint.com/en-us/players/{gamertag}/career/overviewsection?game=Halo3";
								//specialisations: https://assets.halowaypoint.com/games/h4/specializations/v1/small/stalker.png
								//challenges: https://stats.svc.halowaypoint.com/en-us/h4/challenges

	private final String		gamertag;
	
	SharedPreferences			preferences;
	public static ServiceRecord	instance;
	
	public ServiceRecord(String gamertag)
	{
		this.gamertag = gamertag;
		
		instance = this;
		
		preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.instance);
	}
	
	public static String getGamertag()
	{
		return ServiceRecord.instance.gamertag;
	}
	
	public InputStream requestServiceRecord()
	{
		try
		{
			URL url = new URL(GAMERTAG_URL.replace("{gamertag}", URLEncoder.encode(gamertag, "UTF-8")));
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			
			return connection.getInputStream();
		}
		catch(Exception e)
		{
			System.err.println(e.getMessage());
			
			return null;
		}
	}
	
	public InputStream requestRecentGames()
	{
		String email = preferences.getString(PreferencesActivity.KEY_EMAILADDRESS, ""),
			   password = preferences.getString(PreferencesActivity.KEY_PASSWORD, "");
		
		if(email.equals("") || password.equals(""))
			return null;
		
		String spartanAuth = H4Authentication.requestSpartanToken(email, password);
		
		try
		{
			URL url = new URL(RECENTGAMES_URL.replace("{gamertag}", URLEncoder.encode(gamertag, "UTF-8")));
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			
			connection.addRequestProperty(H4Authentication.AUTH_HEADER, spartanAuth);
			
			return connection.getInputStream();
		}
		catch(Exception e)
		{
			System.err.println(e.getMessage());
			
			return null;
		}
	}
	
	public File requestSpartan(File cacheDir)
	{
		try
		{
			URL url = new URL(SPARTAN_URL.replace("{gamertag}", URLEncoder.encode(gamertag, "UTF-8")).replace("{size}", preferences.getString(PreferencesActivity.KEY_SPARTANQUALITY, "")));
			//File file = File.createTempFile(gamertag + "_spartan", ".png", cacheDir);
			File file = new File(cacheDir, gamertag + "_spartan.png");
			
			cacheFile(url, file);
			
			return file;
		}
		catch(Exception e)
		{
			System.err.println(e.getMessage());
			
			return null;
		}
	}
	
	public File requestEmblem(File cacheDir, String emblem)
	{
		try
		{
			URL url = new URL(EMBLEM_URL.replace("{emblem}", emblem.replace("{size}", "80")));
			//File file = File.createTempFile(gamertag + "_emblem", ".png", cacheDir);
			File file = new File(cacheDir, gamertag + "_emblem.png");
			
			cacheFile(url, file);
			
			return file;
		}
		catch(Exception e)
		{
			System.err.println(e.getMessage());
			
			return null;
		}
	}
	
	private void cacheFile(URL url, File file) throws IOException
	{
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		
		InputStream in = connection.getInputStream();
		FileOutputStream out = new FileOutputStream(file);
		
		int read = 0;
		byte[] bytes = new byte[1024];
		
		while((read = in.read(bytes)) != -1)
			out.write(bytes, 0, read);
		
		out.flush();
		out.close();
	}
	
	private void cache()
	{
		/*long currentTime = System.currentTimeMillis();
		long expires = connection.getHeaderFieldDate("Expires", currentTime);
		long lastModified = connection.getHeaderFieldDate("Last-Modified", currentTime);
		
		File cacheFile = new File(gamertag + ".cache");
		
		if(!cacheFile.exists() || lastModified > cacheFile.lastModified() || currentTime > expires)
		{
			InputStream in = connection.getInputStream();
			InputStream bIn = new BufferedInputStream(in);
			FileOutputStream out = new FileOutputStream(gamertag + ".cache");
			
			int read = 0;
			byte[] bytes = new byte[1024];
			bIn.mark(1 << 24); //Integer.MAX_VALUE ?
			
			while((read = bIn.read(bytes)) != -1)
				out.write(bytes, 0, read);
			
			out.flush();
			out.close();
			
			bIn.reset();
			
			return bIn;
		}
		else
		{
			return new FileInputStream(gamertag + ".cache");
		}*/
	}
	
	public void finalize()
	{
		
	}
}
