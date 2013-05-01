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

package org.generationcp.breeding.manager.crossingmanager;

import java.util.Map;

import org.generationcp.breeding.manager.crossingmanager.pojos.Germplasm;
import org.generationcp.breeding.manager.crossingmanager.pojos.Name;

/**
 * A class should implement this interface if it will store the map
 * of new Germplasm crosses created in Breeding Manager - Make Crosses screen
 * 
 * @author Darla Ani
 *
 */
public interface StoresCrossesMade {
	
	public void setCrossesMadeMap(Map<Germplasm, Name> crossesMap);
	
}
