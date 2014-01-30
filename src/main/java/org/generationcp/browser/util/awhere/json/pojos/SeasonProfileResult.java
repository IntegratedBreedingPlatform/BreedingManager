package org.generationcp.browser.util.awhere.json.pojos;

import java.io.Serializable;
import java.util.List;

public class SeasonProfileResult implements Serializable {

	private static final long serialVersionUID = 5123643449443564191L;

	private String SeasonProfile;
	private String LocationName;
	private String LocationCoordinates;
	private String GDDModel;
	private List<SeasonProfile> Season_Profile;
	private List<TenYearAverage> TenYearAverage;
	
	public SeasonProfileResult(String seasonProfile, String locationName,
			String locationCoordinates, String gDDModel,
			List<SeasonProfile> season_Profile,
			List<TenYearAverage> tenYearAverage) {
		super();
		this.SeasonProfile = seasonProfile;
		this.LocationName = locationName;
		this.LocationCoordinates = locationCoordinates;
		this.GDDModel = gDDModel;
		this.Season_Profile = season_Profile;
		this.TenYearAverage = tenYearAverage;
	}

	public String getSeasonProfile() {
		return SeasonProfile;
	}

	public void setSeasonProfile(String seasonProfile) {
		this.SeasonProfile = seasonProfile;
	}

	public String getLocationName() {
		return LocationName;
	}

	public void setLocationName(String locationName) {
		this.LocationName = locationName;
	}

	public String getLocationCoordinates() {
		return LocationCoordinates;
	}

	public void setLocationCoordinates(String locationCoordinates) {
		this.LocationCoordinates = locationCoordinates;
	}

	public String getgDDModel() {
		return GDDModel;
	}

	public void setgDDModel(String gDDModel) {
		this.GDDModel = gDDModel;
	}

	public List<SeasonProfile> getSeason_Profile() {
		return Season_Profile;
	}

	public void setSeason_Profile(List<SeasonProfile> season_Profile) {
		this.Season_Profile = season_Profile;
	}

	public List<TenYearAverage> getTenYearAverage() {
		return TenYearAverage;
	}

	public void setTenYearAverage(List<TenYearAverage> tenYearAverage) {
		this.TenYearAverage = tenYearAverage;
	}

	@Override
	public String toString() {
		return "SeasonProfileResult [seasonProfile=" + SeasonProfile
				+ ", locationName=" + LocationName + ", locationCoordinates="
				+ LocationCoordinates + ", gDDModel=" + GDDModel
				+ ", season_Profile=" + Season_Profile + ", tenYearAverage="
				+ TenYearAverage + "]";
	}
	
}
