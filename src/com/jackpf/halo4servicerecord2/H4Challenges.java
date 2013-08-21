package com.jackpf.halo4servicerecord2;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class H4Challenges
{
	
	protected static List<Challenge> challenges = null;
	
	private static boolean authenticated = false;
	
	private static void getChallenges()
	{
    	//Intent intent = new Intent(MainActivity.instance, H4ChallengesThread.class);
    	//Lib.loading(MainActivity.instance, false);
    	//MainActivity.instance.startService(intent);
		new H4ChallengesThread().execute();
	}
	
	protected static InputStream requestChallenges()
	{
		String challengesUrl = "", spartanAuth = "";
		
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.instance);
		
		String email = preferences.getString(PreferencesActivity.KEY_EMAILADDRESS, ""),
			   password = preferences.getString(PreferencesActivity.KEY_PASSWORD, "");
		
		if(!email.equals("") && !password.equals(""))
		{
			spartanAuth = H4Authentication.requestSpartanToken(email, password);
			
			if(spartanAuth != null)
			{
				challengesUrl = "https://stats.svc.halowaypoint.com/en-gb/players/" + ServiceRecord.getGamertag() + "/H4/challenges";
				authenticated = true;
			}
		}
		
		if(challengesUrl.equals(""))
			challengesUrl = "https://stats.svc.halowaypoint.com/en-GB/h4/challenges";
		
		try
		{
			URL url = new URL(challengesUrl);
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
	
	protected static List<Challenge> parse(InputStream xmlStream)
	{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		Element document = null;
		
		try
		{
			DocumentBuilder db = dbf.newDocumentBuilder();
			document = db.parse(xmlStream).getDocumentElement();
		}
		catch(Exception e)
		{
			System.err.println(e.getMessage());
			return null;
		}
		
		NodeList challenges = document.getElementsByTagName("Challenges").item(0).getChildNodes();
		
		List<Challenge> challengesMap = new ArrayList<Challenge>();
		
		for(int i = 0; i < challenges.getLength(); i++)
		{
			NodeList challengeList = challenges.item(i).getChildNodes();
			
			HashMap<String, String> detail = new HashMap<String, String>();
			
			for(int j = 0; j < challengeList.getLength(); j++)
				detail.put(challengeList.item(j).getNodeName(), challengeList.item(j).getFirstChild().getNodeValue());
			
			challengesMap.add(new Challenge(detail));
		}
		
		Collections.sort(challengesMap);
		
		return challengesMap;
	}
	
	public static void startActivity()
	{
		Intent intent = new Intent(MainActivity.instance, MainActivity.class);
		intent.putExtra(MainActivity.I_CHALLENGES_ACTIVITY, true);
		MainActivity.instance.startActivity(intent);
	}
	
	public static void display(Activity context)
	{
		if(challenges == null)
		{
			getChallenges();
		}
		else
		{
			LinearLayout contentPane = (LinearLayout) context.findViewById(R.id.content_pane);
	    	View view = context.getLayoutInflater().inflate(R.layout.activity_challenges, contentPane, false);
	    	contentPane.addView(view);
	    	
	    	for(int i = 0; i < challenges.size(); i++)
	    	{
	    		LinearLayout challengesContent = (LinearLayout) context.findViewById(R.id.challenges);
	        	View view2 = context.getLayoutInflater().inflate(R.layout.list_challenge, challengesContent, false);
	        	challengesContent.addView(view2);
	        	
	        	Date startDate = null, endDate = null;
	        	long endTime = 0;
	        	
	        	try
	        	{
					DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.UK);
					
					startDate = dateFormat.parse(challenges.get(i).get("BeginDate").replace("T", " ").replace("Z", ""));
					endDate = dateFormat.parse(challenges.get(i).get("EndDate").replace("T", " ").replace("Z", ""));
					endTime = endDate.getTime();
				}
	        	catch(ParseException e)
	        	{
	        		System.err.println(e.getMessage());
	        	}
	        	
	        	long timeLeft = (endTime - new Date().getTime()) / 1000;
	        	
				int days = (int) TimeUnit.SECONDS.toDays(timeLeft);        
				long hours = TimeUnit.SECONDS.toHours(timeLeft) - (days * 24);
				//long minutes = TimeUnit.SECONDS.toMinutes(timeLeft) - (TimeUnit.SECONDS.toHours(timeLeft) * 60);
				//long seconds = TimeUnit.SECONDS.toSeconds(timeLeft) - (TimeUnit.SECONDS.toMinutes(timeLeft) * 60);
	        	
				String title, description;
	        	
	        	title = challenges.get(i).get("Name") + " - " + challenges.get(i).get("GameModeName") + " " + challenges.get(i).get("PeriodNamely");
	        	description = challenges.get(i).get("Description") + "\n\n" +
	        				  challenges.get(i).get("XpReward") + "XP reward\n" +
	        				  "~" + ((days > 0) ? days + " days, " : "") + hours + " hours left";
				
	        	if(authenticated)
	        	{
	        		String progress = challenges.get(i).get("Progress") + " / " + challenges.get(i).get("RequiredCount");
	        		description += "\n\n" + ((challenges.get(i).get("Completed").equals("true")) ? "<font color='green'>Completed - " + progress + "</font>" : "<font color='red'>Not completed - " + progress + "</font>");
	        	}
	        	
	        	((TextView) view2.findViewById(R.id.challenge_title)).setText(title);
	        	((TextView) view2.findViewById(R.id.challenge_description)).setText(Html.fromHtml(description.replace("\n", "<br>")), TextView.BufferType.SPANNABLE);
	    		
	        	//for(Map.Entry<String, String> entry : challengesMap.get(i).entrySet())
					//output += entry.getKey() + ": " + entry.getValue() + "\n";
	    	}
    	
	    	//((TextView) context.findViewById(R.id.challenges)).setText(output);
		}
	}
	
}

class Challenge implements Comparable<Challenge>
{
	protected Map<String, String> details;
	
	public Challenge(Map<String, String> _details)
	{
		details = _details;
	}
	
	@Override
	public int compareTo(Challenge c)
	{
		if(get("CategoryName").equals("War Games") &&
		  (c.get("CategoryName").equals("Spartan Ops") || c.get("CategoryName").equals("Campaign")))
			return -1;
		else if(get("CategoryName").equals("Spartan Ops") &&
				c.get("CategoryName").equals("War Games"))
		{
			return 1;
		}
		else if(get("CategoryName").equals("Spartan Ops") &&
				c.get("CategoryName").equals("Campaign"))
		{
			return -1;
		}
		else if(get("CategoryName").equals("Campaign") &&
			   (c.get("CategoryName").equals("War Games") || c.get("CategoryName").equals("Spartan Ops")))
		{
			return 1;
		}
		else
		{
			return 0;
		}
	}
	
	public String get(String key)
	{
		return details.get(key);
	}
}