/*
 * This Plugin is licensed under GNU GPL v3 
 *
 *      http://www.gnu.org/licenses/gpl.html
 *
 *Author: Luaks Ruge
 */
package org.ambientdynamix.contextplugins.withingsdataplugin;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.Vector;

import org.ambientdynamix.contextplugin.api.ContextPluginSettings;
import org.ambientdynamix.contextplugin.api.PluginPowerScheme;
import org.ambientdynamix.contextplugin.api.PushPullContextPluginRuntime;
import org.ambientdynamix.contextplugin.api.security.PrivacyRiskLevel;
import org.ambientdynamix.contextplugin.api.security.SecuredContextInfo;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.os.Bundle;
import android.util.Log;

/**
 * Bla
 * 
 * @author Lukas Ruge
 */
public class WithingsDataPluginRuntime extends PushPullContextPluginRuntime 
{
    private final String TAG = this.getClass().getSimpleName();
	private boolean okToRun=true;
    private String sampleData;
    private PluginPowerScheme powerScheme;
    public static final String SAMPLE_DATA_KEY = "SAMPLE_DATA_KEY";
    private static final int EVENT_VALID_MILLS = 86400000;
    private String username="";
    private String password="";
    private String email="";
	private long id=0;
	private int ispublc=0;
    private String hash="";
    private String publicKey="";
    WithingsUser user=new WithingsUser();
    //private String publicKey="0bbd8d45f59506b95871fcf4fc1f6f981326d00d6cd47b1db5fff4053513 ";
    
	@Override
    public void init(PluginPowerScheme scheme, ContextPluginSettings settings) throws Exception 
    {
		setPowerScheme(scheme);
		powerScheme=scheme;
		Log.i("Withings", "INIT");
		/*
		 * Try to load our settings. Note: init can be called when we're NEW and INITIALIZED (during updates)
		 */
		if (loadSettings(settings)) 
		{
		    // Since we successfully loaded settings, tell Dynamix we're configured.
		    getAndroidFacade().setPluginConfiguredStatus(getSessionId(), true);
		}
		else
		{
		    // Since failed to load our settings, tell Dynamix we're not configured.
		    getAndroidFacade().setPluginConfiguredStatus(getSessionId(), false);
		}
		Log.i("Withings", "init  done");
    }
	 
	/**
     * start the process by loging in, finding the right user and 
     * retreiving the values, then submitting the new values.
     * 
     * the method is  loop that sleeps a while in between.
     * 
     * a new context is only pushed if a new value is found.
     *
     */
	@Override
    public void start() 
    {
		Log.i("Withings", "start()");
		okToRun=true;
    	while(okToRun)
    	{
    		float[] values=new float[8];
    		Log.i("Withings", "oktorun");
    		//HERE COMES ALL THE LOGIC
    		//only do anything if the user has configurated his username, email and password
    		if(!(email.equals("")) && !(username.equals("")) && !(password.equals("")))
    		{
    			Log.i("Withings", "we will try:");
    			try 
    			{
    				Log.i("Withings", "to log in");
    				login();
    			} 
    			catch (Exception e) 
    			{
    				Log.i("Withings", "but failed");
    				e.printStackTrace();
    			}
    			try 
    			{
    				Log.i("Withings", "to get the userlist");
    				getUsersList();
    			}
        		catch (Exception e) 
        		{
        			Log.i("Withings", "but failed");
        			e.printStackTrace();
        		}
        		try 
        		{
        			Log.i("Withings", "to get actual values");
    				values = getValues();
    			} 
    			catch (Exception e) 
    			{
    				Log.i("Withings", "but failed");
    				Log.i("Withings", ""+e);
    				e.printStackTrace();
    			}
    		}
    		else
    		{
    			Log.i("Withings","No username, email or password provided. Plugin can not be used.");
    		}
    		boolean push=false;
    		for(int i=0; i<values.length; i++)
    		{
    			if(values[i]>0)
    			{
    				Log.i("Withings", ""+values[i]);
    				push=true;
    			}
    		}
    		if(push)
    		{
    			Log.i("Withings", "new stuff");
    			doPushContextDetection();
    		}
    		//and some sleep time... this may be rather high since updates to the withings datatabse are probebly just happen about once a day
    		try 
    		{
    			Thread.sleep(300000); //five minutes minute
    			if(powerScheme == PluginPowerScheme.BALANCED || powerScheme == PluginPowerScheme.MANUAL)
    			{
    				Thread.sleep(10500000); //severakl hours or so...
    			}
    			if(powerScheme == PluginPowerScheme.POWER_SAVER)
    			{
    				Thread.sleep(10500000); //three hours
    			}
    		}
    		catch (Exception e) 
    		{
				e.printStackTrace();
			}
    	}
    }

