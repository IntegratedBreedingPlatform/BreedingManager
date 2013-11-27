package org.generationcp.breeding.manager.listmanager.constants;

public enum ListDataTablePropertyID {
	GID("gid")
    ,GID_VALUE("gid-value")
    ,ENTRY_ID("entryId")
    ,ENTRY_CODE("entryCode")
    ,SEED_SOURCE("seedSource")
    ,DESIGNATION("desig")
    ,GROUP_NAME("groupName")
    ,STATUS("status")
    ,PARENTAGE("parentage");
    
    private String name;
	
	private ListDataTablePropertyID(String name){
		this.name = name;
	}
	
	public String getName(){
		return this.name;
	}
}
