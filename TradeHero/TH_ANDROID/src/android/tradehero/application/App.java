/**
 * App.java 
 * TradeHero
 *
 * Created by @author Siddesh Bingi on Jul 21, 2013
 */
package android.tradehero.application;

import android.app.Application;
import android.content.Context;
import android.tradehero.models.ProfileDTO;
import android.tradehero.models.Trend;

public class App extends Application {
	
	private static App instance;
	
	private Trend trend;
	private ProfileDTO profileDTO;
	
	public ProfileDTO getProfileDTO() {
		return profileDTO;
	}

	public void setProfileDTO(ProfileDTO profileDTO) {
		this.profileDTO = profileDTO;
	}

	public Trend getTrend() {
		return trend;
	}

	public void setTrend(Trend trend) {
		this.trend = trend;
	}

	public App() {
		super();
		instance = this;
	}

	public static Context getContext() {
		return instance;
	}
	
	
	

}
