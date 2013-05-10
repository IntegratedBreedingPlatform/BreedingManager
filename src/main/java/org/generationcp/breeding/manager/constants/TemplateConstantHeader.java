package org.generationcp.breeding.manager.constants;


public enum TemplateConstantHeader {
    
    //order the values as they are expected to appear on file
    CONSTANT("CONSTANT")
    ,DESCRIPTION("DESCRIPTION")
    ,PROPERTY("PROPERTY")
    ,SCALE("SCALE")
    ,METHOD("METHOD")
    ,DATA_TYPE("DATA TYPE")
    ,VALUE("VALUE")
    ,SAMPLE_LEVEL("SAMPLE LEVEL");
    
    private String header;
    
    private TemplateConstantHeader(String header){
    	this.header = header;
    }
	
    public String getHeader(){
    	return this.header;
    }
    
}
