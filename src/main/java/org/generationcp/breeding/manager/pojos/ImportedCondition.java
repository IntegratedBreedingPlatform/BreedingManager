package org.generationcp.breeding.manager.pojos;

public class ImportedCondition {
    
    private String condition;
    private String description;
    private String property;
    private String scale;
    private String method;
    private String dataType;
    private String value;
    private String label;

    public ImportedCondition(){
        
    }
    
    public ImportedCondition(String condition, String description, String property, String scale, String method
            , String dataType, String value, String label) {
        this.condition = condition;
        this.description = description;
        this.property = property;
        this.scale = scale;
        this.method = method;
        this.dataType = dataType;
        this.value = value;
        this.label = label;
    }
    
    public String getCondition() {
        return condition;
    }
    
    public void setCondition(String condition){
        this.condition = condition;
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
    
    public String getValue() {
        return value;
    }
    
    public void setValue(String value){
        this.value = value;
    }
    
    public String getLabel() {
        return label;
    }
    
    public void setLabel(String label){
        this.label = label;
    }
    
};