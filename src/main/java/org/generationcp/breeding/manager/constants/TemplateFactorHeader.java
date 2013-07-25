package org.generationcp.breeding.manager.constants;


public enum TemplateFactorHeader {
    
    //order the values as they are expected to appear on file
    FACTOR("FACTOR")
    ,DESCRIPTION("DESCRIPTION")
    ,PROPERTY("PROPERTY")
    ,SCALE("SCALE")
    ,METHOD("METHOD")
    ,DATA_TYPE("DATA TYPE")
    ,NESTED_IN("")
    ,LABEL("LABEL");
    
    private String header;
    
    private TemplateFactorHeader(String header){
        this.header = header;
    }
    
    public String getHeader(){
        return this.header;
    }
    
}
