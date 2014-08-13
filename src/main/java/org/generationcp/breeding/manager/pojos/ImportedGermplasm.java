package org.generationcp.breeding.manager.pojos;

import java.util.HashMap;
import java.util.Map;

public class ImportedGermplasm {
    
	private Integer gid;
    private Integer entryId;
    private String desig;
    private String cross;
    private String source;
    private String entryCode;
    private Double seedAmount;
    private Map<String, String> attributeVariates;
    
    public ImportedGermplasm(){
    	attributeVariates = new HashMap<String, String>();
        
    }
    
    public ImportedGermplasm(Integer entryId, String desig){
        this.entryId = entryId;
        this.desig = desig;
        attributeVariates = new HashMap<String, String>();
    }
    
    public Integer getEntryId(){
        return entryId;
    }
    
    public void setEntryId(Integer entryId){
        this.entryId = entryId;
    }
    
    public String getDesig(){
        return desig;
    }
    
    public void setDesig(String desig){
        this.desig = desig;
    }

	public void setGid(Integer gid) {
		this.gid = gid;
	}
	
	public Integer getGid(){
		return gid;
	}

	public void setCross(String cross) {
		this.cross = cross;
	}
	
	public String getCross(){
		return cross;
	}
    
	public void setSource(String source){
		this.source = source;
	}
	
	public String getSource(){
		return source;
	}
	
	public void setEntryCode(String entryCode){
		this.entryCode = entryCode;
	}
	
	public String getEntryCode(){
		return entryCode;
	}
	
    public void setSeedAmount(Double seedAmount){
        this.seedAmount = seedAmount;
	}
	
	public Double getSeedAmount(){
	    return seedAmount;
	}

	public Map<String, String> getAttributeVariates() {
		return attributeVariates;
	}

	public void setAttributeVariates(Map<String, String> variatesMap) {
		this.attributeVariates = variatesMap;
	}
	
	public void addAttributeVariate(String property, String value){
		attributeVariates.put(property, value);
	}
};