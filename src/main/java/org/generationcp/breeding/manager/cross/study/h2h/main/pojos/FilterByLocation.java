
package org.generationcp.breeding.manager.cross.study.h2h.main.pojos;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FilterByLocation {

	String countryName;
	String trialEnvId;
	private List<String> provinceNameList = new ArrayList<String>();
	private List<String> locationStudyNameList = new ArrayList<String>();
	private final Map<String, List<LocationStudyDto>> provinceLocationStudyMap = new HashMap<String, List<LocationStudyDto>>();
	private final Map<String, List<String>> locationStudyMap = new HashMap<String, List<String>>();
	private final List<String> locationList = new ArrayList<String>();

	public FilterByLocation(String country, String trialEnvId) {
		this.countryName = country;
		this.trialEnvId = trialEnvId;
	}

	public String getTrialEnvId() {
		return this.trialEnvId;
	}

	public void setTrialEnvId(String trialEnvId) {
		this.trialEnvId = trialEnvId;
	}

	public String getCountryName() {
		return this.countryName;
	}

	public void setCountryName(String countryName) {
		this.countryName = countryName;
	}

	public List<String> getProvinceNameList() {
		return this.provinceNameList;
	}

	public void setProvinceNameList(List<String> provinceNameList) {
		this.provinceNameList = provinceNameList;
	}

	public List<String> getLocationStudyNameList() {
		return this.locationStudyNameList;
	}

	public void setLocationStudyNameList(List<String> locationStudyNameList) {
		this.locationStudyNameList = locationStudyNameList;
	}

	public void addProvinceAndLocationAndStudy(String provinceName, String locationName, String studyName) {
		this.provinceNameList.add(provinceName);
		List<LocationStudyDto> locationStudyUniqueMap = this.provinceLocationStudyMap.get(provinceName);
		if (locationStudyUniqueMap == null) {
			locationStudyUniqueMap = new ArrayList<LocationStudyDto>();
		}
		LocationStudyDto dto = null;
		if (dto == null) {
			dto = new LocationStudyDto(locationName, studyName);
		}
		locationStudyUniqueMap.add(dto);
		this.provinceLocationStudyMap.put(provinceName, locationStudyUniqueMap);

		List<String> studyList = this.locationStudyMap.get(locationName);
		if (studyList == null) {
			studyList = new ArrayList<String>();
		}
		studyList.add(studyName);
		this.locationStudyMap.put(locationName, studyList);

		this.locationList.add(locationName);
	}

	public Integer getNumberOfEnvironmentForCountry() {
		return this.locationList.size();
	}

	public Set<String> getListOfLocationNames() {
		return this.locationStudyMap.keySet();
	}

	public Set<String> getListOfProvinceNames() {
		return this.provinceLocationStudyMap.keySet();
	}

	public Integer getNumberOfEnvironmentForProvince(String provinceName) {
		List<LocationStudyDto> locationStudyUniqueMap = this.provinceLocationStudyMap.get(provinceName);
		return locationStudyUniqueMap.size();
	}

	public Integer getNumberOfEnvironmentForLocation(String locationName) {
		List<String> studyList = this.locationStudyMap.get(locationName);
		if (studyList != null) {
			return studyList.size();
		}
		return 0;
	}

	public Collection<LocationStudyDto> getLocationStudyForProvince(String provinceName) {
		List<LocationStudyDto> locationStudyUniqueMap = this.provinceLocationStudyMap.get(provinceName);
		return locationStudyUniqueMap;
	}
}
