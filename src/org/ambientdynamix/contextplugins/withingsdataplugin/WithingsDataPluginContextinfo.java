/*
 * This Plugin is licensed under GNU GPL v3 
 *
 *      http://www.gnu.org/licenses/gpl.html
 *
 *Author: Luaks Ruge
 */
package org.ambientdynamix.contextplugins.withingsdataplugin;

import java.util.HashSet;
import java.util.Set;

import org.ambientdynamix.application.api.IContextInfo;

import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;

public class WithingsDataPluginContextinfo implements IContextInfo 
{
    public static Parcelable.Creator<WithingsDataPluginContextinfo> CREATOR = new Parcelable.Creator<WithingsDataPluginContextinfo>() 
    {
    	public WithingsDataPluginContextinfo createFromParcel(Parcel in) 
    	{
    		return new WithingsDataPluginContextinfo(in);
    	}

    	public WithingsDataPluginContextinfo[] newArray(int size) 
    	{
    		return new WithingsDataPluginContextinfo[size];
    	}
    };
    
    // Sample context data
    private WithingsUser contextData;

    public WithingsUser getSampleData() 
    {
    	return contextData;
    }

    @Override
    public String toString() 
    {
    	return this.getClass().getSimpleName();
    };

    @Override
    public String getContextType() 
    {
    	return "org.ambientdynamix.contextplugins.withingsdataplugin";
    }

    @Override
    public String getStringRepresentation(String format) 
    {
    	
    	String result="";
    	WithingsUser user = contextData;
    	result="weight at "+user.getLastUpdate()+": "+user.getWeight();
    	return result;
    }

    @Override
    public String getImplementingClassname() 
    {
    	return this.getClass().getName();
    }

    @Override
    public Set<String> getStringRepresentationFormats() 
    {
    	Set<String> formats = new HashSet<String>();
    	formats.add("text/plain");
    	return formats;
    };

    public WithingsDataPluginContextinfo(WithingsUser m) 
    {
    	this.contextData = m;
    }

    private WithingsDataPluginContextinfo(final Parcel in) 
    {
    	this.contextData = in.readParcelable(getClass().getClassLoader());
    }

    public IBinder asBinder() 
    {
    	return null;
    }

    public int describeContents() 
    {
    	return 0;
    }

    public void writeToParcel(Parcel out, int flags) 
    {
    	out.writeParcelable(this.contextData, flags);
    }
}