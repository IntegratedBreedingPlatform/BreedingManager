package org.generationcp.breeding.manager.constants;


public enum TemplateConditionHeader {
    
    //order the values as they are expected to appear on file
    CONDITION("CONDITION")
    ,DESCRIPTION("DESCRIPTION")
    ,PROPERTY("PROPERTY")
    ,SCALE("SCALE")
    ,METHOD("METHOD")
    ,DATA_TYPE("DATA TYPE")
    ,VALUE("VALUE");
    
    
    private String header;
    
    private TemplateConditionHeader(String header){
        this.header = header;
    }
    
    public String getHeader(){
        return this.header;
    }
    
}