	/**
     * start the process by loging in, finding the right user and 
     * retreiving the values, then submitting the new values.
     * 
     * the method is  loop that sleeps a while in between.
     * 
     * a new context is only pushed if a new value is found.
     *
     *@return a array with the values (default 0 if no new value is found)
     */
	private float[] getValues()  throws Exception 
	{
		Log.i("Withings", "getvalues");
		Date lastupdate = user.getLastUpdate();
		Log.i("Withings", "lastupdate"+lastupdate);
		Date newlastupdate= lastupdate;
		String newlastupdatestring="";
		String request = "http://wbsapi.withings.net/measure?action=getmeas&userid=" + id +
                "&publickey=" + publicKey +
                //"&limit=10"+
                "&devtype=1";                   // Only values from the scale
        String response = performRequest(request);
        System.out.println(response);
        // Decode JSon object
        JSONObject json = new JSONObject(response);
        JSONArray JSONvalues = json.getJSONObject("body").getJSONArray("measuregrps");
        JSONObject JSONvalue;
        JSONArray JSONtypes;
        JSONObject JSONtype;
        long timestamp;
        float weight=0;
        float height=0;
        float fatFreeMass=0;
        float fatRatio=0;
        float fatMassWeight=0;
        float diastolicBloodPressure=0;
        float systolicBloodPressure=0;
        float pulse=0;
        int size = JSONvalues.length();
        for (int i = 0; i < size; i++) 
        {
                JSONvalue = JSONvalues.getJSONObject(i);
                if (JSONvalue.getInt("category") != 1) 
                {        
                        continue;
                }
                timestamp = JSONvalue.getLong("date");
                String stamp = ""+timestamp;
                Date newdate = new Date(Long.parseLong(stamp) * 1000);
               	System.out.println(timestamp);
               	if(newdate.after(newlastupdate))
               	{
               	   Log.i("Withings", "newdate "+newdate);
               	   newlastupdate=newdate;
               	   newlastupdatestring=stamp;
               	}
                if(newdate.after(lastupdate))
                {
                	//Log.i("Withings", "newdate after lastupdate");
                	JSONtypes = JSONvalue.getJSONArray("measures");
                    for (int j = 0; j < JSONtypes.length(); j++) 
                    {
                            JSONtype = JSONtypes.getJSONObject(j);
                         	//1  Weight (kg)
                         	//4  Height (meter)
                         	//5  Fat Free Mass (kg)
                         	//6  Fat Ratio (%)
                         	//8  Fat Mass Weight (kg)
                         	//9  Diastolic Blood Pressure (mmHg)
                         	//10 Systolic Blood Pressure (mmHg)
                         	//11 Heart Pulse (bpm)
                            if (JSONtype.getInt("type") == 1) 
                            {             
                            	if(weight==0)
                            	{
                            		Log.i("Withings", ""+weight);
                                    weight = (float) (JSONtype.getInt("value") * Math.pow(10, JSONtype.getInt("unit")));
                            		Log.i("Withings", ""+weight);
                            	}
                            }
                            if (JSONtype.getInt("type") == 2) 
                            { 
                            	//not used yet
                            }
                            if (JSONtype.getInt("type") == 3) 
                            { 
                            	//not used yet
                            }
                            if (JSONtype.getInt("type") == 4) 
                            { 
                            	if(height==0)
                            	{
                            		height = (float) (JSONtype.getInt("value") * Math.pow(10, JSONtype.getInt("unit")));
                            	}
                            }
                            if (JSONtype.getInt("type") == 5) 
                            { 
                            	if(fatFreeMass==0)
                            	{
                            		fatFreeMass = (float) (JSONtype.getInt("value") * Math.pow(10, JSONtype.getInt("unit")));
                            	}
                            }
                            if (JSONtype.getInt("type") == 6) 
                            { 
                            	if(fatRatio==0)
                            	{
                            		fatRatio = (float) (JSONtype.getInt("value") * Math.pow(10, JSONtype.getInt("unit")));
                            	}
                            }
                            if (JSONtype.getInt("type") == 7) 
                            { 
                            	//not used yet
                            }
                            if (JSONtype.getInt("type") == 8) 
                            {             
                            		if(fatMassWeight==0)
                            		{
                            			fatMassWeight = (float) (JSONtype.getInt("value") * Math.pow(10, JSONtype.getInt("unit")));
                            		}
                            }
                            if (JSONtype.getInt("type") == 9) 
                            { 
                            	if(diastolicBloodPressure ==0)
                            	{
                            		diastolicBloodPressure = (float) (JSONtype.getInt("value") * Math.pow(10, JSONtype.getInt("unit")));
                            	}
                            }
                            if (JSONtype.getInt("type") == 10) 
                            { 
                            	if(systolicBloodPressure==0)
                            	{
                            		systolicBloodPressure = (float) (JSONtype.getInt("value") * Math.pow(10, JSONtype.getInt("unit")));
                            	}
                            }
                            if (JSONtype.getInt("type") == 11) 
                            { 
                            	if(pulse==0)
                            	{
                            		pulse = (float) (JSONtype.getInt("value") * Math.pow(10, JSONtype.getInt("unit")));
                            	}
                            }
                    }
                }
        }
        request = "http://wbsapi.withings.net/measure?action=getmeas&userid=" + id +
        "&publickey=" + publicKey +
        "&limit=10"+
        "&devtype=0";                   // Only values from the scale
		response = performRequest(request);
		System.out.println(response);
        json = new JSONObject(response);
        JSONvalues = json.getJSONObject("body").getJSONArray("measuregrps");
        size = JSONvalues.length();
        for (int i = 0; i < size; i++) 
        {
                JSONvalue = JSONvalues.getJSONObject(i);
                if (JSONvalue.getInt("category") != 1) 
                {        
                        continue;
                }
                timestamp = JSONvalue.getLong("date");
                String stamp = ""+timestamp;
                Date newdate = new Date();
               	System.out.println(timestamp);
               	try 
               	{
               		newdate = new Date(Long.parseLong(stamp) * 1000);
               	   	if(newdate.after(newlastupdate))
               	   	{
               	   		Log.i("Withings", "newdate2 "+newdate);
               	   		newlastupdate=newdate;
               	   		newlastupdatestring=stamp;
               	   	}
               	} 
               	catch (Exception ex ) 
               	{
               	    ex.getStackTrace();
               	}
                if(newdate.after(lastupdate))
                {
	                JSONtypes = JSONvalue.getJSONArray("measures");
	                for (int j = 0; j < JSONtypes.length(); j++) 
	                {
	                        JSONtype = JSONtypes.getJSONObject(j);
	                        if (JSONtype.getInt("type") == 1) 
	                        {             
	                        	if(weight==0)
	                        	{
	                                weight = (float) (JSONtype.getInt("value") * Math.pow(10, JSONtype.getInt("unit")));
	                        	}
	                        }
	                        if (JSONtype.getInt("type") == 2) 
	                        { 
	                        	//not used yet
	                        }
	                        if (JSONtype.getInt("type") == 3) 
	                        { 
	                        	//not used yet
	                        }
	                        if (JSONtype.getInt("type") == 4) 
	                        { 
	                        	if(height==0)
	                        	{
                            		Log.i("Withings h", ""+height);
	                        		height = (float) (JSONtype.getInt("value") * Math.pow(10, JSONtype.getInt("unit")));
	                        		Log.i("Withings h", ""+height);
	                        	}
	                        }
	                        if (JSONtype.getInt("type") == 5) 
	                        { 
	                        	if(fatFreeMass==0)
	                        	{
	                        		fatFreeMass = (float) (JSONtype.getInt("value") * Math.pow(10, JSONtype.getInt("unit")));
	                        	}
	                        }
	                        if (JSONtype.getInt("type") == 6) 
	                        { 
	                        	if(fatRatio==0)
	                        	{
	                        		fatRatio = (float) (JSONtype.getInt("value") * Math.pow(10, JSONtype.getInt("unit")));
	                        	}
	                        }
	                        if (JSONtype.getInt("type") == 7) 
	                        { 
	                        	//not used yet
	                        }
	                        if (JSONtype.getInt("type") == 8) 
	                        {             
	                        		if(fatMassWeight==0)
	                        		{
	                        			fatMassWeight = (float) (JSONtype.getInt("value") * Math.pow(10, JSONtype.getInt("unit")));
	                        		}
	                        }
	                        if (JSONtype.getInt("type") == 9) 
	                        { 
	                        	if(diastolicBloodPressure ==0)
	                        	{
	                        		diastolicBloodPressure = (float) (JSONtype.getInt("value") * Math.pow(10, JSONtype.getInt("unit")));
	                        	}
	                        }
	                        if (JSONtype.getInt("type") == 10) 
	                        { 
	                        	if(systolicBloodPressure==0)
	                        	{
	                        		systolicBloodPressure = (float) (JSONtype.getInt("value") * Math.pow(10, JSONtype.getInt("unit")));
	                        	}
	                        }
	                        if (JSONtype.getInt("type") == 11) 
	                        { 
	                        	if(pulse==0)
	                        	{
	                        		pulse = (float) (JSONtype.getInt("value") * Math.pow(10, JSONtype.getInt("unit")));
	                        	}
	                        }
	                }
                }
        }
        float[] values = new float[8];
        values[0]=weight;
        values[1]=height;
        values[2]=fatFreeMass;
        values[3]=fatRatio;
        values[4]=fatMassWeight;
        values[5]=diastolicBloodPressure;
        values[6]=systolicBloodPressure;
        values[7]=pulse;
        Log.i("Withings", "Als neuer Timestamp wird übergeben:"+newlastupdatestring);
		user.update(newlastupdatestring, values);
        return values;
	}

