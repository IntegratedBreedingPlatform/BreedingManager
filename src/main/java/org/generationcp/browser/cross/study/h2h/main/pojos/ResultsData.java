package org.generationcp.browser.cross.study.h2h.main.pojos;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.generationcp.middleware.domain.h2h.Observation;
import org.generationcp.middleware.domain.h2h.TraitInfo;

import com.vaadin.ui.ComboBox;


public class ResultsData implements Serializable{

    private static final long serialVersionUID = -879684249019712493L;
    
    private Integer gid1;
    private String gid1Name;
    private Integer gid2;
    private String gid2Name;
    private Map<String, String> traitDataMap; //key is the traitName and suffix, then val
    
    
    public ResultsData(Integer gid1,String gid1Name, Integer gid2,String gid2Name, Map<String, String> traitDataMap) {
        super();
        this.gid1 = gid1;
        this.gid2 = gid2;
        this.gid1Name = gid1Name;
        this.gid2Name = gid2Name;
        this.traitDataMap = traitDataMap;
    //    this.traitAndNumberOfPairsComparableMap = traitAndNumberOfPairsComparableMap;
    }
    
    


	public String getGid1Name() {
		return gid1Name;
	}




	public void setGid1Name(String gid1Name) {
		this.gid1Name = gid1Name;
	}




	public String getGid2Name() {
		return gid2Name;
	}




	public void setGid2Name(String gid2Name) {
		this.gid2Name = gid2Name;
	}




	public Integer getGid1() {
		return gid1;
	}


	public void setGid1(Integer gid1) {
		this.gid1 = gid1;
	}


	public Integer getGid2() {
		return gid2;
	}


	public void setGid2(Integer gid2) {
		this.gid2 = gid2;
	}


	public Map<String, String> getTraitDataMap() {
		return traitDataMap;
	}


	public void setTraitDataMap(Map<String, String> traitDataMap) {
		this.traitDataMap = traitDataMap;
	}
    
    
    
}
