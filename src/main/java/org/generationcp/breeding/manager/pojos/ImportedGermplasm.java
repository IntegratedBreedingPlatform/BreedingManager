package org.generationcp.breeding.manager.pojos;

public class ImportedGermplasm {
    
    private Integer entryId;
    private String desig;
    
    public ImportedGermplasm(){
        
    }
    
    public ImportedGermplasm(Integer entryId, String desig){
        this.entryId = entryId;
        this.desig = desig;
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
    
};