	/**
     * this method handles the login into the Withings plattform
     * 
     * This code is based on the code found at http://code.google.com/p/libra-android/
     *
     */
    private void login() throws Exception 
    {
        // Step one: Get a one-time "magic string"
        String magicString = getMagicString();
        System.out.println(magicString);
        
        //Step two: Get the MD5 hash of the password
        String passwordHash = md5Hash(password);
        System.out.println(passwordHash);
        
        //Step three: Concatenate the email, password hash and magic string, separated by a colon
        String concat = email + ":" + passwordHash + ":" + magicString;
        
        // Step four: Get the MD5 hash of the above string
        hash = md5Hash(concat);
        System.out.println(hash);
    }
    
	/**
     * this method handles the User List from the Withings platform
     * 
     * This code is based on the code found at http://code.google.com/p/libra-android/
     *
     */
    private void getUsersList() throws Exception 
    {
        String request = "http://wbsapi.withings.net/account?action=getuserslist&email=" + email + "&hash=" + hash;
        String response = performRequest(request);
        
        // Decode JSon object
        JSONObject json = new JSONObject(response);
        JSONArray JSONusers = json.getJSONObject("body").getJSONArray("users");
        JSONObject JSONuser;
        int size = JSONusers.length();
        for (int i = 0; i < size; i++) {
           JSONuser = JSONusers.getJSONObject(i);
           String theusername = JSONuser.getString("firstname") + " " + JSONuser.getString("lastname");
           if(theusername.equals(username))
           {
        	   id=JSONuser.getLong("id");
        	   publicKey=JSONuser.getString("publickey");
        	   ispublc = JSONuser.getInt("ispublic");
           }
        }
    }

