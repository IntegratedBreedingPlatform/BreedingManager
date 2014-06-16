package org.generationcp.breeding.manager.listmanager.constants;

public enum ListDataTablePropertyID {
	GID("gid")
    ,GID_VALUE("gid-value")
    ,ENTRY_ID("entryId")
    ,ENTRY_CODE("entryCode")
    ,SEED_SOURCE("seedSource")
    ,DESIGNATION("desig")
    ,GROUP_NAME("groupName")
    ,PARENTAGE("parentage")
    ,AVAIL_INV("availInv")
    ,SEED_RES("seedRes")
	,TAG("tag");
    
    private String name;
	
	private ListDataTablePropertyID(String name){
		this.name = name;
	}
	
	public String getName(){
		return this.name;
	}
}
