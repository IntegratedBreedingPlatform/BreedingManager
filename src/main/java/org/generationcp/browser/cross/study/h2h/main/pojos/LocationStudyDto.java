package org.generationcp.browser.cross.study.h2h.main.pojos;

public class LocationStudyDto {
	private String locationName;
	private String studyName;
	
	public LocationStudyDto(String location, String studyName){
		this.locationName = location;
		this.studyName = studyName;
	}

	public String getLocationName() {
		return locationName;
	}

	public void setLocationName(String locationName) {
		this.locationName = locationName;
	}

	public String getStudyName() {
		return studyName;
	}

	public void setStudyName(String studyName) {
		this.studyName = studyName;
	}
	
	
}
