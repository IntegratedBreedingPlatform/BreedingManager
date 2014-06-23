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




/**
 * A class should implement this interface if it will perform updates on
 * Crosses Made information on a CrossesMadeContainer instance
 * 
 * @author Darla Ani
 *
 */
public interface CrossesMadeContainerUpdateListener {    
    
    /**
     * Validate fields and updates CrossesMadeContainer.
     * 
     * @return true if validation and update successful. Else, return false.
     */
    public boolean updateCrossesMadeContainer(CrossesMadeContainer container);
    
}
