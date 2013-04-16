package org.generationcp.breeding.manager.listimport.pojos;

public class ImportedConstant {
	
	private String constant;
	private String description;
	private String property;
	private String scale;
	private String method;
	private String dataType;
	private String value;

	public ImportedConstant(){
		
	}
	
	public ImportedConstant(String constant, String description, String property, String scale, String method
			, String dataType, String value) {
		this.constant = constant;
		this.description = description;
		this.property = property;
		this.scale = scale;
		this.method = method;
		this.dataType = dataType;
		this.value = value;
	}
	
	public String getConstant() {
		return constant;
	}
	
	public void setConstant(String constant){
		this.constant = constant;
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
	
};