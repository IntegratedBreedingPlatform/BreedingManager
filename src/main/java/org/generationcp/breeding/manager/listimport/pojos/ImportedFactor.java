package org.generationcp.breeding.manager.listimport.pojos;

public class ImportedFactor {
	
	private String factor;
	private String description;
	private String property;
	private String scale;
	private String method;
	private String dataType;
	private String label;

	public ImportedFactor(){
	}
	
	public ImportedFactor(String factor, String description, String property, String scale, String method
			, String dataType, String label) {
		this.factor = factor;
		this.description = description;
		this.property = property;
		this.scale = scale;
		this.method = method;
		this.dataType = dataType;
		this.label = label;
	}
	
	public String getFactor() {
		return factor;
	}
	
	public void setFactor(String factor){
		this.factor = factor;
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
	
	public String getLabel() {
		return label;
	}
	
	public void setLabel(String label){
		this.label = label;
	}
	
};