	/**
     * this method performs requests to the Withings API
     * 
     * This code is based on the code found at http://code.google.com/p/libra-android/
     *
     */
    private String performRequest(String url) throws Exception {
        // Perform the request
        HttpClient client = new DefaultHttpClient();
        HttpGet get = new HttpGet(url);
        HttpResponse response = client.execute(get);
        
        // Translate the response into a string
        String result = "";
        try {
                InputStream in = response.getEntity().getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                StringBuilder str = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) {
                        str.append(line + "\n");
                }
                in.close();
                result = str.toString();
        } catch (Exception ex) {
                result = "Error";
        }
        
        // Decode JSon object
        JSONObject json = new JSONObject(result);
        // Make sure that the request was successful
        if (json.getInt("status") != 0) {
                throw new Exception("Withings error: " + json.getInt("status"));
        }
        
        return result;
    }

	/**
     * this method returns an md5Hash of a String
     * 
     * This code is based on the code found at http://code.google.com/p/libra-android/
     *
     *@param A String to be hashed
     */
	private String md5Hash(String string)  throws Exception 
	{
        MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
        digest.update(string.getBytes());
        byte messageDigest[] = digest.digest();

        // Get the Hex String
        BigInteger bigInt = new BigInteger(1, messageDigest);
        return bigInt.toString(16);
	}

	/**
     * this method requests the so called magic String from the Withings API
     * 
     * This code is based on the code found at http://code.google.com/p/libra-android/
     *
     */
    private String getMagicString() throws Exception 
    {
        String request = "http://wbsapi.withings.net/once?action=get";
        String response = performRequest(request);
        
        // Decode the magic string from the JSon response
        JSONObject json = new JSONObject(response);
        return json.getJSONObject("body").getString("once");
    }

	@Override
    public void handleContextRequest(UUID requestId, String contextDataType) 
    {
		/*
		 * Perform context scan without configuration.
		 */
		    Log.i(TAG, "handleContextRequest for requestId: " + requestId);
		    pullEventHelper("", requestId, EVENT_VALID_MILLS);
    }

    @Override
    public void handleConfiguredContextRequest(UUID requestId, String contextInfoType, Bundle scanConfig) 
    {
	/*
	 * Use the incoming scanConfig Bundle to control how we perform the context scan.
	 */
	    Log.i(TAG, "handleConfiguredContextRequest for requestId: " + requestId);
	    pullEventHelper("", requestId, EVENT_VALID_MILLS);
    }
    
    @Override
    public void stop() 
    {
		okToRun = false;
		Thread t=Thread.currentThread();
		t.interrupt();  
		Log.d(TAG, "Stopped!");
		Log.d("Withings", "Stopped!111");
    }

    @Override
    public void destroy() 
    {
		stop();
		Log.i(TAG, this + " is Destroyed!");
    }

    @Override
    public void doManualContextScan() 
    {
	    pushEventHelper("", EVENT_VALID_MILLS);
    }

    @Override
    public void updateSettings(ContextPluginSettings settings) 
    {
	if (loadSettings(settings)) {
	    getAndroidFacade().storeContextPluginSettings(getSessionId(), settings);
	    getAndroidFacade().setPluginConfiguredStatus(getSessionId(), true);
	}
    }

    @Override
    public void setPowerScheme(PluginPowerScheme scheme) 
    {
		Log.i(TAG, "Setting PowerScheme " + scheme);
		powerScheme = scheme;
    }

    /*
     * Simple context detection loop that generates push events.
     */
    private void doPushContextDetection() 
    {
		Log.i("Muhaha", "Entering doPushContextDetection");
	    // Send a sample broadcast event
	    pushEventHelper("", EVENT_VALID_MILLS);
	    Log.i("Muhaha", "Exiting doPushContextDetection");
    }

	/*
     * Utility for responding to pull requests.
     */
    private void pullEventHelper(String message, UUID requestId, int validMills) 
    {
    	sendContextEvent(requestId, constructEventList(message), validMills);
    }

    /*
     * Utility for sending push events.
     */
    private void pushEventHelper(String message, int validMills) 
    {
    	sendBroadcastContextEvent(constructEventList(message), validMills);
    }

    /*
     * Utility that constructs a list of SecuredContextInfo containing each different FidelityLevel.
     */
    private List<SecuredContextInfo> constructEventList(String message) 
    {
		List<SecuredContextInfo> eventList = new Vector<SecuredContextInfo>();
		eventList.add(new SecuredContextInfo(new WithingsDataPluginContextinfo(user), PrivacyRiskLevel.LOW));
		return eventList;
    }

    /*
     * Utility for loading settings.
     */
    private boolean loadSettings(ContextPluginSettings settings) 
    {
	// Check settings type and store
	if (settings != null) 
	{
	    Log.i(TAG, "Received previously stored settings: " + settings);
	    try 
	    {
	    	sampleData = settings.get(SAMPLE_DATA_KEY);
	    	username=settings.get("username");
	    	email = settings.get("email");
	    	password= settings.get("password");
	    	
	    	return true;
	    }
	    	catch (Exception e) {
	    		Log.w(TAG, "Failed to parse settings: " + e.getMessage());
	    }
	}
	else
	    if (settings == null) 
	    {
		// Create default settings
		Log.i(TAG, "No settings found!");
	    }
	return false;
    }
}