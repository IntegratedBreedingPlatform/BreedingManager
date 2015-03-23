package org.generationcp.breeding.manager.constants;

public enum AddColumnContextMenuOption {
	
	PREFERRED_ID("PREFERRED ID",String.class),
	PREFERRED_NAME("PREFERRED NAME",String.class),
	GERMPLASM_DATE("GERMPLASM DATE",String.class),
	LOCATIONS("LOCATIONS",String.class),
	METHOD_NAME("METHOD NAME",String.class),
	METHOD_ABBREV("METHOD ABBREV",String.class),
	METHOD_NUMBER("METHOD NUMBER",String.class),
	METHOD_GROUP("METHOD GROUP",String.class),
	CROSS_FEMALE_GID("CROSS-FEMALE GID",String.class),
	CROSS_FEMALE_PREF_NAME("CROSS-FEMALE PREFERRED NAME",String.class),
	CROSS_MALE_GID("CROSS-MALE GID",String.class),
	CROSS_MALE_PREF_NAME("CROSS-MALE PREFERRED NAME",String.class);
	
	String name;
	
	@SuppressWarnings("rawtypes")
	Class classProperty;
	
	@SuppressWarnings("rawtypes")
	private AddColumnContextMenuOption(String name, Class classProperty) {
		this.name = name;
		this.classProperty = classProperty;
	}

	public String getName() {
		return name;
	}

	@SuppressWarnings("rawtypes")
	public Class getClassProperty() {
		return classProperty;
	}
	
	@SuppressWarnings("rawtypes")
	public static Class getClassProperty(String propertyId) {
		for(AddColumnContextMenuOption option : AddColumnContextMenuOption.values()){
			if(option.getName().equalsIgnoreCase(propertyId)){
				return option.getClassProperty();
			}
		}
		
		return null;
	}
	
	public static boolean isPartOfAddColumnContextMenuOption(String propertyId){
		for(AddColumnContextMenuOption option : AddColumnContextMenuOption.values()){
			if(option.getName().equalsIgnoreCase(propertyId)){
				return true;
			}
		}
		
		return false;
	}
}
