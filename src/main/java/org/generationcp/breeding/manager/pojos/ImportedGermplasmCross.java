package org.generationcp.breeding.manager.pojos;

import java.util.Date;

public class ImportedGermplasmCross {
	
	private Integer cross;
	private Integer femaleEntryId;
	private Integer maleEntryId;
	private Long femaleGId;
	private Long maleGId;
	private Date crossingDate;
	private String seedsHarvested;
	private String notes;
    
	public ImportedGermplasmCross(){
		
	}
	
	public ImportedGermplasmCross(Integer cross, Integer femaleEntryId, Integer maleEntryId, Long femaleGId, Long maleGId, Date crossingDate, String seedsHarvested, String notes){
		this.cross = cross;
		this.femaleEntryId = femaleEntryId;
		this.maleEntryId = maleEntryId;
		this.femaleGId = femaleGId;
		this.maleGId = maleGId;
		this.crossingDate = crossingDate;
		this.seedsHarvested = seedsHarvested;
		this.notes = notes;
	}
	
	public Integer getCross(){
		return cross;
	}
	
	public void setCross(Integer cross){
		this.cross = cross;
	}
	
	public Integer getFemaleEntryId(){
		return femaleEntryId;
	}
	
	public void setFemaleEntryId(Integer femaleEntryId){
		this.femaleEntryId = femaleEntryId;
	}
	
	public Integer getMaleEntryId(){
		return maleEntryId;
	}
	
	public void setMaleEntryId(Integer maleEntryId){
		this.maleEntryId = maleEntryId;
	}
	
	public Long getFemaleGId(){
		return femaleGId;
	}
	
	public void setFemaleGId(Long femaleGId){
		this.femaleGId = femaleGId;
	}
	
	public Long getMaleGId(){
		return maleGId;
	}
	
	public void setMaleGId(Long maleGId){
		this.maleGId = maleGId;
	}	
	
	public Date getCrossingDate(){
		return crossingDate;
	}
	
	public void setCrossingDate(Date crossingDate){
		this.crossingDate = crossingDate;
	}	

	public String getSeedsHarvested(){
		return seedsHarvested;
	}
	
	public void setSeedsHarvested(String seedsHarvested){
		this.seedsHarvested = seedsHarvested;
	}

	public String getNotes(){
		return notes;
	}
	
	public void setNotes(String notes){
		this.notes = notes;
	}
	
};