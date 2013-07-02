package org.generationcp.breeding.manager.pojos;

import java.util.Date;

public class ImportedGermplasmCross {
    
    private Integer cross;
    private Integer femaleEntryId;
    private Integer maleEntryId;
    private Integer femaleGId;
    private Integer maleGId;
    private Date crossingDate;
    private String seedsHarvested;
    private String notes;
    
    //these fields are NOT part of template, but filled up during validation from middleware
    private String femaleDesignation;
    private String maleDesignation;
    
    public ImportedGermplasmCross(){
        
    }
    
    public ImportedGermplasmCross(Integer cross, Integer femaleEntryId, Integer maleEntryId, Integer femaleGId, Integer maleGId, Date crossingDate, String seedsHarvested, String notes){
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
    
    public Integer getFemaleGId(){
        return femaleGId;
    }
    
    public void setFemaleGId(Integer femaleGId){
        this.femaleGId = femaleGId;
    }
    
    public Integer getMaleGId(){
        return maleGId;
    }
    
    public void setMaleGId(Integer maleGId){
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

    public String getFemaleDesignation() {
        return femaleDesignation;
    }

    public void setFemaleDesignation(String femaleDesignation) {
        this.femaleDesignation = femaleDesignation;
    }

    public String getMaleDesignation() {
        return maleDesignation;
    }

    public void setMaleDesignation(String maleDesignation) {
        this.maleDesignation = maleDesignation;
    }
    
};