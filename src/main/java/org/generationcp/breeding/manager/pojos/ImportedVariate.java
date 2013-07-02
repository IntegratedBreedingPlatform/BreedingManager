package org.generationcp.breeding.manager.pojos;

public class ImportedVariate {
    
    private String variate;
    private String description;
    private String property;
    private String scale;
    private String method;
    private String dataType;
    private String sampleLevel;

    public ImportedVariate(){
        
    }
    
    public ImportedVariate(String variate, String description, String property, String scale, String method
            , String dataType) {
        this.variate = variate;
        this.description = description;
        this.property = property;
        this.scale = scale;
        this.method = method;
        this.dataType = dataType;
    }
    
    public ImportedVariate(String variate, String description, String property, String scale, String method
            , String dataType, String sampleLevel) {
        this.variate = variate;
        this.description = description;
        this.property = property;
        this.scale = scale;
        this.method = method;
        this.dataType = dataType;
        this.sampleLevel = sampleLevel;
    }    
    
    public String getVariate() {
        return variate;
    }
    
    public void setVariate(String variate){
        this.variate = variate;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description){
        this.description = description;
    }
    
    public String getProperty() {
        return property;
    }
    
    public void setProperty(String property){
        this.property = property;
    }
    
    public String getScale() {
        return scale;
    }
    
    public void setScale(String scale){
        this.scale = scale;
    }
    
    public String getMethod() {
        return method;
    }
    
    public void setMethod(String method){
        this.method = method;
    }
    
    public String getDataType() {
        return dataType;
    }
    
    public void setDataType(String dataType){
        this.dataType = dataType;
    }
    
    public String getSampleLevel() {
        return sampleLevel;
    }
    
    public void setSampleLevel(String sampleLevel){
        this.sampleLevel = sampleLevel;
    }
    
};