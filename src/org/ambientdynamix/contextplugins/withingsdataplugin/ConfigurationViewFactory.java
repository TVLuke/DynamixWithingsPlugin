/*
 * This Plugin is licensed under GNU GPL v3 
 *
 *      http://www.gnu.org/licenses/gpl.html
 *
 *Author: Luaks Ruge
 */

package org.ambientdynamix.contextplugins.withingsdataplugin;

import java.util.Date;

import org.ambientdynamix.contextplugin.api.*;

import android.content.Context;
import android.text.method.PasswordTransformationMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ConfigurationViewFactory implements IContextPluginConfigurationViewFactory 
{

    @Override 
    public View initializeView(Context context, final ContextPluginRuntime runtime, int titleBarHeight) 
    {
    	// Discover our screen size for proper formatting 
    	DisplayMetrics met = context.getResources().getDisplayMetrics();

    	// Access our Locale via the incoming context's resource configuration to determine language
    	String language = context.getResources().getConfiguration().locale.getDisplayLanguage();

    	LinearLayout layout = new LinearLayout(context);
    	layout.setOrientation(LinearLayout.VERTICAL);

    	layout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
    	TextView lblTitle = new TextView(context);
    	lblTitle.setText("Withings Settings");
    	lblTitle.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
    	layout.addView(lblTitle);
		
		//Email

		TextView email = new TextView(context);
		email.setText("Email Adress");
		email.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		layout.addView(email);
		final EditText txtemail = new EditText(context);
		txtemail.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		layout.addView(txtemail);

    	//Username
		TextView username = new TextView(context);
		username.setText("User Name");
		username.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		layout.addView(username);
		final EditText txtusername = new EditText(context);
		txtusername.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		layout.addView(txtusername);

		// Password
		TextView lblPass = new TextView(context);
		lblPass.setText("Password");
		lblPass.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		layout.addView(lblPass);
		final EditText txtPass = new EditText(context);
		txtPass.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		txtPass.setTransformationMethod(PasswordTransformationMethod.getInstance());
		layout.addView(txtPass);

    	ContextPluginSettings settings = runtime.getAndroidFacade().getContextPluginSettings(runtime.getSessionId());
		if (settings != null) 
		{
		    txtemail.setText(settings.get("email"));
		    txtPass.setText(settings.get("password"));
		    txtusername.setText(settings.get("username"));
		}

		// Create the button
		final Button flashToggle = new Button(context);
		flashToggle.setText("Submit");
		flashToggle.setMinimumWidth(150);
		flashToggle.setOnClickListener(new View.OnClickListener()
		{
		    public void onClick(View v) 
		    {
				ContextPluginSettings settings = new ContextPluginSettings();
				//settings.put(WithingsDataPluginRuntime.SAMPLE_DATA_KEY, new Date().toString());
				if (txtemail.getText() != null)
				{ 
				    settings.put("email", txtemail.getText().toString());
				}
				if (txtusername.getText() != null)
				{ 
				    settings.put("username", txtusername.getText().toString());
				}		
				if (txtPass.getText() != null)
				{
				    settings.put("password", txtPass.getText().toString());
				}
				// Try to store the settings.
				if (runtime.getAndroidFacade().storeContextPluginSettings(runtime.getSessionId(), settings)) 
				{
				    runtime.getAndroidFacade().setPluginConfiguredStatus(runtime.getSessionId(), true);
				    try 
				    {
				    	runtime.updateSettings(settings);
				    }
				    catch (Exception e) 
				    {
				    	Log.i("withings", "Exception when updating settings: " + e);
				    }
				    runtime.getAndroidFacade().closeConfigurationView(runtime.getSessionId());
				}

				else runtime.getAndroidFacade().setPluginConfiguredStatus(runtime.getSessionId(), false);
		    }
		});
		layout.addView(flashToggle);
		return layout;
    }

    @Override
    public void destroyView() 
    {
    	// Nothing to do in this case.	
    }
}