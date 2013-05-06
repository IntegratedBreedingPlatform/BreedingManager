/*******************************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 * 
 * Generation Challenge Programme (GCP)
 * 
 * 
 * This software is licensed for use under the terms of the GNU General Public
 * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
 * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 * 
 *******************************************************************************/
package org.generationcp.breeding.manager.crossingmanager.pojos;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Name;

/**
 * POJO designed as DTO (Data Transfer Object) for storing Germplasm crosses and
 * transferring between tabs in Crossing Manager
 * 
 * @author Darla Ani
 *
 */
public class CrossesMade implements Serializable{
	
	private static final long serialVersionUID = 8213778974745382283L;

	private Map<Germplasm, Name> crossesMap;
	
	//for storing old cross names when generated name is chosen by user
	private List<GermplasmListEntry> oldCrossNames; 
	
	public CrossesMade(){
	}
	
	public CrossesMade(Map<Germplasm, Name> crossesMap, List<GermplasmListEntry> oldCrosses){
		this.setCrossesMap(crossesMap);
		this.setOldCrossNames(oldCrosses);
	}

	public void setCrossesMap(Map<Germplasm, Name> crossesMap) {
		this.crossesMap = crossesMap;
	}

	public Map<Germplasm, Name> getCrossesMap() {
		return crossesMap;
	}

	public void setOldCrossNames(List<GermplasmListEntry> oldCrosses) {
		this.oldCrossNames = oldCrosses;
	}

	public List<GermplasmListEntry> getOldCrossNames() {
		return oldCrossNames;
	}
	
	/**
	 * Finds GermplasmListEntry for a Germplasm by matching GIDs.
	 * 
	 * @param germplasm
	 * @return GermplasmListEntry containing the old Cross Name. Returns null if no match found.
	 */
	public GermplasmListEntry findOldCrossNameEntry(Germplasm germplasm){
		if (getOldCrossNames() != null){
			for (GermplasmListEntry entry : getOldCrossNames()){
				if (entry.getGid().equals(germplasm.getGid())){
					return entry;
				}
			}
		}
		return null;
	}

}
