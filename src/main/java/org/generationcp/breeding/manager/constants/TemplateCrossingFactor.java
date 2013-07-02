package org.generationcp.breeding.manager.constants;


public enum TemplateCrossingFactor {
    
    //order the values as they are expected to appear on file
    CROSS("CROSS")
    ,FEMALE_ENTRY_ID("FEMALE ENTRY ID")
    ,MALE_ENTRY_ID("MALE ENTRY ID")
    ,FGID("FGID")
    ,MGID("MGID");
    
    private String value;
    
    private TemplateCrossingFactor(String value){
        this.value = value;
    }
    
    public String getValue(){
        return this.value;
    }
    
}
