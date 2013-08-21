package com.jackpf.halo4servicerecord2;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

public class H4Authentication
{
	private static String spartanAuth = null;
	
	public static final String AUTH_HEADER = "X-343-Authorization-Spartan";
	
	public static String requestSpartanToken(String email, String password)
	{
		if(spartanAuth != null)
			return spartanAuth;
		
		//final String cRegexLoginPPFT = "var\\s+ServerData\\s*=\\s*[^<]+(<input(\\s+[a-zA-Z0-9]+\\s*=\\s*(\"[^\"]*\"|[^ ]+))*\\s*/?\\s*>)";
        //final String cRegexLoginPPSX = "var\\s+ServerData\\s*=\\s*[^<]+F\\s*:\\s*['\"]([a-zA-Z0-9]*)['\"]";
		final String cRegexLoginPPFT = "<input.*?name=\"PPFT\".*?value=\"(.*?)\"";
		final String cRegexLoginPPSX = "g:\'(.*?)\'";
		//final String cRegexLoginPPFTValue = "value=\"([^\"]+)\"";
        //final String cRegexHaloTokens = "var\\s+user\\s*=\\s*\\{\\s*access_token\\s*:\\s*'([a-zA-Z0-9=/+]+)'\\s*,\\s*AuthenticationToken\\s*:\\s*'([a-zA-Z0-9=/+.]+)'\\s*,\\s*expires_in\\s*:\\s*([0-9]+)\\s*\\}";
        final String cUserAgent = "Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.97 Safari/537.11";
        final String cUrlHalo = "https://app.halowaypoint.com/en-us/";
        final String cUrlLogin = "https://login.live.com/oauth20_authorize.srf?client_id=000000004C0BD2F1&scope=xbox.basic%20xbox.offline_access&response_type=code&redirect_uri=https://app.halowaypoint.com/oauth/callback&state=MAdodHRwczovL2FwcC5oYWxvd2F5cG9pbnQuY29tL2VuLXVzLw&display=touch";
        final String cUrlLoginPost = "https://login.live.com/ppsecure/post.srf?client_id=000000004C0BD2F1&scope=xbox.basic%20xbox.offline_access&response_type=code&redirect_uri=https://app.halowaypoint.com/oauth/callback&state=MAdodHRwczovL2FwcC5oYWxvd2F5cG9pbnQuY29tL2VuLXVzLw&display=touch&bk=";
        
        try
        {
        	DefaultHttpClient client = new DefaultHttpClient();
        	
        	client.getParams().setParameter(ClientPNames.COOKIE_POLICY,CookiePolicy.BROWSER_COMPATIBILITY);
        	CookieStore cookieStore = new BasicCookieStore();
        	client.setCookieStore(cookieStore);
        	
        	HttpGet request = new HttpGet(cUrlLogin);
        	
        	request.setHeader("User-Agent", cUserAgent);
			request.setHeader("Referer", cUrlHalo);
			
        	HttpResponse response = client.execute(request);

        	BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        	    
        	String line = "", html = "";
        	while((line = rd.readLine()) != null)
        		html+=(line);
        	
        	String loginPPFT = "", loginPPSX = "";
		    Pattern r;
		    Matcher m;
		    
		    r = Pattern.compile(cRegexLoginPPFT);
		    m = r.matcher(html);
			
		    if(m.find())
		    	loginPPFT = m.group(1);
			else
				throw new Exception("Could not extract PPFT");
		    
		    r = Pattern.compile(cRegexLoginPPSX);
		    m = r.matcher(html);
			
		    if(m.find())
		    	loginPPSX = m.group(1);
			else
				throw new Exception("Could not extract PPSX");
		    
		    HttpPost request2 = new HttpPost(cUrlLoginPost);
		    
        	request2.setHeader("Content-Type", "application/x-www-form-urlencoded");
        	//request2.setHeader("Content-Length", Integer.toString(postData.length()));
        	request2.setHeader("User-Agent", cUserAgent);
        	request2.setHeader("Referer", cUrlLogin);
        	
        	List<NameValuePair> params = new ArrayList<NameValuePair>(2);
        	params.add(new BasicNameValuePair("PPFT", loginPPFT));
        	params.add(new BasicNameValuePair("login", email));
        	params.add(new BasicNameValuePair("passwd", password));
        	params.add(new BasicNameValuePair("LoginOptions", "3"));
        	params.add(new BasicNameValuePair("NewUser", "1"));
        	params.add(new BasicNameValuePair("type", "11"));
        	params.add(new BasicNameValuePair("i3", Integer.toString(15000 + (int) (Math.random() * (35000 + 1)))));
        	params.add(new BasicNameValuePair("m1", "1920"));
        	params.add(new BasicNameValuePair("m2", "1080"));
        	params.add(new BasicNameValuePair("m3", "0"));
        	params.add(new BasicNameValuePair("il2", "1"));
        	params.add(new BasicNameValuePair("i17", "0"));
        	params.add(new BasicNameValuePair("i18", "__MobileLogin|1,"));
        	
        	request2.setEntity(new UrlEncodedFormEntity(params));
        	
        	HttpResponse response2 = client.execute(request2);
        	
        	rd = new BufferedReader(new InputStreamReader(response2.getEntity().getContent()));
              	    
        	line = ""; html = "";
        	while((line = rd.readLine()) != null)
        		if(line.contains("access_token"))
        		{
        			html = line;
        			break;
        		}
	      	
	      	/*String location = null;
	      	Header[] headers = response2.getAllHeaders();
	      	for(int i = 0; i < headers.length; i++)
	      	{
	      		String h = headers[i].toString();
	      		
	      		if(h.contains("Location"))
	      			location = h.split("Location: ")[1];
	      	}
        	
        	HttpGet request3 = new HttpGet(location);
        	
        	request3.setHeader("User-Agent", cUserAgent);
			request3.setHeader("Referer", cUrlLoginPost);
			
        	HttpResponse response3 = client.execute(request3);
        	
        	rd = new BufferedReader(new InputStreamReader(response3.getEntity().getContent()));
            
        	line = ""; html = "";
        	while((line = rd.readLine()) != null)
        		html+=(line);
	      	
	      	headers = response3.getAllHeaders();
	      	for(int i = 0; i < headers.length; i++)
	      	{
	      		String h = headers[i].toString();
	      		
	      		if(h.contains("Location"))
	      			location = h.split("Location: ")[1];
	      	}
	      	
	      	request3 = new HttpGet(location);
        	request3.setHeader("User-Agent", cUserAgent);
			request3.setHeader("Referer", cUrlLoginPost);
        	response3 = client.execute(request3);
        	
        	rd = new BufferedReader(new InputStreamReader(response3.getEntity().getContent()));
            
        	line = ""; html="";
	      	while ((line = rd.readLine()) != null) {
	      	  html+=(line);
	      	}*/
	      	
	      	String accessTokenRegex = "access_token:'(.*?)'",
	      		   authenticationTokenRegex = "AuthenticationToken:'(.*?)'",
	      		   tokenExpiresRegex = "expires_in:([0-9]+)";
	      	String accessToken = null, authenticationToken, tokenExpires;
	      	
	      	r = Pattern.compile(accessTokenRegex);
		    m = r.matcher(html);
			
		    if(m.find())
		    	accessToken = m.group(1);
			else
				throw new Exception("Could not extract access token");
		    
		    r = Pattern.compile(authenticationTokenRegex);
		    m = r.matcher(html);
			
		    if(m.find())
		    	authenticationToken = m.group(1);
			else
				throw new Exception("Could not extract authentication token");
		    
		    String spartanUrl = "https://settings.svc.halowaypoint.com/RegisterClientService.svc/spartantoken/wlid";
        	
		    HttpGet request4 = new HttpGet(spartanUrl);
        	request4.setHeader("X-343-Authorization-WLID", "v1=" + accessToken);
        	request4.setHeader("Accept", "application/json");
        	
        	HttpResponse response4 = client.execute(request4);
        	
        	rd = new BufferedReader(new InputStreamReader(response4.getEntity().getContent()));
            
        	line = ""; html="";
	      	while ((line = rd.readLine()) != null) {
	      	  html+=(line);
	      	}
	      	
	      	r = Pattern.compile("\"SpartanToken\":\"(.*?)\"");
		    m = r.matcher(html);
		    
		    String spartan = "";
		    
		    if(m.find())
		    	spartanAuth = m.group(1);
		    else
		    	throw new Exception("Could not extract spartan token");
		    
		    return spartanAuth;
        }
        catch(Exception e)
        {
        	e.printStackTrace(); //System.err.println(e.getMessage());
        	
        	return null;
        }
	}
}