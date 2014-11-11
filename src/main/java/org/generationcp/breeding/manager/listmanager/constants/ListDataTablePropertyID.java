package org.generationcp.breeding.manager.listmanager.constants;

import org.generationcp.breeding.manager.application.Message;

public enum ListDataTablePropertyID {
	GID("gid", Message.LISTDATA_GID_HEADER)
    ,TAG("tag",Message.CHECK_ICON)
    ,ENTRY_ID("entryId",Message.HASHTAG)
    ,DESIGNATION("desig",Message.LISTDATA_DESIGNATION_HEADER)
    ,PARENTAGE("parentage",Message.LISTDATA_PARENTAGE_HEADER)
    ,AVAILABLE_INVENTORY("availInv",Message.LISTDATA_AVAIL_INV_HEADER)
    ,SEED_RESERVATION("seedRes",Message.LISTDATA_SEED_RES_HEADER)
    ,ENTRY_CODE("entryCode",Message.LISTDATA_ENTRY_CODE_HEADER)
    ,GID_VALUE("gid-value", Message.LISTDATA_GID_HEADER)
    ,SEED_SOURCE("seedSource",Message.LISTDATA_SEEDSOURCE_HEADER);
	
    private String name;
    private Message columnDisplay;
	
	private ListDataTablePropertyID(String name, Message columnDisplay){
		this.name = name;
		this.columnDisplay = columnDisplay;
	}
	
	public String getName(){
		return this.name;
	}
	
	public Message getColumnDisplay(){
		return this.columnDisplay;
	}
}
