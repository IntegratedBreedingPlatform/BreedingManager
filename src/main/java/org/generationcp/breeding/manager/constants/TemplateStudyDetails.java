package org.generationcp.breeding.manager.constants;


public enum TemplateStudyDetails {
    
    //order the values as they are expected to appear on file
    STUDY("STUDY")
    ,TITLE("TITLE")
    ,PMKEY("PMKEY")
    ,OBJECTIVE("OBJECTIVE")
    ,START_DATE("START DATE")
    ,END_DATE("END DATE")
    ,STUDY_TYPE("STUDY TYPE");
    
    private String value;
    
    private TemplateStudyDetails(String value){
    	this.value = value;
    }
	
    public String getValue(){
    	return this.value;
    }
    
}
