package org.generationcp.browser.cross.study.h2h.main.pojos;


public class FilterLocationDto {

	String countryName;
	String provinceName;
	String locationName;
	String studyName;
	int level;
	
	public FilterLocationDto(String country, String province, String location, String study, int level){
		this.countryName = country;
		this.provinceName = province;
		this.locationName = location;
		this.studyName = study;
		this.level = level;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public String getCountryName() {
		return countryName;
	}

	public void setCountryName(String countryName) {
		this.countryName = countryName;
	}

	public String getProvinceName() {
		return provinceName;
	}

	public void setProvinceName(String provinceName) {
		this.provinceName = provinceName;
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