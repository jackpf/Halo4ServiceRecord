package com.jackpf.halo4servicerecord2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ServiceRecordDisplay implements OnClickListener
{
	public static void display(Activity context, Intent intent)
	{
		LinearLayout contentPane = (LinearLayout) context.findViewById(R.id.content_pane);
    	View view = context.getLayoutInflater().inflate(R.layout.activity_servicerecord, contentPane, false);
    	contentPane.addView(view);
    	
    	String output;
    	
    	//initialise collapsible sections
    	int sections_headers[] = {R.id.section_player, R.id.section_weapons, R.id.section_wargames, R.id.section_wargames_custom, R.id.section_csr, R.id.section_recentgames};
    	initialiseSections(view, sections_headers);
    	
    	//player stats
    	((TextView) view.findViewById(R.id.gamertag_title)).setText(intent.getStringExtra("Gamertag"));
    	//((TextView) view.findViewById(R.id.xboxlive_link)).setText(Html.fromHtml(String.format(context.getString(R.string.xboxlive_profile_url), intent.getStringExtra("Gamertag"))));
    	//((TextView) view.findViewById(R.id.xboxlive_link)).setMovementMethod(LinkMovementMethod.getInstance());
    	((TextView) view.findViewById(R.id.servicetag_title)).setText(intent.getStringExtra("ServiceTag"));

    	int csrResource = context.getResources().getIdentifier("csr_" + intent.getStringExtra("TopSkillRank"), "drawable",  context.getPackageName());
    	((ImageView)view.findViewById(R.id.csr_image)).setImageResource(csrResource);
    	
    	int srResource = context.getResources().getIdentifier("sr_" + String.format("%03d", Integer.parseInt(intent.getStringExtra("RankName"))), "drawable",  context.getPackageName());
    	((ImageView)view.findViewById(R.id.sr_image)).setImageResource(srResource);
    	
    	output = String.format(context.getString(R.string.player_stats_output),
			//"Tag",			intent.getStringExtra("ServiceTag"),
	    	//"Rank",			"SR" + intent.getStringExtra("RankName"),
	    	//"Top CSR",		"CSR" + intent.getStringExtra("TopSkillRank"),
	    	//"Spec.",		intent.getStringExtra("SpecialisationCurrent"),
	    	"SP earned",	intent.getStringExtra("SpartanPoints") + " / 52",
	    	"Progress",		String.format("%.0f", Float.parseFloat(intent.getStringExtra("TotalCommendationProgress")) * 100) + "%",
	    	"Challenges",	intent.getStringExtra("TotalChallengesCompleted"),
	    	"Play time",	ServiceRecordParser.parseTime(intent.getStringExtra("TotalGameplay"))
    	);

    	((TextView) view.findViewById(R.id.player_stats)).setText(output);
        
    	//show spartan image
    	final File spartan = new File(context.getCacheDir(), intent.getStringExtra("Spartan"));
    	if(spartan.exists())
    	{
    		final String pathName = spartan.getPath(); 
    		Drawable drawable = Drawable.createFromPath(pathName);
    		
    		ImageView spartanImg = (ImageView) view.findViewById(R.id.spartan_image);
    		spartanImg.setImageDrawable(drawable);
    		
    		//open image in gallery when clicked
    		spartanImg.setOnClickListener(
    			new OnClickListener()
    			{
	    			@Override
	    	        public void onClick(View v)
	    			{
	    				//image needs copying to sd card to allow it to be viewed in gallery
	    				final String sdDir = "halo4servicerecord",
	    							 sdSpartan = Environment.getExternalStorageDirectory() + "/" + sdDir + "/" + spartan.getName();
	    				
	    				File file = new File(Environment.getExternalStorageDirectory(), sdDir);
	    			    if(!file.exists())
	    			        if(!file.mkdirs())
	    			        	System.err.println("Error creating SD directory");
	    			    
	    			    try
	    			    {
		    				FileChannel inChannel = new FileInputStream(pathName).getChannel(),
		    							outChannel = new FileOutputStream(sdSpartan).getChannel();
		    			    
		    				inChannel.transferTo(0, inChannel.size(), outChannel);
	    			    }
	    			    catch(IOException e)
	    			    {
	    			    	System.err.println(e.getMessage());
	    			    }
	    			    /*finally
	    			    {
	    			        if(inChannel != null)
	    			            inChannel.close();
	    			        if(outChannel != null)
	    			            outChannel.close();
	    			    }*/
	    				
	    				Intent intent = new Intent();
	    				intent.setAction(Intent.ACTION_VIEW);
	    				intent.setDataAndType(Uri.fromFile(/*spartan*/ new File(sdSpartan)), "image/*");
	    				MainActivity.instance.startActivity(intent);
	    	        }
    			}
    		);
    	}
    	else
    		Lib.error(context, context.getString(context.getResources().getIdentifier("error_" + ServiceRecordParser.PARSE_ERROR_PARSING, "string", context.getPackageName())));
    	
    	File emblem = new File(context.getCacheDir(), intent.getStringExtra("Emblem"));
    	if(emblem.exists())
    	{
    		String pathName = emblem.getPath();
    		Drawable drawable = Drawable.createFromPath(pathName);
    		
    		((ImageView) view.findViewById(R.id.emblem_image)).setImageDrawable(drawable);
    	}
    	else
    		Lib.error(context, context.getString(context.getResources().getIdentifier("error_" + ServiceRecordParser.PARSE_ERROR_PARSING, "string", context.getPackageName())));
    	
    	//rank progress bar
    	int	xp = Integer.parseInt(intent.getStringExtra("XP")),
    		rankStartXp = Integer.parseInt(intent.getStringExtra("RankStartXP")),
    		nextRankStartXp;

    	//for god knows why, nextRankStartXp = 9223372036854775807 when rank = 130
    	String _nextRankStartXp = intent.getStringExtra("NextRankStartXP");
    	String	currentRank = intent.getStringExtra("RankName"),
    			nextRank = intent.getStringExtra("NextRankName");
    	if(!_nextRankStartXp.equals("9223372036854775807"))
    		nextRankStartXp = Integer.parseInt(_nextRankStartXp);
    	else
    	{
    		nextRankStartXp = 0;
    		nextRank = currentRank;
    	}
    	
    	//not sure why, but NextRankStartXP seems to be 0 when equal to XP and this messes things up
    	int progress, total;
    	if(nextRankStartXp == 0)
    		progress = total = xp;
    	else
    	{
    		progress = xp - rankStartXp;
        	total = nextRankStartXp - rankStartXp;
    	}
    	
    	final ProgressBar rankProgress = (ProgressBar) view.findViewById(R.id.rank_progress);
    	final int _progress = progress, _total = total, progressTo = (int) ((float) progress / (float) total * 100.0);;
    	//rankProgress.setProgress();
    	final Handler handler = new Handler()
    	{
    		@Override
    		public void handleMessage(Message msg)
    		{
    			int i = Integer.parseInt((String) msg.obj);
    			rankProgress.setProgress(i);
    			
    			int xp = 0;
    			if(progressTo > 0)
    				xp = _progress / progressTo * i;
    			
    			xp = (i < progressTo) ? xp : _progress; //fix innacuracy caused by calculating percentage
    			((TextView) MainActivity.instance.findViewById(R.id.progress_current_rank_xp)).setText(Integer.toString(xp) + "XP");
    		}
    	};
    	new Thread()
    	{
	    	public void run()
	    	{
	    		
    			
				for(int i = 0; i <= progressTo; i++)
				{
					Message msg = new Message();
			    	msg.obj = Integer.toString(i);
			    	handler.sendMessage(msg);
			    	try
					{
					    sleep(50);
					}
					catch(InterruptedException ex)
					{
					    Thread.currentThread().interrupt();
					}
				}
	    	}
    	}.start();
    	
    	((TextView) view.findViewById(R.id.progress_current_rank)).setText("SR" + currentRank);
    	((TextView) view.findViewById(R.id.progress_next_rank)).setText("SR" + nextRank);
    	
    	//((TextView) view.findViewById(R.id.progress_current_rank_xp)).setText(Integer.toString(progress) + "XP");
    	((TextView) view.findViewById(R.id.progress_next_rank_xp)).setText(Integer.toString(total) + "XP");
    	
    	//show medals
    	for(int i = 1; i <= 5; i++)
    	{
    		String asset = intent.getStringExtra("Medal" + i);
    		
    		if(asset != null)
    		{
	    		String medal = Lib.getFilename(asset);
	    		
	    		int imageResource = context.getResources().getIdentifier("medal" + i + "_image", "id", context.getPackageName());
	        	ImageView medalView = (ImageView) view.findViewById(imageResource);
	            
	        	int medalResource = context.getResources().getIdentifier("medal_" + medal, "drawable",  context.getPackageName());
	        	medalView.setImageResource(medalResource);
	        	
	        	final String description = intent.getStringExtra("Medal" + i + "_name") + ": " + intent.getStringExtra("Medal" + i + "_description");
	        	medalView.setOnTouchListener(
	    			new OnTouchListener()
	    			{
						@Override
						public boolean onTouch(View v, MotionEvent event)
						{
							Toast toast = Toast.makeText(MainActivity.instance, description, Toast.LENGTH_SHORT);
							//toast.setGravity(Gravity.TOP | Gravity.LEFT, (int) event.getX(), (int) event.getY());
							toast.show();
							
							return false;
						}
	    			}
	        	);
    		}
    	}

    	for(int i = 1; true; i++)
    	{
    		String asset = intent.getStringExtra("SpecialisationCompleted" + i);
    		
    		if(asset != null)
    		{
    			String specialisation = Lib.getFilename(asset);
	    		
	    		int imageResource = context.getResources().getIdentifier("specialisation" + i + "_image", "id", context.getPackageName());
	        	ImageView specialisationView = (ImageView) view.findViewById(imageResource);
	        	
	        	int specialisationResource = context.getResources().getIdentifier("specialisation_" + specialisation, "drawable",  context.getPackageName());
	        	specialisationView.setImageResource(specialisationResource);
	        	
	        	final String description = intent.getStringExtra("SpecialisationCompleted" + i + "_name");//"SpecialisationCompleted" + i + "_name") + ": " + intent.getStringExtra("SpecialisationCompleted" + i + "_description");
	        	specialisationView.setOnTouchListener(
	    			new OnTouchListener()
	    			{
						@Override
						public boolean onTouch(View v, MotionEvent event)
						{
							Toast toast = Toast.makeText(MainActivity.instance, description, Toast.LENGTH_SHORT);
							//toast.setGravity(Gravity.TOP | Gravity.LEFT, (int) event.getX(), (int) event.getY());
							toast.show();
							
							return false;
						}
	    			}
	        	);
    		}
    		else
    			break;
    	}
    	
    	//weapons stats
    	output = String.format(context.getString(R.string.weapons_stats_output),
    			"Favourite weapon",	intent.getStringExtra("FavoriteWeaponName"),
    			"Total kills",		intent.getStringExtra("FavoriteWeaponTotalKills"),
    			"Percentage total",	String.format("%.2f", Float.parseFloat(intent.getStringExtra("FavoriteWeaponTotalKills")) / Float.parseFloat(intent.getStringExtra("WargamesTotalKills")) * 100) + "%"
    	);
    	
    	((TextView) view.findViewById(R.id.favourite_weapon)).setText(output);
    	
    	//show favourite weapon image
    	String favWeapon = Lib.getFilename(intent.getStringExtra("FavoriteWeaponImageUrl"));
    	ImageView favWeaponImg = (ImageView) view.findViewById(R.id.favourite_weapon_image);
        int imageResource = context.getResources().getIdentifier("weapon_" + favWeapon, "drawable",  context.getPackageName());
    	favWeaponImg.setImageResource(imageResource);
    	
    	output = "<i>" + intent.getStringExtra("FavoriteWeaponDescription") + "</i>";
    	
    	((TextView) view.findViewById(R.id.weapons_stats)).setText(Html.fromHtml(output));
    	
    	//wargames stats
    	String modes[] = {"Wargames", "WargamesCustom"};
    	int sections[] = {R.id.section_wargames, R.id.section_wargames_custom};
    	int contents[] = {R.id.wargames_stats, R.id.wargames_custom_stats};
    	for(int i = 0; i < modes.length; i++)
    	{
	    	String prefix = modes[i];
	    	
	    	output = String.format(context.getString(R.string.wargames_stats_output),
	    			"Total kills",		intent.getStringExtra(prefix + "TotalKills"),
	    			"Total deaths",		intent.getStringExtra(prefix + "TotalDeaths"),
	    			"K/D ratio",		String.format("%.2f", Float.parseFloat(intent.getStringExtra(prefix + "KDRatio"))),
	    			"Average score",	intent.getStringExtra(prefix + "AveragePersonalScore"),
	    			"Average kills",	String.format("%.2f", Float.parseFloat(intent.getStringExtra(prefix + "TotalKills")) / Float.parseFloat(intent.getStringExtra(prefix + "TotalGamesCompleted"))),
	    			"Time played",		ServiceRecordParser.parseTime(intent.getStringExtra(prefix + "TotalDuration")),
	    			"Medals earned",	intent.getStringExtra(prefix + "TotalMedals"),
	    			"Games completed",	intent.getStringExtra(prefix + "TotalGamesCompleted"),
	    			"Games won",		intent.getStringExtra(prefix + "TotalGamesWon"),
	    			"Games lost",		Integer.toString(Integer.parseInt(intent.getStringExtra(prefix + "TotalGamesCompleted")) - Integer.parseInt(intent.getStringExtra(prefix + "TotalGamesWon"))),
	    			"Win ratio",		String.format("%.2f", Float.parseFloat(intent.getStringExtra(prefix + "TotalGamesWon")) / Float.parseFloat(intent.getStringExtra(prefix + "TotalGamesCompleted")) * 100) + "%"
	    	);
	
	        ((TextView) view.findViewById(sections[i])).setOnClickListener(new ServiceRecordDisplay()); //make collapsible
	        ((TextView) view.findViewById(contents[i])).setText(output);
    	}
    	
    	//CSR stats
    	//output = "";
    	int playlist = 1;
    	while(intent.getStringExtra("PlaylistName" + playlist) != null)
    	{
    		/*output += String.format(context.getString(R.string.csr_stats_output),
	    			intent.getStringExtra("PlaylistName" + playlist),
	    			intent.getStringExtra("PlaylistCSR" + playlist)
	    	);*/
    		
    		String output1 = intent.getStringExtra("PlaylistName" + playlist),
    		       output2 = intent.getStringExtra("PlaylistCSR" + playlist);
    		
    		LinearLayout csrContent = (LinearLayout) view.findViewById(R.id.section_csr_content);
        	View view2 = context.getLayoutInflater().inflate(R.layout.list_csr, csrContent, false);

			((TextView) view2.findViewById(R.id.tv1)).setText(output1);
			((TextView) view2.findViewById(R.id.tv2)).setText(String.format("%2s", output2));
			
			float barWeight = Integer.parseInt(output2) / 50.0f;

			((TextView) view2.findViewById(R.id.bar)).setLayoutParams(new TableLayout.LayoutParams(0, 10, 1 - barWeight));
			((TextView) view2.findViewById(R.id.antibar)).setLayoutParams(new TableLayout.LayoutParams(0, 10, barWeight));
        	
			csrContent.addView(view2);

    		playlist++;
    	}
    	
        ((TextView) view.findViewById(R.id.section_csr)).setOnClickListener(new ServiceRecordDisplay()); //make collapsible
		//((TextView) view.findViewById(R.id.csr)).setText(output);
        
        //scroll out the input
        ((ScrollView) context.findViewById(R.id.scroll_view)).postDelayed(
	        new Runnable()
	        {
	            public void run()
	            {
	                ((ScrollView) MainActivity.instance.findViewById(R.id.scroll_view)).smoothScrollTo(0, ((View) MainActivity.instance.findViewById(R.id.gamertag).getParent()).getHeight());
	            }
	        }
        , 100L);
        
        //get recent games
        MainActivity.loadRecentGames();
    }
	
	private static void initialiseSections(View view, int sections[])
	{
		SharedPreferences preferences = MainActivity.instance.getPreferences(Context.MODE_PRIVATE);
		
		for(int i = 0; i < sections.length; i++)
		{
			((TextView) view.findViewById(sections[i])).setOnClickListener(new ServiceRecordDisplay()); //make collapsible
			
			if(preferences.getBoolean("Section" + Integer.toString(sections[i]), true) == false)
				toggleSection(sections[i], false);
		}
	}
	
	private static void toggleSection(int sid, boolean write)
	{
		int resId = MainActivity.instance.getResources().getIdentifier(MainActivity.instance.getResources().getResourceEntryName(sid) + "_content", "id",  MainActivity.instance.getPackageName());
    	LinearLayout section = (LinearLayout) MainActivity.instance.findViewById(resId);
		
    	int visibility, arrow;
    	boolean display;
    	
    	if(section.getVisibility() == LinearLayout.VISIBLE)
    	{
    		visibility = LinearLayout.GONE;
    		arrow = R.drawable.arrow_side;
    		display = false;
    	}
    	else
    	{
    		visibility = LinearLayout.VISIBLE;
    		arrow = R.drawable.arrow_down;
    		display = true;
    	}

    	section.setVisibility(visibility);
		
		((TextView) MainActivity.instance.findViewById(sid)).setCompoundDrawablesWithIntrinsicBounds(arrow, 0, 0, 0);
		
		if(write)
		{
			SharedPreferences preferences = MainActivity.instance.getPreferences(Context.MODE_PRIVATE);
			SharedPreferences.Editor preferencesEditor = preferences.edit();
			
			preferencesEditor.putBoolean("Section" + Integer.toString(sid), (visibility == LinearLayout.VISIBLE) ? true : false);
			
			preferencesEditor.apply();
		}
	}
	
	@Override
	public void onClick(View v)
	{
		toggleSection(v.getId(), true);
	}
	
	public static void displayRecentGames(Map<Integer, Map<String, String>> recentGames)
	{
		LinearLayout rgContent = (LinearLayout) MainActivity.instance.findViewById(R.id.section_recentgames_content);
		
		if(rgContent == null)
			return; //finished?
		
		if(recentGames == null)
		{
			((TextView) MainActivity.instance.findViewById(R.id.recentgames)).setText("Unable to load recent games, please ensure you have a valid email address and password entered in the settings.");
			return;
		}
		
		rgContent.removeAllViews();
		
		for(int i = 0; i < Math.min(recentGames.size(), 10); i++)
		{
			View view = MainActivity.instance.getLayoutInflater().inflate(R.layout.list_recentgame, rgContent, false);
	    	
			Map<String, String> stats = recentGames.get(i);
	    	String output = "";
	    	
	    	output = String.format(MainActivity.instance.getString(R.string.recentgames_stats_output),
	    		stats.get("MapVariantName"),
	    		stats.get("VariantName"),
	    		/*"Completed",*/				(stats.get("Completed").equals("true")) ? "Game completed" : "Game not completed",
	    		/*"Result",*/					stats.get("Result"),
	    		stats.get("FeaturedStatName"),	stats.get("FeaturedStatValue"),
	    		"Score",						stats.get("PersonalScore"),
	    		"Medals",						stats.get("TotalMedals")
	    	);
	    	
	    	((TextView) view.findViewById(R.id.game_description)).setText(output);
	    	
	    	if(stats.get("MapImageUrl") != null) //getting crash reports of this being null too
	    		new ImageFromUrl((ImageView) view.findViewById(R.id.game_image)).execute("https://assets.halowaypoint.com/games/h4/maps/v1/medium/" + stats.get("MapImageUrl").split("/")[1]);
	    	if(stats.get("BaseVariantImageUrl") != null) //idk why this is null sometimes, silly 343... maybe for custom games?
	    		new ImageFromUrl((ImageView) view.findViewById(R.id.game_variant)).execute("https://assets.halowaypoint.com/games/h4/game-base-variants/v1/medium/" + stats.get("BaseVariantImageUrl").split("/")[1]);
	    	
	    	rgContent.addView(view);
		}
	}
	
	private static class ImageFromUrl extends AsyncTask<String, Void, Bitmap> {
	    ImageView bmImage;

	    public ImageFromUrl(ImageView bmImage) {
	        this.bmImage = bmImage;
	    }

	    protected Bitmap doInBackground(String... urls) {
	        String urldisplay = urls[0];
	        Bitmap mIcon11 = null;
	        try {
	            InputStream in = new java.net.URL(urldisplay).openStream();
	            mIcon11 = BitmapFactory.decodeStream(in);
	        } catch (Exception e) {
	            Log.e("Error", e.getMessage());
	            e.printStackTrace();
	        }
	        return mIcon11;
	    }

	    protected void onPostExecute(Bitmap result) {
	        bmImage.setImageBitmap(result);
	    }
	}
}

