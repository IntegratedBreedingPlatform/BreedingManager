package org.generationcp.browser.cross.study.h2h.pojos;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


public class EnvironmentForComparison implements Serializable{

    private static final long serialVersionUID = -879684249019712493L;
    
    private Integer environmentNumber;
    private String locationName;
    private String countryName;
    private String studyName;
    private Map<String, Integer> traitAndNumberOfPairsComparableMap;
    
    public EnvironmentForComparison(Integer environmentNumber, String locationName, String countryName, String studyName,
            Map<String, Integer> traitAndNumberOfPairsComparableMap) {
        super();
        this.environmentNumber = environmentNumber;
        this.locationName = locationName;
        this.countryName = countryName;
        this.studyName = studyName;
        this.traitAndNumberOfPairsComparableMap = traitAndNumberOfPairsComparableMap;
    }
    
    public Integer getEnvironmentNumber() {
        return environmentNumber;
    }
    
    public void setEnvironmentNumber(Integer environmentNumber) {
        this.environmentNumber = environmentNumber;
    }
    
    public String getLocationName() {
        return locationName;
    }
    
    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }
    
    public String getCountryName() {
        return countryName;
    }
    
    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }
    
    public String getStudyName() {
        return studyName;
    }
    
    public void setStudyName(String studyName) {
        this.studyName = studyName;
    }
    
    public Map<String, Integer> getTraitAndNumberOfPairsComparableMap() {
        if(traitAndNumberOfPairsComparableMap == null){
            traitAndNumberOfPairsComparableMap = new HashMap<String, Integer>();
        }
        return traitAndNumberOfPairsComparableMap;
    }
    
    public void setTraitAndNumberOfPairsComparableMap(Map<String, Integer> traitAndNumberOfPairsComparableMap) {
        this.traitAndNumberOfPairsComparableMap = traitAndNumberOfPairsComparableMap;
    }
}
