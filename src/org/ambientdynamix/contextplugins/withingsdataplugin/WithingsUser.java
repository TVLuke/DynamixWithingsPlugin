/*
 * This Plugin is licensed under GNU GPL v3 
 *
 *      http://www.gnu.org/licenses/gpl.html
 *
 * @author Lukas Ruge
 *  
 * @version 1.0.0
 */

package org.ambientdynamix.contextplugins.withingsdataplugin;

import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class WithingsUser  implements Parcelable {

	private String lastupdate;
	private float weight=0;
	private float height=0;
	private float fatFreeMass=0;
	private float fatRatio=0;
	private float fatMassWeight=0;
	private float diastolicBloodPressure=0;
	private float systolicBloodPressure=0;
	private float pulse=0;
	/**
     * Static Creator factory for Parcelable.
     */
    public static Parcelable.Creator<WithingsUser> CREATOR = new Parcelable.Creator<WithingsUser>() 
    {
		public WithingsUser createFromParcel(Parcel in) 
		{
		    return new WithingsUser(in);
		}
	
		public WithingsUser[] newArray(int size) 
		{
		    return new WithingsUser[size];
		}
    };
    /**
     * Public Constructor.
     *
     * The Public Constructer gegerates a new Withungs user with the lastupdate time 0 (1.1.1970)
     */
    public WithingsUser()
    {
    	//create an initial lastmeasurment at time 0 (1.1.1970)
        lastupdate= "0";
    }
    
    /**
     * Get the time of the last update
     *
     * @return Date of the last update
     */
    public Date getLastUpdate()
    {
    	Log.i("Withings", "lastupdate Sting="+lastupdate);
    	Date stamp = new Date(Long.parseLong(lastupdate) * 1000);
       	Log.i("Withings", "The Last Update happened at"+stamp);
    	return stamp;
    }
    
    /**
     * Get the users last submitted weight
     *
     * @return weight of the user
     */
    public float getWeight()
    {
    	return weight;
    }
    
    /**
     * Get the users entered height
     *
     * @return height of the user
     */
    public float getHight()
    {
    	return height;
    }
    
    /**
     * Get the users last submitted fat free mass
     *
     * @return fat free mass of the user
     */
    public float getFatFreeMass()
    {
    	return fatFreeMass;
    }
    
    /**
     * Get the users last submitted fat ratio
     *
     * @return fat ratio of the user
     */
    public float getFatRatio()
    {
    	return fatRatio;
    }
    
    /**
     * Get the users last submitted fat mass weight
     *
     * @return fat mass weight of the user
     */
    public float getFatMassWeight()
    {
    	return fatMassWeight;
    }
    
    /**
     * Get the users last submitted diastolic blood pressure
     *
     * @return diastolic blood pressure of the user
     */
    public float getdiastolicBloodPressure()
    {
    	return diastolicBloodPressure;
    }
    
    /**
     * Get the users last submitted systolic blood pressure
     *
     * @return systolic blood pressure of the user
     */
    public float getsystolicBloodPressure()
    {
    	return systolicBloodPressure;
    }
    
    /**
     * Get the users last submitted systolic blood pressure
     *
     * @return pulse of the user
     */
    public float pulse()
    {
    	return pulse;
    }
    
    /**
     * submit an update to the user
     *
     * @param timestamp String in the EPOCH Format used by Withings
     * @param float[8] with the Withings measurements (default for no value is 0)
     */
    public void update(String timestamp, float[] measurements)
    {
    	//update new measurements
    	Log.i("Withings", "update");
    	if(measurements.length==8)
    	{
    		Log.i("Withings", "ok");
    		if(!(timestamp.equals("")))
    		{
    			lastupdate=timestamp;
    		}
	    	if(measurements[0]>0)
	    	{
	    		weight=measurements[0];
	    	}
	    	if(measurements[1]>0)
	    	{
	    		height=measurements[1];
	    	}
	    	if(measurements[2]>0)
	    	{
	    		fatFreeMass=measurements[1];
	    	}
	    	if(measurements[3]>0)
	    	{
	    		fatRatio=measurements[3];
	    	}
	    	if(measurements[4]>0)
	    	{
	    		fatMassWeight=measurements[4];
	    	}
	    	if(measurements[5]>0)
	    	{
	    		diastolicBloodPressure=measurements[5];
	    	}
	    	if(measurements[6]>0)
	    	{
	    		systolicBloodPressure=measurements[6];
	    	}
	    	if(measurements[7]>0)
	    	{
	    		pulse=measurements[7];
	    	}
    	}
    }
    
    /**
     * private constructor
     *
     * @param the Parcel
     */
	private WithingsUser(Parcel in) 
	{
		lastupdate= in.readString();
		weight=in.readFloat();
		height=in.readFloat();
		fatFreeMass=in.readFloat();
		fatRatio=in.readFloat();
		fatMassWeight=in.readFloat();
		diastolicBloodPressure=in.readFloat();
		systolicBloodPressure=in.readFloat();
		pulse=in.readFloat();
	}

	@Override
	public int describeContents() 
	{
		return 0;
	}

	@Override
	public void writeToParcel(Parcel parcel, int flags) 
	{
		parcel.writeString(lastupdate);	
		parcel.writeFloat(weight);
		parcel.writeFloat(height);
		parcel.writeFloat(fatFreeMass);
		parcel.writeFloat(fatRatio);
		parcel.writeFloat(fatMassWeight);
		parcel.writeFloat(diastolicBloodPressure);
		parcel.writeFloat(systolicBloodPressure);
		parcel.writeFloat(pulse);
	}
}
