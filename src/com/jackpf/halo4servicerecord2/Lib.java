package com.jackpf.halo4servicerecord2;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jackpf.halo4servicerecord2.R;

public class Lib
{
	public static final String paypalUrl = "https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=CAK7V3N3YTURY";
	
	//shows an error message popup
	public static void error(Activity context, String message)
	{
		Lib.showMessage(MainActivity.instance, "Error", message);
		
		//context.finish(); //hacked in showMessage
	}
	
	public static void showMessage(Activity context, String title, String message)
	{
		AlertDialog.Builder dialog = new AlertDialog.Builder(context);
		
		//hack
		final Activity _context = (title.equals("Error")) ? context : null;
		
		dialog.setMessage(message)
	    .setCancelable(false)
	    .setPositiveButton("OK", new DialogInterface.OnClickListener(){public void onClick(DialogInterface dialog, int id){dialog.cancel(); /*hack*/ if(_context != null) _context.finish();}});
		AlertDialog alert = dialog.create();
		alert.setTitle(title);
	    alert.setIcon(R.drawable.ic_launcher);
	    alert.show();
	}
	
	//toggles the loading state
	private static ProgressDialog dialog;
	public static void loading(Activity activity, boolean enable)
	{
		//toggle gamertag input
    	//activity.findViewById(R.id.gamertag).setEnabled(enable);
		//toggle loading bar
    	if(!enable)
    		dialog = ProgressDialog.show(activity, activity.getString(R.string.loading_title), activity.getString(R.string.loading_text), true, false);
    	else if(dialog != null)
    		try{dialog.dismiss();} catch(Exception e){} //fix for crash when orientation is switched?
	}
	
	//return valid filename from asset
	public static String getFilename(String asset)
	{
		return asset.split("/")[1].split("\\.")[0].replaceAll("-", "");
	}
	
	private final static int LAUNCHES_UNTIL_PROMPT = 5;
	
	public static String getApplicationName(Context context)
	{
		PackageManager packageManager = context.getApplicationContext().getPackageManager();
		ApplicationInfo applicationInfo;
		
		try
		{
			applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
		}
		catch(final NameNotFoundException e)
		{
			applicationInfo = null;
		}
		
		return (String) (applicationInfo != null ? packageManager.getApplicationLabel(applicationInfo) : "unknown");
	}
	
	public static void ratePrompt(Context context, SharedPreferences preferences)
	{
        if(preferences.getBoolean("rate_dontshowagain", false))
        	return;
        
        SharedPreferences.Editor preferencesEditor = preferences.edit();
        
        //increment launch counter
        long rate_launchcount = preferences.getLong("rate_launchcount", 0) + 1;
        preferencesEditor.putLong("rate_launchcount", rate_launchcount);

        //prompt
        if(rate_launchcount >= LAUNCHES_UNTIL_PROMPT)
            showRateDialog(context, preferencesEditor);
        
        preferencesEditor.apply();
    }
	
	public static void showRateDialog(final Context context, final SharedPreferences.Editor preferencesEditor)
	{
		final Dialog dialog = new Dialog(context);
        dialog.setTitle("Rate " + getApplicationName(context));

        LinearLayout dialogLayout = new LinearLayout(context);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        
        TextView prompt = new TextView(context);
        prompt.setText(context.getString(R.string.rate_prompt_text));
        prompt.setWidth(240);
        prompt.setPadding(4, 0, 4, 10);
        dialogLayout.addView(prompt);
        
        Button b1 = new Button(context);
        b1.setText(context.getString(R.string.rate_b1));
        b1.setOnClickListener(
	        new OnClickListener()
	        {
	            public void onClick(View v)
	            {
	            	preferencesEditor.putBoolean("rate_dontshowagain", true);
	            	preferencesEditor.apply();
	            	
	                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + context.getPackageName())));
	               
	                dialog.dismiss();
	            }
	        }
        );        
        dialogLayout.addView(b1);

        Button b2 = new Button(context);
        b2.setText(context.getString(R.string.rate_b2));
        b2.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
            	preferencesEditor.putLong("rate_launchcount", 0);
            	preferencesEditor.apply();
                
                dialog.dismiss();
            }
        });
        dialogLayout.addView(b2);

        Button b3 = new Button(context);
        b3.setText(context.getString(R.string.rate_b3));
        b3.setOnClickListener(
        new OnClickListener()
        {
            public void onClick(View v)
            {
            	preferencesEditor.putBoolean("rate_dontshowagain", true);
            	preferencesEditor.apply();
                
                dialog.dismiss();
            }
        });
        dialogLayout.addView(b3);

        dialog.setContentView(dialogLayout);        
        dialog.show();    
    }
}
