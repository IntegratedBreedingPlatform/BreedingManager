package org.generationcp.breeding.manager.constants;


public enum ExportCrossesObservationSheetHeaders {
    
    //order the values as they are expected to appear on file
    ENTRY_ID("Entry ID")
    ,GID("GID")
    ,ENTRY_CODE("Entry Code")
    ,DESIG("Designation")
    ,CROSS("Cross")
    ,SOURCE("Source")
    ,FEMALE("Female")
    ,MALE("Male")
    ,FEMALE_GID("Female GID")
    ,MALE_GID("Male GID");
    
    private String value;
    
    private ExportCrossesObservationSheetHeaders(String value){
        this.value = value;
    }
    
    public String getValue(){
        return this.value;
    }
    
}